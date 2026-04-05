package com.example.attendance.controller

import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.DetalleAsistenciaModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.MateriaModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AsistenciaController(
    private val estudianteModel: EstudianteModel,
    private val asistenciaModel: AsistenciaModel,
    private val detalleAsistenciaModel: DetalleAsistenciaModel
) {
    sealed class NavigationEvent {
        data object Volver : NavigationEvent()
        data class IrInscritos(val materia: MateriaModel) : NavigationEvent()
        data class IrDetalle(val asistenciaId: Long) : NavigationEvent()
    }

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun seleccionarMateria(materia: MateriaModel) {
        asistenciaModel.setMateriaSeleccionada(materia)
        asistenciaModel.cargarAsistenciasMateria(materia.id)
    }

    fun iniciarAsistenciaSeleccionada(): Long? {
        val materia = asistenciaModel.materiaSeleccionada.value ?: return null
        val asistenciaId = asistenciaModel.insertarConFechaActual(materia.id)
        registrarDetallesIniciales(asistenciaId, materia.id)
        asistenciaModel.cargarAsistenciasMateria(materia.id)
        return asistenciaId
    }

    fun solicitarVolver() {
        _navigationEvent.value = NavigationEvent.Volver
    }

    fun abrirInscritos() {
        val materia = asistenciaModel.materiaSeleccionada.value ?: return
        _navigationEvent.value = NavigationEvent.IrInscritos(materia)
    }

    fun iniciarAsistenciaYAbrirDetalle() {
        val asistenciaId = iniciarAsistenciaSeleccionada() ?: return
        _navigationEvent.value = NavigationEvent.IrDetalle(asistenciaId)
    }

    fun abrirDetalle(asistenciaId: Long) {
        _navigationEvent.value = NavigationEvent.IrDetalle(asistenciaId)
    }

    fun limpiarNavegacion() {
        _navigationEvent.value = null
    }

    fun limpiar() {
        asistenciaModel.limpiarEstadoMateria()
    }

    private fun registrarDetallesIniciales(asistenciaId: Long, materiaId: Long) {
        val alumnos = estudianteModel.obtenerPorMateria(materiaId)
        for (alumno in alumnos) {
            detalleAsistenciaModel.insertar(
                DetalleAsistenciaModel(
                    asistenciaId = asistenciaId,
                    estudianteId = alumno.id,
                    estado = "FALTA"
                )
            )
        }
    }
}
