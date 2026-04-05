package com.example.attendance.controller

import com.example.attendance.IAsistenciaDetalleView
import com.example.attendance.model.DetalleAsistenciaModel

class AsistenciaDetalleController(
    private val detalleAsistenciaModel: DetalleAsistenciaModel,
    private var view: IAsistenciaDetalleView,
) {
    fun setView(view: IAsistenciaDetalleView) {
        this.view = view
    }

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

    fun volver() {
        view.irVolver()
    }
}
