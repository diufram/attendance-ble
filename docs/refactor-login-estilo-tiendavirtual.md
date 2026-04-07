# Plan de Refactorización: Login al estilo TiendaVirtual

## Objetivo
Migrar el Login de Attendance para seguir el mismo patrón que TiendaVirtual, donde:
- El Controller crea directamente la Vista
- La Vista recibe callbacks como parámetros en lugar de interfaces

## Estructura Actual (Attendance)

```
LoginController (recibe ILoginView por constructor)
    ↓
    view.onMateriaDocenteView(carnet) / view.onMateriaEstudianteView(carnet)
    ↓
App.kt (implementa ILoginView → navegación)
```

## Estructura Target (Estilo TiendaVirtual)

```
LoginController
    ├── Crea VLogin() directamente
    ├── Configura callbacks (onIrMateriaDocente, onIrMateriaEstudiante)
    └── Llama a vista.mostrarPantalla()
         ↓
VLogin
    └── @Composable fun mostrarPantalla(
            onIniciarSesion: (carnet, esDocente) -> String?,
            onIrMateriaDocente: (carnet) -> Unit,
            onIrMateriaEstudiante: (carnet) -> Unit
        )
```

## Pasos a Seguir

### 1. Modificar VLogin.kt
- [x] Renombrar `LoginView` a `VLogin` (clase)
- [x] Crear método `mostrarPantalla()` que reciba callbacks
- [x] Mover el `@Composable` `LoginView` dentro de la clase como método de la misma

### 2. Modificar LoginController.kt
- [x] Crear instancia de `VLogin()`
- [x] Configurar los callbacks `onIrMateriaDocente` y `onIrMateriaEstudiante`
- [x] Llamar a `vista.mostrarPantalla(...)`

### 3. Modificar App.kt
- [x] Eliminar interfaz `ILoginView`
- [x] Eliminar objeto `EmptyLoginView`
- [x] Crear función de navegación en `NavHost` que use `LoginController`

### 4. Eliminar dependencias
- [x] Remover dependencias de `ILoginView` en LoginController y Container

## Código Resultante

### VLogin.kt (nuevo)
```kotlin
class VLogin {
    @Composable
    fun mostrarPantalla(
        onIniciarSesion: (String, Boolean) -> String?,
        onIrMateriaDocente: (Int) -> Unit,
        onIrMateriaEstudiante: (Int) -> Unit
    ) {
        // UI actual de LoginView
    }
}
```

### LoginController.kt (nuevo)
```kotlin
class LoginController(
    private val docenteModel: DocenteModel,
    private val estudianteModel: EstudianteModel,
    private val materiaModel: MateriaModel
) {
    private val vista = VLogin()
    private var onIrMateriaDocente: ((Int) -> Unit)? = null
    private var onIrMateriaEstudiante: ((Int) -> Unit)? = null

    fun configurarNavegacion(
        onIrMateriaDocente: (Int) -> Unit,
        onIrMateriaEstudiante: (Int) -> Unit
    ) {
        this.onIrMateriaDocente = onIrMateriaDocente
        this.onIrMateriaEstudiante = onIrMateriaEstudiante
    }

    @Composable
    fun mostrar() {
        vista.mostrarPantalla(
            onIniciarSesion = { carnet, esDocente -> iniciarSesion(carnet, esDocente) },
            onIrMateriaDocente = { onIrMateriaDocente?.invoke(it) },
            onIrMateriaEstudiante = { onIrMateriaEstudiante?.invoke(it) }
        )
    }

    private fun iniciarSesion(carnetInput: String, esDocente: Boolean): String? {
        // ... lógica actual
    }
}
```

### App.kt (cambios)
```kotlin
// En NavHost:
composable("login") {
    val controller = remember { LoginController(...) }
    controller.configurarNavegacion(
        onIrMateriaDocente = { carnet -> nav.navigate("materia_docente/$carnet") },
        onIrMateriaEstudiante = { carnet -> nav.navigate("materia_estudiante/$carnet") }
    )
    controller.mostrar()
}
```
