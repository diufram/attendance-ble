package com.example.attendance.controller

import com.example.attendance.ble.BleStudentService
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation
import com.example.attendance.util.QrUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MateriaEstudianteController(
    private val materiaModel: MateriaModel,
    private val docenteModel: DocenteModel,
    private val inscritoModel: InscritoModel,
    private val navigator: AppNavigation,
) {
    data class BleConfirmacionUi(
        val nombreMateria: String,
        val sigla: String,
        val grupo: String,
    )

    private val bleService = BleStudentService()

    private val _bleEstado = MutableStateFlow("BLE inactivo")
    val bleEstado: StateFlow<String> = _bleEstado.asStateFlow()

    private val _bleActivoMateriaId = MutableStateFlow<Long?>(null)
    val bleActivoMateriaId: StateFlow<Long?> = _bleActivoMateriaId.asStateFlow()

    private val _bleConfirmacion = MutableStateFlow<BleConfirmacionUi?>(null)
    val bleConfirmacion: StateFlow<BleConfirmacionUi?> = _bleConfirmacion.asStateFlow()

    fun cerrarSesion() {
        detenerMarcadoAsistencia()
        materiaModel.limpiarMaterias()
        navigator.irLoginView()
    }

    fun marcarAsistencia(materia: MateriaModel): String? {
        val bitmapIndex = materia.bitmapIndexEstudiante
            ?: return "Esta materia no tiene bitmap index asignado"

        bleService.iniciarMarcadoAsistencia(
            nombreMateria = materia.nombre,
            sigla = materia.sigla,
            grupo = materia.grupo,
            bitmapIndex = bitmapIndex,
        )

        _bleActivoMateriaId.value = materia.id
        _bleEstado.value = "Escuchando confirmacion BLE..."

        return null
    }

    fun detenerMarcadoAsistencia() {
        bleService.detenerMarcadoAsistencia()
        _bleActivoMateriaId.value = null
        _bleEstado.value = "BLE inactivo"
    }

    fun cerrarConfirmacionAsistencia() {
        _bleConfirmacion.value = null
        if (_bleActivoMateriaId.value == null) {
            _bleEstado.value = "BLE inactivo"
        }
    }

    fun registrarMateriaDesdeQr(payload: String): String? {
        val carnet = materiaModel.usuarioCarnet.value
            ?: return "No hay estudiante activo"

        val qr = QrUtils.parsearQrMateria(payload) ?: return "QR invalido"
        val bitmapIndex = qr.bitmapIndexPorCarnet[carnet]
            ?: return "Tu carnet no esta habilitado en este QR"

        val docenteCarnetInt = qr.docenteCarnet.toIntOrNull()
        val docenteCarnet = if (docenteCarnetInt != null) {
            var docente = docenteModel.obtenerPorCarnet(docenteCarnetInt)
            if (docente == null) {
                docenteModel.insertar(
                    DocenteModel(
                        carnetIdentidad = docenteCarnetInt.toLong(),
                        nombre = qr.docenteNombre.ifBlank { "" },
                        apellido = qr.docenteApellido.ifBlank { "" }
                    )
                )
                docente = docenteModel.obtenerPorCarnet(docenteCarnetInt)
            }
            docente?.carnetIdentidad?.toLong()
        } else null

        val materia = materiaModel.obtenerPorFormacion(
            sigla = qr.sigla,
            grupo = qr.grupo,
            periodo = qr.periodo
        ) ?: run {
            materiaModel.insertar(
                MateriaModel(
                    sigla = qr.sigla,
                    nombre = qr.nombre,
                    grupo = qr.grupo,
                    periodo = qr.periodo,
                    docenteCarnet = docenteCarnet
                )
            )
            materiaModel.obtenerPorFormacion(
                sigla = qr.sigla,
                grupo = qr.grupo,
                periodo = qr.periodo
            )
        } ?: return "No se pudo registrar la materia"

        return try {
            inscritoModel.guardarInscripcionConBitmap(
                materiaId = materia.id,
                carnetIdentidad = carnet.toLong(),
                bitmapIndex = bitmapIndex
            )
            materiaModel.cargarMateriasUsuario(carnet, esDocente = false, docente = null)
            null
        } catch (_: Throwable) {
            "No se pudo guardar la inscripcion desde el QR"
        }
    }
}