package com.example.attendance.controller

import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.model.DetalleAsistencia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AsistenciaDetalleController(
    private val db: AttendanceDatabase
) {
    sealed class NavigationEvent {
        data object Volver : NavigationEvent()
    }

    private val _asistenciaSeleccionadaId = MutableStateFlow<Long?>(null)
    val asistenciaSeleccionadaId: StateFlow<Long?> = _asistenciaSeleccionadaId

    private val _detalles = MutableStateFlow<List<DetalleAsistencia>>(emptyList())
    val detalles: StateFlow<List<DetalleAsistencia>> = _detalles

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun seleccionarAsistencia(asistenciaId: Long) {
        _asistenciaSeleccionadaId.value = asistenciaId
        _detalles.value = DetalleAsistencia.obtenerPorAsistencia(db, asistenciaId)
    }

    fun alternarEstado(estudianteId: Long, estadoActual: String) {
        val asistenciaId = _asistenciaSeleccionadaId.value ?: return
        val nuevoEstado = if (estadoActual == "PRESENTE") "FALTA" else "PRESENTE"
        DetalleAsistencia.actualizarEstado(db, asistenciaId, estudianteId, nuevoEstado)
        _detalles.value = DetalleAsistencia.obtenerPorAsistencia(db, asistenciaId)
    }

    fun limpiar() {
        _asistenciaSeleccionadaId.value = null
        _detalles.value = emptyList()
    }

    fun solicitarVolver() {
        _navigationEvent.value = NavigationEvent.Volver
    }

    fun limpiarNavegacion() {
        _navigationEvent.value = null
    }
}
