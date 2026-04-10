package com.example.attendance.controller

import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation
import com.example.attendance.util.QrUtils

class AsistenciaController(
    private val asistenciaModel: AsistenciaModel,
    private val docenteModel: DocenteModel,
    private val inscritoModel: InscritoModel,
    private val materiaModel: MateriaModel,
    private val navigator: AppNavigation,
) {
    fun irInscritos(materiaId: Long) {
        navigator.irInscritosView(materiaId)
    }

    fun irCrearAsistencia(materiaId: Long) {
        navigator.irNuevaAsistenciaView(materiaId)
    }

    fun abrirDetalle(materiaId: Long, asistenciaId: Long) {
        navigator.irAsistenciaDetalleView(materiaId, asistenciaId)
    }

    fun eliminar(materiaId: Long, asistenciaId: Long): Boolean {
        val eliminado = asistenciaModel.eliminar(
            AsistenciaModel(
                id = asistenciaId,
                materiaId = materiaId,
            )
        )
        if (!eliminado) return false
        asistenciaModel.cargarAsistenciasMateria(materiaId)
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
    fun volver() {
        navigator.volver()
    }
}
