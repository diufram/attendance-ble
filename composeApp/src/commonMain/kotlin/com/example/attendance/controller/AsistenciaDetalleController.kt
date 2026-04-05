package com.example.attendance.controller

import com.example.attendance.model.DetalleAsistenciaModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AsistenciaDetalleController(
    private val detalleAsistenciaModel: DetalleAsistenciaModel
) {
    sealed class NavigationEvent {
        data object Volver : NavigationEvent()
    }

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun seleccionarAsistencia(asistenciaId: Long) {
        detalleAsistenciaModel.setAsistenciaSeleccionada(asistenciaId)
        detalleAsistenciaModel.cargarDetallesAsistencia(asistenciaId)
    }

    fun alternarEstado(estudianteId: Long, estadoActual: String) {
        val asistenciaId = detalleAsistenciaModel.asistenciaSeleccionadaId.value ?: return
        val nuevoEstado = if (estadoActual == "PRESENTE") "FALTA" else "PRESENTE"
        detalleAsistenciaModel.actualizarEstado(asistenciaId, estudianteId, nuevoEstado)
        detalleAsistenciaModel.cargarDetallesAsistencia(asistenciaId)
    }

    fun limpiar() {
        detalleAsistenciaModel.limpiarEstadoAsistencia()
    }

    fun solicitarVolver() {
        _navigationEvent.value = NavigationEvent.Volver
    }

    fun limpiarNavegacion() {
        _navigationEvent.value = null
    }
}
