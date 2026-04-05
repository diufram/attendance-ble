# Estructura de paquetes BLE para el sistema de asistencia

## Objetivo

Implementar un protocolo BLE basado en advertisement legacy para registrar asistencia entre estudiantes y docente usando bitmap por materia/grupo.

## 1) Paquete del estudiante

El estudiante emite un paquete BLE advertisement con 3 campos:

- `sigla` de la materia
- `grupo`
- `indice` en el bitmap

Ejemplo:

- Estudiante inscrito en `INF-321`, grupo `SA`, con indice `30`
- Payload: `INF-321 SA 30`

Tamano por campo:

- `sigla`: hasta 7 bytes (UTF-8, 1 caracter por byte esperado)
- `grupo`: 2 bytes
- `indice`: 1 byte

Total payload estudiante: **10 bytes**.

Capacidad:

- 1 byte para indice soporta valores `0..255`
- Maximo: **256 alumnos por materia**

## 2) Limite de datos en BLE advertisement legacy

- Tamaño total del advertisement legacy: **31 bytes**
- Overhead BLE (flags, service id, headers): **9 bytes**
- Datos custom disponibles: **22 bytes**

Como el payload del estudiante usa 10 bytes, cabe con margen:

- Disponible: 22 bytes
- Usado por estudiante: 10 bytes
- Sobra: **12 bytes**

## 3) Paquete del profesor (confirmacion por fragmentos)

El docente emite un advertisement con 4 campos:

- `sigla` (7 bytes)
- `grupo` (2 bytes)
- `ARK` (1 byte)
- `fragmento` del bitmap (hasta 12 bytes)

Cabecera fija docente:

- `7 + 2 + 1 = 10 bytes`

Con 22 bytes de datos custom, quedan:

- `22 - 10 = 12 bytes` para bitmap por fragmento

### Rol del ARK

`ARK` es un byte que cumple dos funciones:

1. Diferencia paquetes del docente frente a paquetes de estudiante.
2. Indica el numero de fragmento del bitmap.

Convencion:

- `0x01` -> primer fragmento
- `0x02` -> segundo fragmento
- `0x03` -> tercero
- etc.

## 4) Cobertura del bitmap y fragmentacion

- Cada byte del bitmap = 8 bits
- Cada bit representa 1 alumno
- 12 bytes por fragmento = `12 * 8 = 96 alumnos`

Ejemplo clase de 200 alumnos:

- Bitmap total: 200 bits = 25 bytes
- Se divide en 3 fragmentos:

1. `ARK 0x01`: bytes 0..11 del bitmap (indices 0..95)
2. `ARK 0x02`: bytes 12..23 del bitmap (indices 96..191)
3. `ARK 0x03`: byte 24 del bitmap (indices 192..199)

## 5) Como diferencia señales cada actor

### Estudiante al escanear

- Si el paquete contiene campo `ARK` -> es confirmacion del docente.
- Extrae el fragmento y reconstruye bitmap.
- Si no contiene `ARK` -> lo ignora (es anuncio de otro estudiante).

### Docente al escanear

- Si el paquete **no** tiene `ARK` -> lo interpreta como anuncio de estudiante.
- Extrae `indice` y marca ese bit en el bitmap como presente.

## 6) Verificacion del estudiante

Cada estudiante conoce su propio `indice`.
Cuando reconstruye el bitmap completo recibido del docente, valida su presencia con:

- `byteIndex = indice / 8`
- `bitIndex = indice % 8`
- `confirmado = (bitmap[byteIndex] & (1 << bitIndex)) != 0`

Si `confirmado` es verdadero, fue marcado como presente.

---

## Estado actual del proyecto (base existente)

Ya existe una base muy util para implementar BLE:

- Inscripciones con `bitmap_index` en BD (`inscrito.bitmap_index`)
- Flujo docente/estudiante y navegacion separados
- Patron `expect/actual` ya aplicado (QR, CSV), reutilizable para BLE

Lo que aun falta:

- Capa BLE (advertiser/scanner)
- Codec binario para paquetes estudiante/docente
- Sesion BLE para fragmentacion y reconstruccion de bitmap
- Integracion con asistencia en vivo y permisos Bluetooth Android + iOS

---

## Pasos a seguir (roadmap de implementacion)

## Paso 1: Definir protocolo binario comun (commonMain)

