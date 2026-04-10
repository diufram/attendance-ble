package com.example.attendance.controller

import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation
import com.example.attendance.util.QrUtils

class AsistenciaController(
    private val docenteModel: DocenteModel,
    private val inscritoModel: InscritoModel,
    private val materiaModel: MateriaModel,
    private val navigator: AppNavigation,
) {
    fun volver() {
        navigator.volver()
    }

    fun abrirInscritos(materia: MateriaModel) {
        navigator.irInscritosView(materia)
    }

    fun abrirNuevaAsistencia(materia: MateriaModel) {
        navigator.irNuevaAsistenciaView(materia.id)
    }

    fun abrirDetalle(materia: MateriaModel, asistencia: AsistenciaModel) {
        navigator.irAsistenciaDetalleView(materia.id, asistencia.id)
    }

    fun generarQr(materia: MateriaModel): String {
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
