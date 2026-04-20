package com.example.attendance.controller

import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.util.QrUtils
import com.example.attendance.view.AsistenciaView

class AsistenciaController(
    private val asistenciaModel: AsistenciaModel,
    private val docenteModel: DocenteModel,
    private val inscritoModel: InscritoModel,
    private val materiaModel: MateriaModel,
    private val view: AsistenciaView,
) {
    fun iniciar(materiaId: Long) {
        asistenciaModel.cargarAsistenciasMateria(materiaId)
        view.setAsistencias(asistenciaModel.asistenciasMateria.value)
    }

    fun eliminar(materiaId: Long): Boolean {
        val asistenciaId = view.asistenciaAEliminar.value?.id ?: return false
        val eliminado = asistenciaModel.eliminar(
            AsistenciaModel(
                id = asistenciaId,
                materiaId = materiaId,
            )
        )
        if (!eliminado) return false
        asistenciaModel.cargarAsistenciasMateria(materiaId)
        view.setAsistencias(asistenciaModel.asistenciasMateria.value)
        return true
    }

    fun generarQr(materiaId: Long): String? {
        val materia = materiaModel.materiasUsuario.value.firstOrNull { it.id == materiaId } ?: return null
        val docenteCarnet = materia.docenteCarnet ?: materiaModel.usuarioCarnet.value?.toLong()
        val docente = docenteCarnet?.toInt()?.let { docenteModel.obtenerPorCarnet(it.toLong()) }
        val inscritos = inscritoModel.obtenerPorMateria(materia.id)
            .mapNotNull {
                val bitmap = it.bitMapIndex ?: return@mapNotNull null
                it.carnetIdentidad to bitmap
            }
        return QrUtils.construirPayloadQrMateria(materia, docente, inscritos)
    }
}
