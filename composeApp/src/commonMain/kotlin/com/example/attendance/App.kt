package com.example.attendance

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.di.AttendanceContainer
import com.example.attendance.view.*
import com.example.attendance.view.theme.AttendanceTheme

interface ILoginView {
    fun onMateriaDocenteView(carnet: Int)
    fun onMateriaEstudianteView(carnet: Int)
}

interface IMateriaDocenteView {
    fun irAsistencia(materia: com.example.attendance.model.MateriaModel)
    fun irLogin()
}

interface IMateriaEstudianteView {
    fun irLogin()
}

object EmptyLoginView : ILoginView {
    override fun onMateriaDocenteView(carnet: Int) = Unit
    override fun onMateriaEstudianteView(carnet: Int) = Unit
}

object EmptyMateriaDocenteView : IMateriaDocenteView {
    override fun irAsistencia(materia: com.example.attendance.model.MateriaModel) = Unit
    override fun irLogin() = Unit
}

object EmptyMateriaEstudianteView : IMateriaEstudianteView {
    override fun irLogin() = Unit
}

@Composable
fun App(db: AttendanceDatabase) {
    val navController = rememberNavController()
    val container = remember { AttendanceContainer(db) }
    val docenteController = container.materiaDocenteController
    val materiaEstudianteController = container.materiaEstudianteController
    val loginController = container.loginController
    val asistenciaController = container.asistenciaController
    val asistenciaDetalleController = container.asistenciaDetalleController
    val inscritosController = container.inscritosController
    val materiaModel = container.materiaModel
    val asistenciaModel = container.asistenciaModel
    val inscritoModel = container.inscritoModel
    val detalleAsistenciaModel = container.detalleAsistenciaModel

    AttendanceTheme {
        NavHost(navController = navController, startDestination = "login") {

            composable("login") {
                val loginView = remember(navController, asistenciaController, asistenciaDetalleController, inscritosController) {
                    object : ILoginView {
                        override fun onMateriaDocenteView(carnet: Int) {
                            asistenciaController.limpiar()
                            asistenciaDetalleController.limpiar()
                            inscritosController.limpiar()
                            navController.navigate("docente_home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }

                        override fun onMateriaEstudianteView(carnet: Int) {
                            asistenciaController.limpiar()
                            asistenciaDetalleController.limpiar()
                            inscritosController.limpiar()
                            navController.navigate("estudiante_home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                }

                DisposableEffect(loginView) {
                    loginController.setView(loginView)
                    onDispose { loginController.setView(EmptyLoginView) }
                }

                LoginView(onIniciarSesion = loginController::iniciarSesion)
            }

            composable("docente_home") {
                val docenteView = remember(navController, asistenciaController, asistenciaDetalleController, inscritosController) {
                    object : IMateriaDocenteView {
                        override fun irAsistencia(materia: com.example.attendance.model.MateriaModel) {
                            asistenciaController.seleccionarMateria(materia)
                            navController.navigate("asistencia")
                        }

                        override fun irLogin() {
                            asistenciaController.limpiar()
                            asistenciaDetalleController.limpiar()
                            inscritosController.limpiar()
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                }

                DisposableEffect(docenteView) {
                    docenteController.setView(docenteView)
                    onDispose { docenteController.setView(EmptyMateriaDocenteView) }
                }

                MateriaDocenteView(
                    model = materiaModel,
                    onLogout = docenteController::cerrarSesion,
                    onMateriaClick = docenteController::seleccionarMateria,
                    onCrearMateria = docenteController::crearMateria
                )
            }

            composable("asistencia") {
                val materia by asistenciaModel.materiaSeleccionada.collectAsState()
                val asistenciaEvent by asistenciaController.navigationEvent.collectAsState()

                if (materia == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                LaunchedEffect(asistenciaEvent) {
                    when (val event = asistenciaEvent) {
                        null -> Unit
                        com.example.attendance.controller.AsistenciaController.NavigationEvent.Volver -> {
                            navController.popBackStack()
                        }

                        is com.example.attendance.controller.AsistenciaController.NavigationEvent.IrInscritos -> {
                            inscritosController.seleccionarMateria(event.materia)
                            navController.navigate("inscritos")
                        }

                        is com.example.attendance.controller.AsistenciaController.NavigationEvent.IrDetalle -> {
                            asistenciaDetalleController.seleccionarAsistencia(event.asistenciaId)
                            navController.navigate("asistencia_detalle")
                        }
                    }
                    if (asistenciaEvent != null) asistenciaController.limpiarNavegacion()
                }

                AsistenciaView(controller = asistenciaController, model = asistenciaModel)
            }

            composable("inscritos") {
                val materia by inscritoModel.materiaSeleccionada.collectAsState()
                val inscritosEvent by inscritosController.navigationEvent.collectAsState()

                if (materia == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                LaunchedEffect(inscritosEvent) {
                    if (inscritosEvent is com.example.attendance.controller.InscritosController.NavigationEvent.Volver) {
                        navController.popBackStack()
                        inscritosController.limpiarNavegacion()
                    }
                }

                InscritosView(controller = inscritosController, model = inscritoModel)
            }

            composable("asistencia_detalle") {
                val asistenciaId by detalleAsistenciaModel.asistenciaSeleccionadaId.collectAsState()
                val detalleEvent by asistenciaDetalleController.navigationEvent.collectAsState()

                if (asistenciaId == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                LaunchedEffect(detalleEvent) {
                    if (detalleEvent is com.example.attendance.controller.AsistenciaDetalleController.NavigationEvent.Volver) {
                        navController.popBackStack()
                        asistenciaDetalleController.limpiarNavegacion()
                    }
                }

                AsistenciaDetalleView(controller = asistenciaDetalleController, model = detalleAsistenciaModel)
            }

            composable("estudiante_home") {
                val estudianteView = remember(navController, asistenciaController, asistenciaDetalleController, inscritosController) {
                    object : IMateriaEstudianteView {
                        override fun irLogin() {
                            asistenciaController.limpiar()
                            asistenciaDetalleController.limpiar()
                            inscritosController.limpiar()
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                }

                DisposableEffect(estudianteView) {
                    materiaEstudianteController.setView(estudianteView)
                    onDispose { materiaEstudianteController.setView(EmptyMateriaEstudianteView) }
                }

                MateriaEstudianteView(
                    model = materiaModel,
                    onLogout = materiaEstudianteController::cerrarSesion
                )
            }
        }
    }
}
