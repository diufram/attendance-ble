package com.example.attendance.controller

import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation

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
        val materia = materiaModel.materiasDocente.value.firstOrNull { it.id == materiaId } ?: return
        navigator.irInscritosView(materia)
    }

    fun abrirNuevaAsistencia(materiaId: Long) {
        navigator.irNuevaAsistenciaView(materiaId)
    }

    fun abrirDetalle(materiaId: Long, asistenciaId: Long) {
        navigator.irAsistenciaDetalleView(materiaId, asistenciaId)
    }

    fun generarPayloadQrMateria(materiaId: Long): String? {
        val materia = materiaModel.materiasDocente.value.firstOrNull { it.id == materiaId } ?: return null
        val docente = materiaModel.docenteActual.value
        return inscritoModel.construirPayloadQrMateria(materia, docente)
    }
}
