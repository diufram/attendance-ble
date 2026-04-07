package com.example.attendance.controller

import com.example.attendance.IAsistenciaView
import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.DetalleAsistenciaModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel

class AsistenciaController(
    private val estudianteModel: EstudianteModel,
    private val asistenciaModel: AsistenciaModel,
    private val detalleAsistenciaModel: DetalleAsistenciaModel,
    private val inscritoModel: InscritoModel,
    private var view: IAsistenciaView,
) {
    fun setView(view: IAsistenciaView) {
        this.view = view
    }

    fun seleccionarMateria(materia: MateriaModel) {
        asistenciaModel.setMateriaSeleccionada(materia)
        asistenciaModel.cargarAsistenciasMateria(materia.id)
    }

    fun iniciarAsistenciaSeleccionada(): Long? {
        val materia = asistenciaModel.materiaSeleccionada.value
        if (materia == null) return null

        val asistenciaId = asistenciaModel.insertarConFechaActual(materia.id)

        registrarDetallesIniciales(asistenciaId, materia.id)
        asistenciaModel.cargarAsistenciasMateria(materia.id)

        return asistenciaId
    }

    fun volver() {
        view.irVolver()
    }

    fun abrirInscritos() {
        val materia = asistenciaModel.materiaSeleccionada.value ?: return
        view.irInscritos(materia)
    }

    fun abrirNuevaAsistencia() {
        val materia = asistenciaModel.materiaSeleccionada.value ?: return
        view.irNuevaAsistencia(materia.id)
    }

    fun iniciarAsistenciaYAbrirDetalle() {
        val asistenciaId = iniciarAsistenciaSeleccionada() ?: return
        view.irDetalle(asistenciaId)
    }

    fun abrirDetalle(asistenciaId: Long) {
        view.irDetalle(asistenciaId)
    }

    fun generarPayloadQrMateria(): String? {
        val materia = asistenciaModel.materiaSeleccionada.value ?: return null
        return inscritoModel.construirPayloadQrMateria(materia)
    }

    fun limpiar() {
        asistenciaModel.limpiarEstadoMateria()
    }

    private fun registrarDetallesIniciales(asistenciaId: Long, materiaId: Long) {
        val alumnos = estudianteModel.obtenerPorMateria(materiaId)

        alumnos.forEach { alumno ->
            if (alumno.id == 0L) {
                return@forEach
            }

            try {
                detalleAsistenciaModel.insertar(
                    DetalleAsistenciaModel(
                        asistenciaId = asistenciaId,
                        estudianteId = alumno.id,
                        estado = "FALTA"
                    )
                )
            } catch (e: Exception) {
                throw e
            }
        }
    }
}
