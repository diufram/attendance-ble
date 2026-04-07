package com.example.attendance.navigation

import androidx.navigation.NavHostController
import com.example.attendance.model.MateriaModel

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

    fun irAsistenciaView(materia: MateriaModel) {
        navigateSafely("${AppRoutes.ASISTENCIA}/${materia.id}")
    }

    fun irInscritosView(materia: MateriaModel) {
        navigateSafely("${AppRoutes.INSCRITOS}/${materia.id}")
    }

    fun irAsistenciaDetalleView(materiaId: Long, asistenciaId: Long) {
        navigateSafely("${AppRoutes.ASISTENCIA_DETALLE}/$materiaId/$asistenciaId")
    }

    fun irNuevaAsistenciaView(materiaId: Long) {
        navigateSafely("${AppRoutes.ASISTENCIA_DETALLE}/$materiaId/-1")
    }

    fun volver() {
        navController.popBackStack()
    }
}
