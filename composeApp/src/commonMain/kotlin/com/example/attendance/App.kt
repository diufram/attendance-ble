package com.example.attendance

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.attendance.controller.DocenteController
import com.example.attendance.controller.EstudianteController
import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.view.screens.*
import com.example.attendance.view.theme.AttendanceTheme

@Composable
fun App(db: AttendanceDatabase) {
    val navController = rememberNavController()
    val docenteController = remember { DocenteController(db) }
    val estudianteController = remember { EstudianteController(db) }

    AttendanceTheme {
        NavHost(navController = navController, startDestination = "login") {

            composable("login") {
                LoginScreen(
                    onIngresar = { carnet, esDocente ->
                        if (esDocente) {
                            docenteController.ingresar(carnet)
                            navController.navigate("docente_home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            estudianteController.ingresar(carnet)
                            navController.navigate("estudiante_home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable("docente_home") {
                val docente by docenteController.docente.collectAsState()
                val materias by docenteController.materias.collectAsState()

                DocenteHomeScreen(
                    carnet = docente?.carnetIdentidad ?: 0,
                    materias = materias,
                    onCrearMateria = { sigla, nombre, grupo ->
                        docenteController.crearMateria(sigla, nombre, grupo)
                    },
                    onMateriaClick = { materiaId ->
                        navController.navigate("materia_detalle/$materiaId")
                    },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("materia_detalle/{materiaId}") { backStackEntry ->
                val materiaId = backStackEntry.arguments?.getString("materiaId")?.toLongOrNull() ?: return@composable
                val alumnos by docenteController.alumnos.collectAsState()
                val asistencias by docenteController.asistencias.collectAsState()
                val materias by docenteController.materias.collectAsState()
                val materia = materias.find { it.id == materiaId }

                LaunchedEffect(materiaId) {
                    docenteController.cargarAlumnos(materiaId)
                    docenteController.cargarAsistencias(materiaId)
                }

                MateriaDetalleScreen(
                    materiaNombre = materia?.let { "${it.sigla} - ${it.grupo}" } ?: "",
                    alumnos = alumnos,
                    asistencias = asistencias,
                    onBack = { navController.popBackStack() },
                    onAgregarAlumno = { carnet, nombre, apellido ->
                        docenteController.inscribirAlumno(materiaId, carnet, nombre, apellido)
                    },
                    onCargarCSV = { contenido ->
                        docenteController.cargarAlumnosDesdeCSV(materiaId, contenido)
                    },
                    onEliminarAlumno = { estudianteId ->
                        docenteController.desinscribirAlumno(materiaId, estudianteId)
                    },
                    onIniciarAsistencia = {
                        val fecha = "2026-04-04" // TODO: usar fecha real
                        val id = docenteController.crearAsistencia(materiaId, fecha)
                        navController.navigate("asistencia/$id")
                    },
                    onAsistenciaClick = { asistenciaId ->
                        navController.navigate("asistencia/$asistenciaId")
                    }
                )
            }

            composable("asistencia/{asistenciaId}") { backStackEntry ->
                val asistenciaId = backStackEntry.arguments?.getString("asistenciaId")?.toLongOrNull() ?: return@composable
                val detalles by docenteController.detalleAsistencia.collectAsState()

                LaunchedEffect(asistenciaId) {
                    docenteController.cargarDetalleAsistencia(asistenciaId)
                }

                AsistenciaScreen(
                    detalles = detalles,
                    onBack = { navController.popBackStack() },
                    onToggleEstado = { estudianteId, estadoActual ->
                        if (estadoActual == "PRESENTE") {
                            docenteController.marcarFalta(asistenciaId, estudianteId)
                        } else {
                            docenteController.marcarPresente(asistenciaId, estudianteId)
                        }
                    }
                )
            }

            composable("estudiante_home") {
                val estudiante by estudianteController.estudiante.collectAsState()
                val materias by estudianteController.materias.collectAsState()

                EstudianteHomeScreen(
                    carnet = estudiante?.carnetIdentidad ?: 0,
                    materias = materias,
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}