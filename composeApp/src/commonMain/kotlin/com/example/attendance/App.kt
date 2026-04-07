package com.example.attendance

import androidx.compose.runtime.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.attendance.controller.LoginController
import com.example.attendance.controller.MateriaDocenteController
import com.example.attendance.controller.MateriaEstudianteController
import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.di.AttendanceContainer
import com.example.attendance.navigation.AppNavigation
import com.example.attendance.navigation.AppRoutes
import com.example.attendance.view.*
import com.example.attendance.view.theme.AttendanceTheme

interface IAsistenciaView {
    fun irVolver()
    fun irInscritos(materia: com.example.attendance.model.MateriaModel)
    fun irDetalle(asistenciaId: Long)
    fun irNuevaAsistencia(materiaId: Long)
}

interface IInscritosView {
    fun irVolver()
}

interface IAsistenciaDetalleView {
    fun irVolver()
}

object EmptyAsistenciaView : IAsistenciaView {
    override fun irVolver() = Unit
    override fun irInscritos(materia: com.example.attendance.model.MateriaModel) = Unit
    override fun irDetalle(asistenciaId: Long) = Unit
    override fun irNuevaAsistencia(materiaId: Long) = Unit
}

object EmptyInscritosView : IInscritosView {
    override fun irVolver() = Unit
}

object EmptyAsistenciaDetalleView : IAsistenciaDetalleView {
    override fun irVolver() = Unit
}

