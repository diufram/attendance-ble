# Instrucciones de Depuración - Agregar Estudiantes en iOS

## ✅ ÚLTIMA ACTUALIZACIÓN: Patrón replicado de "Crear Materia"

## Cambios realizados:

### 1. **Base de datos actualizada**
- ✅ Cambiado nombre de BD a `attendance_v3.db` (fuerza recreación)
- ✅ `estudiante.carnet_identidad` ahora tiene UNIQUE constraint
- ✅ `docente` ahora tiene estructura consistente con `estudiante`
- ✅ `inscrito` tiene UNIQUE constraints para evitar duplicados
- ✅ `materia.docente_id` es nullable

### 2. **Validaciones agregadas**
- ✅ Validación de campos vacíos en el controlador
- ✅ Validación de campos vacíos en la vista
- ✅ Mensajes de error con Snackbar

### 3. **Logging agregado**
- ✅ Prints de errores en el controlador
- ✅ Stack traces de excepciones

## Pasos para probar en iOS:

### A. Recompilar completamente

```bash
# Limpiar todo
./gradlew clean

# Borrar carpetas de build
rm -rf composeApp/build
rm -rf build

# Regenerar código SQLDelight
./gradlew :composeApp:generateCommonMainAttendanceDatabaseInterface
```

### B. Desde Xcode

```bash
# Abrir proyecto
open iosApp/iosApp.xcodeproj
```

1. En Xcode, selecciona: **Product → Clean Build Folder** (Cmd+Shift+K)
2. **Borra la app del simulador** (presiona y mantén el ícono → Eliminar)
3. Ejecuta la app (Cmd+R)

### C. Probar funcionalidad

1. **Login como docente**: Carnet: 12345678
2. **Crear materia**:
   - Sigla: INF-301
   - Nombre: Base de Datos
   - Grupo: A
   - Periodo: 1-2026
3. **Tocar la materia** para ver opciones
4. **Ir a "Inscritos"** o similar
5. **Agregar estudiante**:
   - Carnet: 87654321
   - Nombre: Juan
   - Apellido: Pérez
6. **Guardar**

### D. Ver logs en Xcode

Si falla, revisa la consola de Xcode para ver los mensajes:
- `Error: Carnet vacío`
- `Error: Nombre vacío`
- `Error: Apellido vacío`
- `Error: No hay materia seleccionada`
- `Error: Carnet inválido`
- `Error al agregar estudiante: [mensaje]`

## Posibles problemas y soluciones:

### Problema 1: La BD antigua no se eliminó
**Solución**: Borrar completamente la app del simulador antes de ejecutar

### Problema 2: El código SQLDelight no se regeneró
**Solución**: Ejecutar `./gradlew :composeApp:generateCommonMainAttendanceDatabaseInterface`

### Problema 3: Foreign key constraint falla
**Síntoma**: Error con "FOREIGN KEY constraint failed"
**Causa**: Intentando insertar estudiante con `docente_id` inválido
**Solución**: Ya está arreglado con `docente_id` nullable

### Problema 4: Unique constraint falla
**Síntoma**: Error con "UNIQUE constraint failed"
**Causa**: Intentando insertar carnet duplicado
**Solución**: El código ya verifica estudiantes existentes

## Verificar que el esquema es correcto:

El archivo generado debería estar en:
```
composeApp/build/generated/sqldelight/code/AttendanceDatabase/commonMain/com/example/attendance/db/composeApp/AttendanceDatabaseImpl.kt
```

Busca las tablas y verifica que tengan:
- `estudiante.carnet_identidad INTEGER NOT NULL UNIQUE`
- `docente.id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT`
- `materia.docente_id INTEGER` (nullable, sin NOT NULL)
- `inscrito` con dos UNIQUE constraints
