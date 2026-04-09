package com.example.attendance

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.attendance.controller.AsistenciaDetalleController
import com.example.attendance.controller.AsistenciaController
import com.example.attendance.controller.AuthController
import com.example.attendance.controller.InscritosController
import com.example.attendance.controller.MateriaDocenteController
import com.example.attendance.controller.MateriaEstudianteController
import com.example.attendance.db.AttendanceDatabase
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

@Composable
fun App(db: AttendanceDatabase) {
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
            estudianteModel = estudianteModel,
            asistenciaDetalleModel = asistenciaDetalleModel,
            navigator = appNavigation
        )
    }

    val asistenciaController = remember(appNavigation) {
        AsistenciaController(
            asistenciaModel = asistenciaModel,
            inscritoModel = inscritoModel,
            materiaModel = materiaModel,
            navigator = appNavigation
        )
    }

    val docenteController = remember(appNavigation) {
        MateriaDocenteController(
            materiaModel = materiaModel,
            navigator = appNavigation
        )
    }

    val materiaEstudianteController = remember(appNavigation) {
        MateriaEstudianteController(
            materiaModel = materiaModel,
            docenteModel = docenteModel,
            inscritoModel = inscritoModel,
            navigator = appNavigation
        )
    }

    val inscritosController = remember(appNavigation) {
        InscritosController(
            estudianteModel = estudianteModel,
            inscritoModel = inscritoModel,
            navigator = appNavigation
        )
    }

    val authController = remember(appNavigation, asistenciaController, asistenciaDetalleController) {
        AuthController(
            docenteModel = docenteModel,
            estudianteModel = estudianteModel,
            materiaModel = materiaModel,
            navigator = appNavigation
        )
    }

    AttendanceTheme {
        NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {

            composable(AppRoutes.LOGIN) {
                LoginView(
                    onLogin = authController::login,
                    onIrRegistro = { appNavigation.navigateSafely("registro") }
                )
            }

            composable("registro") {
                RegistroView(
                    onRegistrar = authController::registrar,
                    onVolver = { appNavigation.volver() }
                )
            }

            composable(AppRoutes.DOCENTE_HOME) {
                MateriaDocenteView(
                    model = materiaModel,
                    onCerrarSesion = docenteController::cerrarSesion,
                    onMateriaSeleccionada = docenteController::materiaSeleccionada,
                    onCrearMateria = docenteController::crearMateria
                )
            }

            composable(
                route = AppRoutes.ASISTENCIA_WITH_ID,
                arguments = listOf(navArgument("materiaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val args = backStackEntry.arguments
                val materiaId = args?.getString("materiaId")?.toLongOrNull()
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
                    onIrInscritos = { asistenciaController.abrirInscritos(materiaId) },
                    onIrCrearAsistencia = { asistenciaController.abrirNuevaAsistencia(materiaId) },
                    onAbrirDetalle = { asistenciaId -> asistenciaController.abrirDetalle(materiaId, asistenciaId) },
                    onGenerarQr = { asistenciaController.generarQr(materiaId) },
                )
            }

            composable(
                route = AppRoutes.INSCRITOS_WITH_ID,
                arguments = listOf(navArgument("materiaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val args = backStackEntry.arguments
                val materiaId = args?.getString("materiaId")?.toLongOrNull() 
                    ?: return@composable

                val materia = materiaModel.materiasUsuario.value.firstOrNull { it.id == materiaId }
                val materiaNombre = materia?.let { "${it.sigla} - ${it.grupo}" } ?: "Inscritos"

                LaunchedEffect(materiaId) {
                    inscritoModel.cargarInscritosMateria(materiaId)
                }

                InscritosView(
                    model = inscritoModel,
                    materiaId = materiaId,
                    materiaNombre = materiaNombre,
                    onVolver = inscritosController::volver,
                    onAgregarEstudiante = { carnet, nombre, apellido -> 
                        inscritosController.agregarEstudiante(materiaId, carnet, nombre, apellido) 
                    },
                    onImportarCsv = { contenido -> 
                        inscritosController.importarDesdeCsv(materiaId, contenido) 
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
                val args = backStackEntry.arguments
                val materiaId = args?.getString("materiaId")?.toLongOrNull() 
                    ?: return@composable
                val asistenciaId = args?.getString("asistenciaId")?.toLongOrNull() 
                    ?: return@composable
                val esNueva = asistenciaId == -1L

                val materiaActiva = materiaModel.materiasUsuario.value.firstOrNull { it.id == materiaId }
                val bleActivo by asistenciaDetalleController.bleActivo.collectAsState()
                val bleEstado by asistenciaDetalleController.bleEstado.collectAsState()

                DisposableEffect(asistenciaId) {
                    if (esNueva) {
                        val alumnos = estudianteModel.obtenerPorMateria(materiaId)
                        val detallesTemporales = alumnos.mapIndexed { index, alumno ->
                            com.example.attendance.model.AsistenciaDetalleModel(
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
                        asistenciaDetalleController.detenerBleDocente()
                    }
                }

                AsistenciaDetalleView(
                    model = asistenciaDetalleModel,
                    materiaSigla = materiaActiva?.sigla.orEmpty(),
                    materiaGrupo = materiaActiva?.grupo.orEmpty(),
                    bleActivo = bleActivo,
                    bleEstado = bleEstado,
                    esNuevaAsistencia = esNueva,
                    onVolver = asistenciaDetalleController::volver,
                    onAlternarEstado = asistenciaDetalleController::alternarEstado,
                    onIniciarEscaneo = {
                        asistenciaDetalleController.iniciarBleDocente(
                            sigla = materiaActiva?.sigla.orEmpty(),
                            grupo = materiaActiva?.grupo.orEmpty(),
                            asistenciaId = asistenciaId,
                            esNueva = esNueva
                        )
                    },
                    onDetenerEscaneo = asistenciaDetalleController::detenerBleDocente,
                    onGuardar = {
                        val guardado = asistenciaDetalleController.guardarAsistencia(materiaId, asistenciaId, esNueva)
                        if (guardado) {
                            asistenciaDetalleController.volver()
                        }
                    },
                )
            }

            composable(AppRoutes.ESTUDIANTE_HOME) {
                val bleEstado by materiaEstudianteController.bleEstado.collectAsState()
                val bleActivoMateriaId by materiaEstudianteController.bleActivoMateriaId.collectAsState()
                val bleConfirmacion by materiaEstudianteController.bleConfirmacion.collectAsState()
                DisposableEffect(materiaEstudianteController) {
                    onDispose {
                        materiaEstudianteController.detenerMarcadoAsistencia()
                    }
                }

                MateriaEstudianteView(
                    model = materiaModel,
                    bleEstado = bleEstado,
                    bleActivoMateriaId = bleActivoMateriaId,
                    bleConfirmacion = bleConfirmacion,
                    onCerrarSesion = materiaEstudianteController::cerrarSesion,
                    onRegistrarMateriaDesderQr = materiaEstudianteController::registrarMateriaDesdeQr,
                    onMarcarAsistencia = materiaEstudianteController::marcarAsistencia,
                    onDetenerMarcadoAsistencia = materiaEstudianteController::detenerMarcadoAsistencia,
                    onCerrarConfirmacionAsistencia = materiaEstudianteController::cerrarConfirmacionAsistencia,
                )
            }
        }
    }
}