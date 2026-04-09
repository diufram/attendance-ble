package com.example.attendance.controller

import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation
import com.example.attendance.util.QrUtils

class AsistenciaController(
    private val asistenciaModel: AsistenciaModel,
    private val inscritoModel: InscritoModel,
    private val materiaModel: MateriaModel,
    private val navigator: AppNavigation,
) {
    fun volver() {
        navigator.volver()
    }

    fun abrirInscritos(materiaId: Long) {
        val materia = materiaModel.materiasUsuario.value.firstOrNull { it.id == materiaId } ?: return
        navigator.irInscritosView(materia)
    }

    fun abrirNuevaAsistencia(materiaId: Long) {
        navigator.irNuevaAsistenciaView(materiaId)
    }

    fun abrirDetalle(materiaId: Long, asistenciaId: Long) {
        navigator.irAsistenciaDetalleView(materiaId, asistenciaId)
    }

    fun generarQr(materiaId: Long): String? {
        val materia = materiaModel.materiasUsuario.value.firstOrNull { it.id == materiaId } ?: return null
        val docente = materiaModel.docenteActual.value
        val inscritos = inscritoModel.obtenerPorMateria(materiaId)
            .map { it.carnetIdentidad to it.bitMapIndex }
        return QrUtils.construirPayloadQrMateria(materia, docente, inscritos)
    }
}