@Composable
fun App(db: AttendanceDatabase) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var navigationLocked by remember { mutableStateOf(false) }
    val container = remember { AttendanceContainer(db) }
    val asistenciaController = container.asistenciaController
    val asistenciaDetalleController = container.asistenciaDetalleController
    val inscritosController = container.inscritosController
    val materiaModel = container.materiaModel
    val asistenciaModel = container.asistenciaModel
    val inscritoModel = container.inscritoModel
    val detalleAsistenciaModel = container.detalleAsistenciaModel

    LaunchedEffect(navBackStackEntry?.destination?.route) {
        navigationLocked = false
    }

    val appNavigation = remember(navController) {
        AppNavigation(
            navController = navController,
            isNavigationLocked = { navigationLocked },
            lockNavigation = { navigationLocked = true },
            onIrMateriaDocenteView = {
                asistenciaController.limpiar()
                asistenciaDetalleController.limpiar()
                inscritosController.limpiar()
            },
            onIrMateriaEstudianteView = {
                asistenciaController.limpiar()
                asistenciaDetalleController.limpiar()
                inscritosController.limpiar()
            },
            onIrLoginView = {
                asistenciaController.limpiar()
                asistenciaDetalleController.limpiar()
                inscritosController.limpiar()
            },
            onIrAsistenciaView = { materia ->
                asistenciaController.seleccionarMateria(materia)
            }
        )
    }

    val docenteController = remember(container, appNavigation) {
        MateriaDocenteController(
            materiaModel = container.materiaModel,
            navigator = appNavigation
        )
    }

    val materiaEstudianteController = remember(container, appNavigation) {
        MateriaEstudianteController(
            materiaModel = container.materiaModel,
            estudianteModel = container.estudianteModel,
            inscritoModel = container.inscritoModel,
            navigator = appNavigation
        )
    }

    val loginController = remember(container, appNavigation, asistenciaController, asistenciaDetalleController, inscritosController) {
        LoginController(
            docenteModel = container.docenteModel,
            estudianteModel = container.estudianteModel,
            materiaModel = container.materiaModel,
            navigator = appNavigation
        )
    }

    AttendanceTheme {
        NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {

            composable(AppRoutes.LOGIN) {
                LoginView(onIniciarSesion = loginController::iniciarSesion)
            }

            composable(AppRoutes.DOCENTE_HOME) {
                MateriaDocenteView(
                    model = materiaModel,
                    onLogout = docenteController::cerrarSesion,
                    onMateriaClick = docenteController::seleccionarMateria,
                    onCrearMateria = docenteController::crearMateria
                )
            }

            composable(AppRoutes.ASISTENCIA) {
                val materia by asistenciaModel.materiaSeleccionada.collectAsState()

                if (materia == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                val asistenciaView = remember(navController, inscritosController, asistenciaDetalleController) {
                    object : IAsistenciaView {
                        override fun irVolver() {
                            navController.popBackStack()
                        }

                        override fun irInscritos(materia: com.example.attendance.model.MateriaModel) {
                            inscritosController.seleccionarMateria(materia)
                            appNavigation.navigateSafely(AppRoutes.INSCRITOS)
                        }

                        override fun irDetalle(asistenciaId: Long) {
                            asistenciaDetalleController.seleccionarAsistencia(asistenciaId)
                            appNavigation.navigateSafely(AppRoutes.ASISTENCIA_DETALLE)
                        }

                        override fun irNuevaAsistencia(materiaId: Long) {
                            asistenciaDetalleController.prepararNuevaAsistencia(materiaId)
                            appNavigation.navigateSafely(AppRoutes.ASISTENCIA_DETALLE)
                        }
                    }
                }

                DisposableEffect(asistenciaView) {
                    asistenciaController.setView(asistenciaView)
                    onDispose { asistenciaController.setView(EmptyAsistenciaView) }
                }

                AsistenciaView(
                    model = asistenciaModel,
                    onVolver = asistenciaController::volver,
                    onAbrirInscritos = asistenciaController::abrirInscritos,
                    onIniciarAsistencia = asistenciaController::abrirNuevaAsistencia,
                    onAbrirDetalle = asistenciaController::abrirDetalle,
                    onGenerarQrPayload = asistenciaController::generarPayloadQrMateria,
                )
            }

            composable(AppRoutes.INSCRITOS) {
                val materia by inscritoModel.materiaSeleccionada.collectAsState()

                if (materia == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                val inscritosView = remember(navController) {
                    object : IInscritosView {
                        override fun irVolver() {
                            navController.popBackStack()
                        }
                    }
                }

                DisposableEffect(inscritosView) {
                    inscritosController.setView(inscritosView)
                    onDispose { inscritosController.setView(EmptyInscritosView) }
                }

                InscritosView(
                    model = inscritoModel,
                    onVolver = inscritosController::volver,
                    onAgregarEstudiante = inscritosController::agregarEstudiante,
                    onImportarCsv = inscritosController::importarDesdeCsv,
                )
            }

            composable(AppRoutes.ASISTENCIA_DETALLE) {
                val asistenciaId by detalleAsistenciaModel.asistenciaSeleccionadaId.collectAsState()
                val materiaActiva by asistenciaModel.materiaSeleccionada.collectAsState()
                val bleActivo by asistenciaDetalleController.bleActivo.collectAsState()
                val bleEstado by asistenciaDetalleController.bleEstado.collectAsState()
                val esNueva by detalleAsistenciaModel.esNuevaAsistencia.collectAsState()

                if (asistenciaId == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }

                val asistenciaDetalleView = remember(navController) {
                    object : IAsistenciaDetalleView {
                        override fun irVolver() {
                            navController.popBackStack()
                        }
                    }
                }

                DisposableEffect(asistenciaDetalleView) {
                    asistenciaDetalleController.setView(asistenciaDetalleView)
                    onDispose {
                        asistenciaDetalleController.detenerBleDocente()
                        asistenciaDetalleController.setView(EmptyAsistenciaDetalleView)
                    }
                }

                AsistenciaDetalleView(
                    model = detalleAsistenciaModel,
                    materiaSigla = materiaActiva?.sigla.orEmpty(),
                    materiaGrupo = materiaActiva?.grupo.orEmpty(),
                    bleActivo = bleActivo,
                    bleEstado = bleEstado,
                    esNuevaAsistencia = esNueva,
                    onVolver = asistenciaDetalleController::volver,
                    onAlternarEstado = asistenciaDetalleController::alternarEstado,
                    onIniciarBle = {
                        asistenciaDetalleController.iniciarBleDocente(
                            sigla = materiaActiva?.sigla.orEmpty(),
                            grupo = materiaActiva?.grupo.orEmpty()
                        )
                    },
                    onDetenerBle = asistenciaDetalleController::detenerBleDocente,
                    onGuardarAsistencia = {
                        val guardado = asistenciaDetalleController.guardarAsistencia()
                        if (guardado) {
                            navController.popBackStack()
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
                        materiaEstudianteController.detenerBleEstudiante()
                    }
                }

                MateriaEstudianteView(
                    model = materiaModel,
                    bleEstado = bleEstado,
                    bleActivoMateriaId = bleActivoMateriaId,
                    bleConfirmacion = bleConfirmacion,
                    onLogout = materiaEstudianteController::cerrarSesion,
                    onRegistrarQr = materiaEstudianteController::registrarMateriaDesdeQr,
                    onIniciarBleMateria = materiaEstudianteController::iniciarBleEstudiante,
                    onDetenerBle = materiaEstudianteController::detenerBleEstudiante,
                    onCerrarConfirmacionBle = materiaEstudianteController::cerrarCardConfirmacionBle,
                )
            }
        }
    }
}
