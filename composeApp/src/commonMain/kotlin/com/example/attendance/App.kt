package com.example.attendance

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.attendance.controller.AsistenciaDetalleController
import com.example.attendance.controller.AsistenciaViewController
import com.example.attendance.controller.EstudianteHomeController
import com.example.attendance.controller.InscritosViewController
import com.example.attendance.controller.LoginController
import com.example.attendance.controller.MateriaDocenteController
import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.DetalleAsistenciaModel
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.view.*
import com.example.attendance.view.theme.AttendanceTheme

@Composable
fun App(db: AttendanceDatabase) {
    val navController = rememberNavController()

    val docenteModel = remember { DocenteModel(db) }
    val estudianteModel = remember { EstudianteModel(db) }
    val materiaModel = remember { MateriaModel(db) }
    val inscritoModel = remember { InscritoModel(db) }
    val asistenciaModel = remember { AsistenciaModel(db) }
    val detalleAsistenciaModel = remember { DetalleAsistenciaModel(db) }

    val docenteController = remember {
        MateriaDocenteController(
            docenteModel = docenteModel,
            materiaModel = materiaModel
        )
    }
    val estudianteController = remember {
        EstudianteHomeController(
            estudianteModel = estudianteModel,
            materiaModel = materiaModel
        )
    }
    val loginController = remember {
        LoginController(docenteModel, estudianteModel)
    }
    val asistenciaController = remember {
        AsistenciaViewController(
            estudianteModel = estudianteModel,
            asistenciaModel = asistenciaModel,
            detalleAsistenciaModel = detalleAsistenciaModel
        )
    }
    val asistenciaDetalleController = remember {
        AsistenciaDetalleController(detalleAsistenciaModel)
    }
    val inscritosController = remember {
        InscritosViewController(estudianteModel, inscritoModel)
    }

    AttendanceTheme {
        NavHost(navController = navController, startDestination = "login") {

            composable("login") {
                LoginView(
                    onIngresar = { carnet, esDocente ->
                        val error = loginController.iniciarSesion(carnet, esDocente)
                        if (error == null) {
                            val carnetInt = carnet.toIntOrNull()
                            if (carnetInt != null) {
                                if (esDocente) {
                                    docenteController.cargarDocente(carnetInt)
                                } else {
                                    estudianteController.cargarEstudiante(carnetInt)
                                }
                            }
                            navController.navigate(if (esDocente) "docente_home" else "estudiante_home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        error
                    }
                )
            }

            composable("docente_home") {
                val docente by docenteController.docente.collectAsState()
                val materias by docenteController.materias.collectAsState()

                MateriaDocenteView(
                    materias = materias,
                    onCrearMateria = { sigla, nombre, grupo ->
                        if (sigla.isBlank() || nombre.isBlank() || grupo.isBlank()) return@MateriaDocenteView false
                        docenteController.crearMateria(sigla, nombre, grupo)
                        true
                    },
                    onMateriaClick = { materiaId ->
                        val materiaSeleccionada = materias.find { it.id == materiaId }
                        if (materiaSeleccionada != null) {
                            asistenciaController.seleccionarMateria(materiaSeleccionada)
                            navController.navigate("asistencia")
                        }
                    },
                    onLogout = {
                        docenteController.cerrarSesion()
                        asistenciaController.limpiar()
                        asistenciaDetalleController.limpiar()
                        inscritosController.limpiar()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("asistencia") {
                val asistencias by asistenciaController.asistencias.collectAsState()
                val materia by asistenciaController.materiaSeleccionada.collectAsState()

                if (materia == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }
                val materiaActual = materia ?: return@composable

                AsistenciaView(
                    materiaNombre = "${materiaActual.sigla} - ${materiaActual.grupo}",
                    asistencias = asistencias,
                    onBack = { navController.popBackStack() },
                    onInscritosClick = {
                        inscritosController.seleccionarMateria(materiaActual)
                        navController.navigate("inscritos")
                    },
                    onIniciarAsistencia = {
                        val asistenciaId = asistenciaController.iniciarAsistenciaSeleccionada()
                        if (asistenciaId != null) {
                            asistenciaDetalleController.seleccionarAsistencia(asistenciaId)
                            navController.navigate("asistencia_detalle")
                        }
                    },
                    onAsistenciaClick = { asistenciaId ->
                        asistenciaDetalleController.seleccionarAsistencia(asistenciaId)
                        navController.navigate("asistencia_detalle")
                    }
                )
            }

            composable("inscritos") {
                val materia by inscritosController.materiaSeleccionada.collectAsState()
                val inscritos by inscritosController.inscritos.collectAsState()

                if (materia == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                val materiaActual = materia ?: return@composable

                InscritosView(
                    materiaNombre = "${materiaActual.sigla} - ${materiaActual.grupo}",
                    inscritos = inscritos,
                    onBack = { navController.popBackStack() },
                    onAgregarEstudiante = { carnet, nombre, apellido ->
                        inscritosController.agregarEstudiante(carnet, nombre, apellido)
                    },
                    onImportarCsv = { contenido ->
                        inscritosController.importarDesdeCsv(contenido)
                    }
                )
            }

            composable("asistencia_detalle") {
                val asistenciaId by asistenciaDetalleController.asistenciaSeleccionadaId.collectAsState()
                val detalles by asistenciaDetalleController.detalles.collectAsState()

                if (asistenciaId == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                AsistenciaDetalleView(
                    detalles = detalles,
                    onBack = { navController.popBackStack() },
                    onToggleEstado = { estudianteId, estadoActual ->
                        asistenciaDetalleController.alternarEstado(estudianteId, estadoActual)
                    }
                )
            }

            composable("estudiante_home") {
                val estudiante by estudianteController.estudiante.collectAsState()
                val materias by estudianteController.materias.collectAsState()

                EstudianteHomeView(
                    carnet = estudiante?.carnetIdentidad ?: 0,
                    materias = materias,
                    onLogout = {
                        estudianteController.cerrarSesion()
                        asistenciaController.limpiar()
                        asistenciaDetalleController.limpiar()
                        inscritosController.limpiar()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
