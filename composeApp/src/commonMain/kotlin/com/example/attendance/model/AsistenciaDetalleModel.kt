package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase
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
    private val db: AttendanceDatabase? = null
) {
    private fun requireDb(): AttendanceDatabase = db ?: error("DetalleAsistenciaModel sin db")
    private val _asistenciaSeleccionadaId = MutableStateFlow<Long?>(null)
    val asistenciaSeleccionadaId: StateFlow<Long?> = _asistenciaSeleccionadaId
    private val _detallesAsistencia = MutableStateFlow<List<AsistenciaDetalleModel>>(emptyList())
    val detallesAsistencia: StateFlow<List<AsistenciaDetalleModel>> = _detallesAsistencia
    private val _esNuevaAsistencia = MutableStateFlow(false)
    val esNuevaAsistencia: StateFlow<Boolean> = _esNuevaAsistencia

    fun setAsistenciaSeleccionada(asistenciaId: Long?, esNueva: Boolean = false) {
        _asistenciaSeleccionadaId.value = asistenciaId
        _esNuevaAsistencia.value = esNueva
    }

    fun cargarDetallesAsistencia(asistenciaId: Long) {
        val detalles = obtenerPorAsistencia(asistenciaId)
        _detallesAsistencia.value = detalles
    }

    fun cargarDetallesTemporales(estudiantes: List<AsistenciaDetalleModel>) {
        _detallesAsistencia.value = estudiantes
    }

    fun limpiarEstadoAsistencia() {
        _asistenciaSeleccionadaId.value = null
        _detallesAsistencia.value = emptyList()
        _esNuevaAsistencia.value = false
    }

    fun insertar(detalle: AsistenciaDetalleModel) {
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

    fun obtenerPorId(id: Long): AsistenciaDetalleModel? {
        val database = requireDb()
        return database.detalleAsistenciaQueries.getDetalleById(id)
            .executeAsOneOrNull()
            ?.let {
                AsistenciaDetalleModel(
                    id = it.id,
                    asistenciaId = it.asistencia_id,
carnetIdentidad = it.carnet_identidad,
                    estado = it.estado
                )
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

    fun eliminar(id: Long) {
        val database = requireDb()
        database.detalleAsistenciaQueries.deleteDetalle(id)
    }

    fun eliminarPorAsistencia(asistenciaId: Long) {
        val database = requireDb()
        database.detalleAsistenciaQueries.deleteDetalleByAsistencia(asistenciaId)
    }
}