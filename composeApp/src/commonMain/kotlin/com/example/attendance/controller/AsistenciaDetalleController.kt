package com.example.attendance.controller

import com.example.attendance.model.DetalleAsistencia
import com.example.attendance.model.DetalleAsistenciaModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AsistenciaDetalleController(
    private val detalleAsistenciaModel: DetalleAsistenciaModel
) {
    private val _asistenciaSeleccionadaId = MutableStateFlow<Long?>(null)
    val asistenciaSeleccionadaId: StateFlow<Long?> = _asistenciaSeleccionadaId

    private val _detalles = MutableStateFlow<List<DetalleAsistencia>>(emptyList())
    val detalles: StateFlow<List<DetalleAsistencia>> = _detalles

    fun seleccionarAsistencia(asistenciaId: Long) {
        _asistenciaSeleccionadaId.value = asistenciaId
        _detalles.value = detalleAsistenciaModel.obtenerPorAsistencia(asistenciaId)
    }

    fun alternarEstado(estudianteId: Int, estadoActual: String) {
        val asistenciaId = _asistenciaSeleccionadaId.value ?: return
        val nuevoEstado = if (estadoActual == "PRESENTE") "FALTA" else "PRESENTE"
        detalleAsistenciaModel.actualizarEstado(asistenciaId, estudianteId, nuevoEstado)
        _detalles.value = detalleAsistenciaModel.obtenerPorAsistencia(asistenciaId)
    }

    fun limpiar() {
        _asistenciaSeleccionadaId.value = null
        _detalles.value = emptyList()
    }
}
