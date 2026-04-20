package com.example.attendance.controller

import com.example.attendance.ble.BleStudentService
import com.example.attendance.ble.BleConfirmacion
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.util.QrUtils
import com.example.attendance.view.IMateriaEstudianteView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MateriaEstudianteController(
    private val materiaModel: MateriaModel,
    private val docenteModel: DocenteModel,
    private val inscritoModel: InscritoModel,
    private val view: IMateriaEstudianteView,
) {
    private val bleService = BleStudentService()

    val bleEstado: StateFlow<String> = bleService.bleEstado
    val bleActivoMateriaId: StateFlow<Long?> = bleService.bleActivoMateriaId
    val bleConfirmacion: StateFlow<BleConfirmacion?> = bleService.bleConfirmacion

    fun iniciar(carnet: Long) {
        materiaModel.cargarMaterias(carnet, esDocente = false)
        view.setMaterias(materiaModel.materiasUsuario)
    }

    fun marcarAsistencia(materia: MateriaModel): String? {
        val bitmapIndex = materia.bitmapIndexEstudiante
            ?: return "Esta materia no tiene bitmap index asignado"

        bleService.iniciarMarcadoAsistencia(
            materiaId = materia.id,
            nombreMateria = materia.nombre,
            sigla = materia.sigla,
            grupo = materia.grupo,
            bitmapIndex = bitmapIndex,
        )

        return null
    }
    fun detenerMarcadoAsistencia() {
        bleService.detenerMarcadoAsistencia()
    }
    fun cerrarConfirmacionAsistencia() {
        bleService.cerrarConfirmacionAsistencia()
    }

    fun registrarMateriaDesdeQr(carnet: Long, payload: String): String? {

        val qr = QrUtils.parsearQrMateria(payload) ?: return "QR invalido"
        val bitmapIndex = qr.bitmapIndexPorCarnet[carnet.toInt()]
            ?: return "Tu carnet no esta habilitado en este QR"

        val docenteCarnetInt = qr.docenteCarnet.toIntOrNull()
        val docenteCarnet = if (docenteCarnetInt != null) {
            var docente = docenteModel.obtenerPorCarnet(docenteCarnetInt.toLong())
            if (docente == null) {
                docenteModel.crear(
                    DocenteModel(
                        carnetIdentidad = docenteCarnetInt.toLong(),
                        nombre = qr.docenteNombre.ifBlank { "" },
                        apellido = qr.docenteApellido.ifBlank { "" }
                    )
                )
                docente = docenteModel.obtenerPorCarnet(docenteCarnetInt.toLong())
            }
            docente?.carnetIdentidad
        } else null

        val materia = materiaModel.crear(
            MateriaModel(
                sigla = qr.sigla,
                nombre = qr.nombre,
                grupo = qr.grupo,
                periodo = qr.periodo,
                docenteCarnet = docenteCarnet,
            )
        ) ?: return "No se pudo registrar la materia"

        return try {
            inscritoModel.crear(
                InscritoModel(
                    materiaId = materia.id,
                    carnetIdentidad = carnet,
                    bitMapIndex = bitmapIndex,
                )
            )
            materiaModel.cargarMaterias(carnet, esDocente = false)
            null
        } catch (_: Throwable) {
            "No se pudo guardar la inscripcion desde el QR"
        }
    }
    fun cerrarSesion() {
        detenerMarcadoAsistencia()
        materiaModel.limpiarMaterias()
        view.setMaterias(MutableStateFlow(emptyList()))
    }
}
