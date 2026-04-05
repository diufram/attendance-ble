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

interface IAsistenciaView {
    fun irVolver()
    fun irInscritos(materia: com.example.attendance.model.MateriaModel)
    fun irDetalle(asistenciaId: Long)
}

interface IInscritosView {
    fun irVolver()
}

interface IAsistenciaDetalleView {
    fun irVolver()
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

object EmptyAsistenciaView : IAsistenciaView {
    override fun irVolver() = Unit
    override fun irInscritos(materia: com.example.attendance.model.MateriaModel) = Unit
    override fun irDetalle(asistenciaId: Long) = Unit
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

    fun navigateAndClearStack(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    AttendanceTheme {
        NavHost(navController = navController, startDestination = "login") {

            composable("login") {
                val loginView = remember(navController, asistenciaController, asistenciaDetalleController, inscritosController) {
                    object : ILoginView {
                        override fun onMateriaDocenteView(carnet: Int) {
                            asistenciaController.limpiar()
                            asistenciaDetalleController.limpiar()
                            inscritosController.limpiar()
                            navigateAndClearStack("docente_home")
                        }

                        override fun onMateriaEstudianteView(carnet: Int) {
                            asistenciaController.limpiar()
                            asistenciaDetalleController.limpiar()
                            inscritosController.limpiar()
                            navigateAndClearStack("estudiante_home")
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
                            navigateAndClearStack("login")
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
                            navController.navigate("inscritos")
                        }

                        override fun irDetalle(asistenciaId: Long) {
                            asistenciaDetalleController.seleccionarAsistencia(asistenciaId)
                            navController.navigate("asistencia_detalle")
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
                    onIniciarAsistencia = asistenciaController::iniciarAsistenciaYAbrirDetalle,
                    onAbrirDetalle = asistenciaController::abrirDetalle,
                    onGenerarQrPayload = asistenciaController::generarPayloadQrMateria,
                )
            }

            composable("inscritos") {
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

            composable("asistencia_detalle") {
                val asistenciaId by detalleAsistenciaModel.asistenciaSeleccionadaId.collectAsState()

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
                    onDispose { asistenciaDetalleController.setView(EmptyAsistenciaDetalleView) }
                }

                AsistenciaDetalleView(
                    model = detalleAsistenciaModel,
                    onVolver = asistenciaDetalleController::volver,
                    onAlternarEstado = asistenciaDetalleController::alternarEstado,
                )
            }

            composable("estudiante_home") {
                val estudianteView = remember(navController, asistenciaController, asistenciaDetalleController, inscritosController) {
                    object : IMateriaEstudianteView {
                        override fun irLogin() {
                            asistenciaController.limpiar()
                            asistenciaDetalleController.limpiar()
                            inscritosController.limpiar()
                            navigateAndClearStack("login")
                        }
                    }
                }

                DisposableEffect(estudianteView) {
                    materiaEstudianteController.setView(estudianteView)
                    onDispose { materiaEstudianteController.setView(EmptyMateriaEstudianteView) }
                }

                MateriaEstudianteView(
                    model = materiaModel,
                    onLogout = materiaEstudianteController::cerrarSesion,
                    onRegistrarQr = materiaEstudianteController::registrarMateriaDesdeQr
                )
            }
        }
    }
}
