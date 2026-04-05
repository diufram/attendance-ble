package com.example.attendance

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.di.AttendanceContainer
import com.example.attendance.view.*
import com.example.attendance.view.theme.AttendanceTheme

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
                val loginEvent by loginController.navigationEvent.collectAsState()

                LaunchedEffect(loginEvent) {
                    val event = loginEvent ?: return@LaunchedEffect
                    asistenciaController.limpiar()
                    asistenciaDetalleController.limpiar()
                    inscritosController.limpiar()

                    if (event.esDocente) {
                        docenteController.cargarDocente(event.carnet)
                        navController.navigate("docente_home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        materiaEstudianteController.cargarEstudiante(event.carnet)
                        navController.navigate("estudiante_home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    loginController.limpiarNavegacion()
                }

                LoginView(controller = loginController)
            }

            composable("docente_home") {
                val docenteEvent by docenteController.navigationEvent.collectAsState()

                LaunchedEffect(docenteEvent) {
                    when (val event = docenteEvent) {
                        null -> Unit
                        is com.example.attendance.controller.MateriaDocenteController.NavigationEvent.IrAsistencia -> {
                            asistenciaController.seleccionarMateria(event.materia)
                            navController.navigate("asistencia")
                        }

                        com.example.attendance.controller.MateriaDocenteController.NavigationEvent.IrLogin -> {
                            asistenciaController.limpiar()
                            asistenciaDetalleController.limpiar()
                            inscritosController.limpiar()
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                    if (docenteEvent != null) docenteController.limpiarNavegacion()
                }

                MateriaDocenteView(controller = docenteController, model = materiaModel)
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
                val estudianteEvent by materiaEstudianteController.navigationEvent.collectAsState()

                LaunchedEffect(estudianteEvent) {
                    if (estudianteEvent is com.example.attendance.controller.MateriaEstudianteController.NavigationEvent.IrLogin) {
                        asistenciaController.limpiar()
                        asistenciaDetalleController.limpiar()
                        inscritosController.limpiar()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                        materiaEstudianteController.limpiarNavegacion()
                    }
                }

                MateriaEstudianteView(controller = materiaEstudianteController, model = materiaModel)
            }
        }
    }
}
