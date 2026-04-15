package com.example.attendance

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.attendance.controller.AsistenciaDetalleController
import com.example.attendance.controller.AsistenciaController
import com.example.attendance.controller.AuthController
import com.example.attendance.controller.InscritoController
import com.example.attendance.controller.MateriaDocenteController
import com.example.attendance.controller.MateriaEstudianteController
import com.example.attendance.db.Database
import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.AsistenciaDetalleModel
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation
import com.example.attendance.navigation.AppRoutes
import com.example.attendance.view.*
import com.example.attendance.view.theme.AttendanceTheme

private fun extraerLongArg(backStackEntry: NavBackStackEntry, key: String): Long? {
    val candidates = listOfNotNull(
        backStackEntry.arguments?.toString(),
        backStackEntry.destination.route,
        backStackEntry.toString(),
    )

    // 1) Buscar patrón "key=value"
    candidates.forEach { text ->
        val kvMatch = Regex("\\b$key=([-]?\\d+)").find(text)
        val value = kvMatch?.groupValues?.getOrNull(1)?.toLongOrNull()
        if (value != null) return value
    }

    // 2) Fallback por rutas conocidas
    candidates.forEach { text ->
        when (key) {
            "materiaId" -> {
                val materiaSimple = Regex("(?:asistencia|inscritos)/([-]?\\d+)").find(text)
                val materiaDetalle = Regex("asistencia_detalle/([-]?\\d+)/([-]?\\d+)").find(text)
                val value = materiaSimple?.groupValues?.getOrNull(1)?.toLongOrNull()
                    ?: materiaDetalle?.groupValues?.getOrNull(1)?.toLongOrNull()
                if (value != null) return value
            }

            "asistenciaId" -> {
                val match = Regex("asistencia_detalle/([-]?\\d+)/([-]?\\d+)").find(text)
                val value = match?.groupValues?.getOrNull(2)?.toLongOrNull()
                if (value != null) return value
            }
        }
    }

    return null
}