Crear utilidades para:

- Serializar/parsear paquete estudiante (`10 bytes`).
- Serializar/parsear paquete docente (`10 + N bytes`, `N<=12`).
- Validar tamanos fijos (`sigla<=7`, `grupo==2`, `indice 0..255`).
- Operaciones bitmap (`setBit`, `isBitSet`, `byteIndex`, `bitIndex`).

Entregable:

- Modulo `BlePacketCodec` en `commonMain` + pruebas unitarias.

## Paso 2: Motor de sesion BLE en commonMain

Crear logica de negocio independiente de plataforma:

- **Docente**:
  - Inicializa bitmap de la asistencia activa.
  - Marca bits al recibir anuncios de estudiantes.
  - Fragmenta bitmap en bloques de 12 bytes.
  - Emite fragmentos con `ARK` incremental.

- **Estudiante**:
  - Emite su paquete con `sigla+grupo+indice`.
  - Escanea fragmentos del docente de su materia/grupo.
  - Reconstruye bitmap completo y verifica su bit.

Entregable:

- `BleAttendanceSession` con estados claros (inactivo, emitiendo, escaneando, confirmado).

## Paso 3: Abstraccion multiplataforma BLE (expect/actual)

Definir interfaz comun, por ejemplo:

- `startAdvertising(payload: ByteArray)`
- `startScanning(onPacket: (ByteArray) -> Unit)`
- `stopAll()`

Implementaciones:

- Android: real (BLE advertiser + scanner).
- iOS: real (CoreBluetooth como peripheral + central).

Entregable:

- API BLE reutilizable desde controllers/views.

## Paso 4: Android permisos y wiring

Agregar permisos Bluetooth en Manifest y runtime segun version Android:

- `BLUETOOTH_SCAN`
- `BLUETOOTH_ADVERTISE`
- `BLUETOOTH_CONNECT`
- Compatibilidad para versiones anteriores cuando aplique.

Entregable:

- Flujo de permisos con mensajes de error claros en UI.

## Paso 5: iOS CoreBluetooth y wiring

Implementar BLE nativo en iOS con `CoreBluetooth`:

- `CBPeripheralManager` para advertisement de paquetes.
- `CBCentralManager` para escaneo de paquetes.
- Mapeo del payload custom en manufacturer data o service data.
- Control de estados Bluetooth (`poweredOn`, `poweredOff`, etc.).
- Manejo de permisos y mensajes UX cuando Bluetooth no este disponible.

Entregable:

- Implementacion iOS funcional equivalente a Android para docente y estudiante.

## Paso 6: Integracion con controladores y vistas

Integrar en el flujo actual:

- Docente (asistencia activa):
  - Iniciar/detener sesion BLE.
  - Ver contador de detectados en tiempo real.
  - Actualizar `detalle_asistencia` a `PRESENTE` al marcar bit.

- Estudiante (materia seleccionada):
  - Accion `Marcar asistencia BLE`.
  - Mostrar estado local: esperando / confirmado / error.

Entregable:

- Pantallas conectadas al motor BLE sin romper arquitectura actual.

## Paso 7: Persistencia y consistencia

- Mantener fuente de verdad en BD para detalle de asistencia.
- Evitar duplicados de marcacion por mismo indice.
- Reintentos controlados para anuncios/escaneo.

Entregable:

- Actualizacion estable de estados incluso ante escaneos repetidos.

## Paso 8: Pruebas

Pruebas unitarias:

- Codec de paquetes.
- Fragmentacion de bitmap.
- Reconstruccion por `ARK`.
- Verificacion de bit por indice.

Casos limite:

- Indices `0`, `95`, `96`, `191`, `192`, `255`.
- Clases vacias y clases > 96 alumnos.
- Fragmento final parcial (menos de 12 bytes).

Pruebas funcionales:

- 1 docente + multiples estudiantes en Android real.
- 1 docente + multiples estudiantes en iOS real.
- Prueba cruzada Android <-> iOS para validar interoperabilidad del payload.
- Confirmacion visual correcta en ambos lados.

---

## Recomendacion de alcance inicial (v1)

Alcance definido para esta version:

- Implementar **v1 Android + iOS completos** (advertise/scan real en ambos).
- Asegurar interoperabilidad entre plataformas desde la primera entrega.
