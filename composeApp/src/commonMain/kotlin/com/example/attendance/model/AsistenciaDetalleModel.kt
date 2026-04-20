package com.example.attendance.model

import com.example.attendance.db.Database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AsistenciaDetalleModel(
    val id: Long = 0,
    val asistenciaId: Long = 0,
    val carnetIdentidad: Long = 0,
    val estado: String = "FALTA",
    val nombreEstudiante: String = "",
    val apellidoEstudiante: String = "",
    val bitmapIndexEstudiante: Int? = null,
    private val db: Database? = null
) {
    private fun requireDb(): Database = db ?: error("DetalleAsistenciaModel sin db")
    
    private val _detallesAsistencia = MutableStateFlow<List<AsistenciaDetalleModel>>(emptyList())
    val detallesAsistencia: StateFlow<List<AsistenciaDetalleModel>> = _detallesAsistencia

    fun cargarDetallesAsistencia(asistenciaId: Long) {
        val detalles = obtenerPorAsistencia(asistenciaId)
        _detallesAsistencia.value = detalles
    }

    fun crear(detalle: AsistenciaDetalleModel) {
        val database = requireDb()

        try {
            database.detalleAsistenciaQueries.insertDetalle(
                asistencia_id = detalle.asistenciaId,
                carnet_identidad = detalle.carnetIdentidad,
                estado = detalle.estado
            )
        } catch (e: Exception) {
            throw e
        }
    }
    
    fun obtenerPorAsistencia(asistenciaId: Long): List<AsistenciaDetalleModel> {
        val database = requireDb()

        val resultados = database.detalleAsistenciaQueries.getDetalleByAsistencia(asistenciaId)
            .executeAsList()

        return resultados.map {
            AsistenciaDetalleModel(
                id = it.id,
                asistenciaId = it.asistencia_id,
                carnetIdentidad = it.carnet_identidad,
                estado = it.estado,
                nombreEstudiante = it.nombre,
                apellidoEstudiante = it.apellido,
                bitmapIndexEstudiante = it.bitmap_index?.toInt()
            )
        }
    }

    fun actualizarEstado(asistenciaId: Long, carnetIdentidad: Long, estado: String) {
        val database = requireDb()
        database.detalleAsistenciaQueries.updateEstado(
            estado = estado,
            asistencia_id = asistenciaId,
            carnet_identidad = carnetIdentidad
        )
    }
}