@Composable
fun App(db: Database) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var navigationLocked by remember { mutableStateOf(false) }

    val docenteModel = remember { DocenteModel(db = db) }
    val estudianteModel = remember { EstudianteModel(db = db) }
    val materiaModel = remember { MateriaModel(db = db) }
    val inscritoModel = remember { InscritoModel(db = db) }
    val asistenciaModel = remember { AsistenciaModel(db = db) }
    val asistenciaDetalleModel = remember { AsistenciaDetalleModel(db = db) }

    LaunchedEffect(navBackStackEntry?.destination?.route) {
        navigationLocked = false
    }

    val appNavigation = remember(navController) {
        AppNavigation(
            navController = navController,
            isNavigationLocked = { navigationLocked },
            lockNavigation = { navigationLocked = true }
        )
    }

    val asistenciaDetalleController = remember(appNavigation) {
        AsistenciaDetalleController(
            asistenciaModel = asistenciaModel,
            asistenciaDetalleModel = asistenciaDetalleModel,
            navigator = appNavigation
        )
    }

    val asistenciaController = remember(appNavigation) {
        AsistenciaController(
            asistenciaModel = asistenciaModel,
            docenteModel = docenteModel,
            inscritoModel = inscritoModel,
            materiaModel = materiaModel,
            navigator = appNavigation
        )
    }

    val materiaDocenteView = remember { MateriaDocenteViewData() }
    val materiaEstudianteView = remember { MateriaEstudianteViewData() }
    val loginView = remember { LoginViewData() }
    val registroView = remember { RegistroViewData() }

    val docenteController = remember {
        MateriaDocenteController(
            materiaModel = materiaModel,
            view = materiaDocenteView,
        )
    }

    val materiaEstudianteController = remember {
        MateriaEstudianteController(
            materiaModel = materiaModel,
            docenteModel = docenteModel,
            inscritoModel = inscritoModel,
            view = materiaEstudianteView,
        )
    }

    val inscritosController = remember(appNavigation) {
        InscritoController(
            estudianteModel = estudianteModel,
            inscritoModel = inscritoModel,
            navigator = appNavigation
        )
    }

    val authController = remember(appNavigation, asistenciaController, asistenciaDetalleController) {
        AuthController(
            docenteModel = docenteModel,
            estudianteModel = estudianteModel,
            loginView = loginView,
            registroView = registroView,
        )
    }

    AttendanceTheme {
        NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {

            composable(AppRoutes.LOGIN) {
                LoginView(
                    view = loginView,
                    onLogin = {
                        val resultado = authController.onLogin() ?: return@LoginView
                        val partes = resultado.split(":")
                        val destino = partes.getOrNull(0)
                        val carnet = partes.getOrNull(1)?.toLongOrNull() ?: return@LoginView
                        when (destino) {
                            "DOCENTE" -> appNavigation.irMateriaDocenteView(carnet)
                            "ESTUDIANTE" -> appNavigation.irMateriaEstudianteView(carnet)
                        }
                    },
                    onIrRegistro = { appNavigation.navigateSafely("registro") }
                )
            }

            composable("registro") {
                RegistroView(
                    view = registroView,
                    onRegistrar = {
                        val resultado = authController.onRegistrar() ?: return@RegistroView
                        val partes = resultado.split(":")
                        val destino = partes.getOrNull(0)
                        val carnet = partes.getOrNull(1)?.toLongOrNull() ?: return@RegistroView
                        when (destino) {
                            "DOCENTE" -> appNavigation.irMateriaDocenteView(carnet)
                            "ESTUDIANTE" -> appNavigation.irMateriaEstudianteView(carnet)
                        }
                    },
                    onVolver = { appNavigation.volver() }
                )
            }

            composable(
                route = AppRoutes.DOCENTE_HOME_WITH_ID,
                arguments = listOf(navArgument("carnet") { type = NavType.StringType })
            ) { backStackEntry ->
                val carnet = extraerLongArg(backStackEntry, "carnet") ?: return@composable
                LaunchedEffect(carnet) {
                    docenteController.iniciar(carnet)
                }

                MateriaDocenteView(
                    view = materiaDocenteView,
                    onCerrarSesion = {
                        docenteController.onCerrarSesion()
                        appNavigation.irLoginView()
                    },
                    irAsistenciaView = appNavigation::irAsistenciaView,
                    onCrear = docenteController::onCrear,
                    onGuardar = docenteController::onGuardar,
                    onEliminar = docenteController::onEliminar,
                )
            }

            composable(
                route = AppRoutes.ASISTENCIA_WITH_ID,
                arguments = listOf(navArgument("materiaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val materiaId = extraerLongArg(backStackEntry, "materiaId")
                    ?: appNavigation.ultimoMateriaId
                    ?: return@composable

                val materia = materiaModel.materiasUsuario.value.firstOrNull { it.id == materiaId }
                val materiaNombre = materia?.let { "${it.sigla} - ${it.grupo}" } ?: "Asistencia"
                val materiaQrDetalle = materia?.let { "${it.nombre} - ${it.sigla} - ${it.grupo}" } ?: materiaNombre

                LaunchedEffect(materiaId) {
                    asistenciaModel.cargarAsistenciasMateria(materiaId)
                }

                AsistenciaView(
                    materiaId = materiaId,
                    materiaNombre = materiaNombre,
                    materiaQrDetalle = materiaQrDetalle,
                    model = asistenciaModel,
                    onVolver = asistenciaController::volver,
                    onIrInscritos = { asistenciaController.irInscritos(materiaId) },
                    onIrCrearAsistencia = { asistenciaController.irCrearAsistencia(materiaId) },
                    onAbrirDetalle = { asistenciaId -> asistenciaController.abrirDetalle(materiaId, asistenciaId) },
                    onEliminar = { asistenciaId -> asistenciaController.eliminar(materiaId, asistenciaId) },
                    onGenerarQr = { asistenciaController.generarQr(materiaId) },
                )
            }

            composable(
                route = AppRoutes.INSCRITOS_WITH_ID,
                arguments = listOf(navArgument("materiaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val materiaId = extraerLongArg(backStackEntry, "materiaId")
                    ?: appNavigation.ultimoMateriaId
                    ?: return@composable

                val materia = materiaModel.materiasUsuario.value.firstOrNull { it.id == materiaId }
                val materiaNombre = materia?.let { "${it.sigla} - ${it.grupo}" } ?: "Inscritos"

                LaunchedEffect(materiaId) {
                    inscritoModel.cargarInscritosMateria(materiaId)
                }

                InscritoView(
                    model = inscritoModel,

                    materiaNombre = materiaNombre,
                    onVolver = inscritosController::volver,
                    onAgregar = { estudiante ->
                        inscritosController.agregar(materiaId, estudiante)
                    },
                    onEliminar = { estudiante ->
                        if (materia != null) {
                            inscritosController.eliminar(
                                InscritoModel(
                                    materiaId = materia.id,
                                    carnetIdentidad = estudiante.carnetIdentidad,
                                )
                            )
                        } else {
                            false
                        }
                    },
                )
            }

            composable(
                route = AppRoutes.ASISTENCIA_DETALLE_WITH_ID,
                arguments = listOf(
                    navArgument("materiaId") { type = NavType.StringType },
                    navArgument("asistenciaId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val materiaId = extraerLongArg(backStackEntry, "materiaId")
                    ?: appNavigation.ultimoMateriaId
                    ?: return@composable
                val asistenciaId = extraerLongArg(backStackEntry, "asistenciaId")
                    ?: appNavigation.ultimoAsistenciaId
                    ?: return@composable
                val esNueva = asistenciaId == -1L

                val materiaActiva = materiaModel.materiasUsuario.value.firstOrNull { it.id == materiaId }
                val bleActivo by asistenciaDetalleController.bleActivo.collectAsState()
                val bleEstado by asistenciaDetalleController.bleEstado.collectAsState()

                DisposableEffect(asistenciaId) {
                    if (esNueva) {
                        val alumnos = estudianteModel.obtenerPorMateria(materiaId)
                        val detallesTemporales = alumnos.mapIndexed { index, alumno ->
                            AsistenciaDetalleModel(
                                id = index.toLong(),
                                asistenciaId = -1L,
                                carnetIdentidad = alumno.carnetIdentidad,
                                estado = "FALTA",
                                nombreEstudiante = alumno.nombre,
                                apellidoEstudiante = alumno.apellido,
                                bitmapIndexEstudiante = index,
                            )
                        }
                        asistenciaDetalleModel.cargarDetallesTemporales(detallesTemporales)
                    } else {
                        asistenciaDetalleModel.cargarDetallesAsistencia(asistenciaId)
                    }
                    onDispose {
                        asistenciaDetalleController.detenerEscaneo()
                    }
                }

                AsistenciaDetalleView(
                    model = asistenciaDetalleModel,
                    materiaSigla = materiaActiva?.sigla.orEmpty(),
                    materiaGrupo = materiaActiva?.grupo.orEmpty(),
                    bleActivo = bleActivo,
                    bleEstado = bleEstado,
                    onVolver = asistenciaDetalleController::volver,
                    onAlternarEstado = asistenciaDetalleController::alternarEstado,
                    onIniciarEscaneo = {
                        if (materiaActiva != null) {
                            asistenciaDetalleController.onIniciarEscaneo(materiaActiva)
                        }
                    },
                    onDetenerEscaneo = asistenciaDetalleController::detenerEscaneo,
                    onGuardar = {
                        val asistencia = AsistenciaModel(
                            id = asistenciaId,
                            materiaId = materiaId,
                        )
                        val guardado = asistenciaDetalleController.guardar(asistencia)
                        if (guardado) {
                            asistenciaDetalleController.volver()
                        }
                    },
                )
            }

            composable(
                route = AppRoutes.ESTUDIANTE_HOME_WITH_ID,
                arguments = listOf(navArgument("carnet") { type = NavType.StringType })
            ) { backStackEntry ->
                val carnet = extraerLongArg(backStackEntry, "carnet") ?: return@composable
                val bleEstado by materiaEstudianteController.bleEstado.collectAsState()
                val bleActivoMateriaId by materiaEstudianteController.bleActivoMateriaId.collectAsState()
                val bleConfirmacion by materiaEstudianteController.bleConfirmacion.collectAsState()
                LaunchedEffect(carnet) {
                    materiaEstudianteController.iniciar(carnet)
                }
                DisposableEffect(materiaEstudianteController) {
                    onDispose {
                        materiaEstudianteController.detenerMarcadoAsistencia()
                    }
                }

                MateriaEstudianteView(
                    view = materiaEstudianteView,
                    bleEstado = bleEstado,
                    bleActivoMateriaId = bleActivoMateriaId,
                    bleConfirmacion = bleConfirmacion,
                    onCerrarSesion = {
                        materiaEstudianteController.cerrarSesion()
                        appNavigation.irLoginView()
                    },
                    onRegistrarMateriaDesdeQr = { payload ->
                        materiaEstudianteController.registrarMateriaDesdeQr(carnet, payload)
                    },
                    onMarcarAsistencia = materiaEstudianteController::marcarAsistencia,
                    onDetenerMarcadoAsistencia = materiaEstudianteController::detenerMarcadoAsistencia,
                    onCerrarConfirmacionAsistencia = materiaEstudianteController::cerrarConfirmacionAsistencia,
                )
            }
        }
    }
}
