package com.example.attendance.navigation

import androidx.navigation.NavHostController

object AppRoutes {
    const val LOGIN = "login"
    const val DOCENTE_HOME = "docente_home"
    const val ESTUDIANTE_HOME = "estudiante_home"
    const val ASISTENCIA = "asistencia"
    const val INSCRITOS = "inscritos"
    const val ASISTENCIA_DETALLE = "asistencia_detalle"

    const val ASISTENCIA_WITH_ID = "asistencia/{materiaId}"
    const val INSCRITOS_WITH_ID = "inscritos/{materiaId}"
    const val ASISTENCIA_DETALLE_WITH_ID = "asistencia_detalle/{materiaId}/{asistenciaId}"
}

class AppNavigation(
    private val navController: NavHostController,
    private val isNavigationLocked: () -> Boolean,
    private val lockNavigation: () -> Unit
) {
    var ultimoMateriaId: Long? = null
        private set
    var ultimoAsistenciaId: Long? = null
        private set

    fun navigateSafely(route: String) {
        if (isNavigationLocked()) return
        lockNavigation()
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = false
        }
    }

    fun navigateAndClearStack(route: String) {
        if (isNavigationLocked()) return
        lockNavigation()
        navController.navigate(route) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun irMateriaDocenteView() {
        navigateAndClearStack(AppRoutes.DOCENTE_HOME)
    }

    fun irMateriaEstudianteView() {
        navigateAndClearStack(AppRoutes.ESTUDIANTE_HOME)
    }

    fun irLoginView() {
        navigateAndClearStack(AppRoutes.LOGIN)
    }

    fun irAsistenciaView(materiaId: Long) {
        ultimoMateriaId = materiaId
        navigateSafely("${AppRoutes.ASISTENCIA}/$materiaId")
    }

    fun irInscritosView(materiaId: Long) {
        ultimoMateriaId = materiaId
        navigateSafely("${AppRoutes.INSCRITOS}/$materiaId")
    }

    fun irAsistenciaDetalleView(materiaId: Long, asistenciaId: Long) {
        ultimoMateriaId = materiaId
        ultimoAsistenciaId = asistenciaId
        navigateSafely("${AppRoutes.ASISTENCIA_DETALLE}/$materiaId/$asistenciaId")
    }

    fun irNuevaAsistenciaView(materiaId: Long) {
        ultimoMateriaId = materiaId
        ultimoAsistenciaId = -1L
        navigateSafely("${AppRoutes.ASISTENCIA_DETALLE}/$materiaId/-1")
    }

    fun volver() {
        navController.popBackStack()
    }
}
