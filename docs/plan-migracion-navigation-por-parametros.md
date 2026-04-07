# Plan de Migración: Navigation por IDs en lugar de Estado Global

## Objetivo
Migrar de usar estado global (`materiaSeleccionada`, `asistenciaSeleccionadaId`) a pasar IDs directamente por parámetros de ruta en la navegación.

## Estado Actual (Problema)

### Modelo con estado global:
```kotlin
// AsistenciaModel
private val _materiaSeleccionada = MutableStateFlow<MateriaModel?>(null)
val materiaSeleccionada: StateFlow<MateriaModel?>

// DetalleAsistenciaModel
private val _asistenciaSeleccionadaId = MutableStateFlow<Long?>(null)
val asistenciaSeleccionadaId: StateFlow<Long?>
```

### Navegación actual:
- `docente_home` → `asistencia` (usa estado global en modelo)
- `asistencia` → `asistencia_detalle` (usa estado global en modelo)

### Flujo actual (problemático):
1. User navigates to `asistencia`
2. Controller setea `asistenciaModel.setMateriaSeleccionada(materia)`
3. Pantalla lee `model.materiaSeleccionada.collectAsState()`
4. Si vuelve a entrar, puede ver datos viejos

## Estado Objetivo (Meta)

### Navegación con parámetros:
- `docente_home` → `asistencia/{materiaId}` 
- `asistencia/{materiaId}` → `asistencia_detalle/{asistenciaId}`

### Flujo mejorado:
1. User navega a `asistencia/123` (pasando materiaId=123)
2. Pantalla lee `materiaId` de la ruta
3. `LaunchedEffect(materiaId)` carga datos desde BD
4. Datos siempre vienen frescos de la DB

---

## Checklist de Migración

### Fase 1: Migrar AsistenciaDetalleView

#### 1.1. Modificar schema SQL (si aplica)
- No hay cambio de schema necesario

#### 1.2. Agregar nuevas rutas en AppNavigation.kt
```kotlin
// Agregar:
fun irAsistenciaDetalleView(materiaId: Long, asistenciaId: Long)
```

#### 1.3. Agregar nueva route en NavHost
```kotlin
composable(
    route = "${AppRoutes.ASISTENCIA_DETALLE}/{asistenciaId}",
    arguments = listOf(navArgument("asistenciaId") { type = NavType.LongType })
) { backStackEntry ->
    val asistenciaId = backStackEntry.arguments?.getLong("asistenciaId") ?: return@composable
    // mostrar pantalla
}
```

#### 1.4. Modificar AsistenciaDetalleView
- Cambiar parámetro `materiaSigla/materiaGrupo` por `materiaId`
- En `LaunchedEffect`, cargar materia por ID

#### 1.5. Modificar AsistenciaController
- `abrirDetalle(asistenciaId)` → debe pasar ambos IDs
- `navigator.irAsistenciaDetalleView(materiaId, asistenciaId)`

#### 1.6. Modificar AsistenciaView
- Pasar `materiaId` al callback `onAbrirDetalle`
- Algo como `onAbrirDetalle(materia.id, asistencia.id)`

---

### Fase 2: Migrar AsistenciaView

#### 2.1. Agregar ruta con parámetro en NavHost
```kotlin
composable(
    route = "${AppRoutes.ASISTENCIA}/{materiaId}",
    arguments = listOf(navArgument("materiaId") { type = NavType.LongType })
)
```

#### 2.2. Modificar AsistenciaView
- Eliminar uso de `model.materiaSeleccionada`
- Recibir `materiaId` como parámetro
- En `LaunchedEffect(materiaId)`, cargar asistencia por materiaId

#### 2.3. Modificar flujo de navegación
- docenteController → ahora hace `navigator.irAsistenciaView(materia.id)`
- AsistenciaController → ya no necesita `seleccionarMateria()`

#### 2.4. Limpiar AsistenciaModel
- Eliminar `setMateriaSeleccionada()`
- Eliminar `_materiaSeleccionada`
- Eliminar `limpiarEstadoMateria()`

---

### Fase 3: Migrar InscritosView

#### 3.1. Similar a Fase 2 pero para Inscritos
- Ruta: `inscritos/{materiaId}`
- Pantalla recibe `materiaId` por parámetro
- Carga datos en `LaunchedEffect`

---

### Fase 4: Limpieza Final

#### 4.1. Eliminar estados obsoletos
- `AsistenciaModel._materiaSeleccionada`
- `DetalleAsistenciaModel._asistenciaSeleccionadaId`
- `InscritoModel._materiaSeleccionada`

#### 4.2. Eliminar métodos no usados
- `setMateriaSeleccionada()`
- `setAsistenciaSeleccionada()`
- `setMateriaSeleccionada()` en InscritoModel

---

## Resumen de Archivos a Modificar

| Archivo | Cambio |
|---------|--------|
| `AppNavigation.kt` | Agregar rutas con parámetros |
| `App.kt` | Modificar NavHost, agregar argumentos |
| `AsistenciaDetalleView.kt` | Recibir materiaId, cargar en LaunchedEffect |
| `AsistenciaView.kt` | Recibir materiaId, cargar en LaunchedEffect |
| `InscritosView.kt` | Recibir materiaId, cargar en LaunchedEffect |
| `AsistenciaController.kt` | Eliminar setView, navegación por navigator |
| `AsistenciaModel.kt` | Eliminar _materiaSeleccionada |
| `DetalleAsistenciaModel.kt` | Eliminar _asistenciaSeleccionadaId |

---

## Orden Sugerido de Implementación

1. **Primero AsistenciaDetalle** (más simple, un solo ID)
2. **Segundo Asistencia** (ya que depende de detalle)
3. **Tercero Inscritos** (similar a Asistencia)
4. **Limpieza final** (quitar estados sobrantes)

---

## Ejemplo de Código Resultante

### Nueva AsistenciaView:
```kotlin
@Composable
fun AsistenciaView(
    materiaId: Long,
    onVolver: () -> Unit,
    onAbrirInscritos: () -> Unit,
    onCrearAsistencia: () -> Unit,
    onAbrirDetalle: (asistenciaId: Long) -> Unit,
    onGenerarQr: () -> String?
) {
    val model = remember { AsistenciaModel() }
    val materia by model.materiaSeleccionada.collectAsState()
    
    LaunchedEffect(materiaId) {
        model.cargarPorId(materiaId)
    }
    
    // Resto del UI...
}
```

### Nueva Ruta:
```kotlin
composable(
    route = "${AppRoutes.ASISTENCIA}/{materiaId}",
    arguments = listOf(navArgument("materiaId") { type = NavType.LongType })
) { backStackEntry ->
    val materiaId = backStackEntry.arguments?.getLong("materiaId") ?: return@composable
    
    AsistenciaView(
        materiaId = materiaId,
        onVolver = { ... },
        // ...
    )
}
```

---

## Validaciones Previas (antes de empezar)

- [ ] Backup del proyecto
- [ ] Compilar antes de cambios
- [ ] Tests funcionales (si existen)
- [ ] Revisar que no haya otras pantallas usando estos modelos

---

## Notas Importantes

1. **No borrar todo de golpe**: hacer por fases y validar que compila cada fase.
2. **Mantener compatibilidad hacia atrás** mientras se migra: dejar los dos flujos posibles si es necesario.
3. **Probar navegación manual**: navegar a cada pantalla para verificar que funciona.
4. **Limpiar cache de navegación**: al cambiar rutas, el historial puede tener rutas obsoletas.

---

## Fin del Plan