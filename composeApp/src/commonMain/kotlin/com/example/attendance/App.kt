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

    val asistenciaDetalleView = remember { AsistenciaDetalleViewData() }

    val asistenciaDetalleController = remember {
        AsistenciaDetalleController(
            asistenciaModel = asistenciaModel,
            asistenciaDetalleModel = asistenciaDetalleModel,
            estudianteModel = estudianteModel,
            view = asistenciaDetalleView
        )
    }

    val asistenciaView = remember { AsistenciaViewData() }

    val asistenciaController = remember {
        AsistenciaController(
            asistenciaModel = asistenciaModel,
            docenteModel = docenteModel,
            inscritoModel = inscritoModel,
            materiaModel = materiaModel,
            view = asistenciaView
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

    val inscritoView = remember { InscritoViewData() }

    val inscritosController = remember {
        InscritoController(
            estudianteModel = estudianteModel,
            inscritoModel = inscritoModel,
            view = inscritoView
        )
    }

    val authController = remember {
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
                    asistenciaController.iniciar(materiaId)
                }

                AsistenciaView(
                    view = asistenciaView,
                    materiaId = materiaId,
                    materiaNombre = materiaNombre,
                    materiaQrDetalle = materiaQrDetalle,
                    onVolver = { appNavigation.volver() },
                    onIrInscritos = { appNavigation.irInscritosView(materiaId) },
                    onIrCrearAsistencia = { appNavigation.irAsistenciaDetalleView(materiaId, -1L) },
                    onAbrirDetalle = { asistenciaId -> appNavigation.irAsistenciaDetalleView(materiaId, asistenciaId) },
                    onGenerarQr = { asistenciaController.generarQr(materiaId) },
                    onEliminar = { asistenciaController.eliminar(materiaId) },
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
                    inscritosController.iniciar(materiaId)
                }

                InscritoView(
                    view = inscritoView,
                    materiaNombre = materiaNombre,
                    onVolver = { appNavigation.volver() },
                    onAgregar = { inscritosController.agregar(materiaId) },
                    onEliminar = { inscritosController.eliminar(materiaId) },
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

                LaunchedEffect(asistenciaId) {
                    asistenciaDetalleController.iniciar(asistenciaId, esNueva, materiaId)
                }

                DisposableEffect(asistenciaId) {
                    onDispose {
                        asistenciaDetalleController.detenerEscaneo()
                    }
                }

                AsistenciaDetalleView(
                    view = asistenciaDetalleView,
                    materiaSigla = materiaActiva?.sigla.orEmpty(),
                    materiaGrupo = materiaActiva?.grupo.orEmpty(),
                    onVolver = { appNavigation.volver() },
                    onIniciarEscaneo = {
                        if (materiaActiva != null) {
                            asistenciaDetalleController.onIniciarEscaneo(materiaActiva)
                        }
                    },
                    onDetenerEscaneo = asistenciaDetalleController::detenerEscaneo,
                    onAlternarEstado = asistenciaDetalleController::alternarEstado,
                    onGuardar = {
                        val guardado = asistenciaDetalleController.guardar(materiaId, asistenciaId, esNueva)
                        if (guardado) {
                            appNavigation.volver()
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
