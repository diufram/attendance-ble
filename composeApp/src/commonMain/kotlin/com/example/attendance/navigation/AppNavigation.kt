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
}

class AppNavigation(
    private val navController: NavHostController,
    private val isNavigationLocked: () -> Boolean,
    private val lockNavigation: () -> Unit,
    private val onIrMateriaDocenteView: () -> Unit = {},
    private val onIrMateriaEstudianteView: () -> Unit = {},
    private val onIrLoginView: () -> Unit = {},
    private val onIrAsistenciaView: (MateriaModel) -> Unit = {}
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
        onIrMateriaDocenteView()
        navigateAndClearStack(AppRoutes.DOCENTE_HOME)
    }

    fun irMateriaEstudianteView() {
        onIrMateriaEstudianteView()
        navigateAndClearStack(AppRoutes.ESTUDIANTE_HOME)
    }

    fun irLoginView() {
        onIrLoginView()
        navigateAndClearStack(AppRoutes.LOGIN)
    }

    fun irAsistenciaView(materia: MateriaModel) {
        onIrAsistenciaView(materia)
        navigateSafely(AppRoutes.ASISTENCIA)
    }
}
