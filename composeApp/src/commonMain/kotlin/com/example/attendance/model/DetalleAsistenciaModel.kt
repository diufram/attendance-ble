package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DetalleAsistenciaModel(
    val id: Long = 0,
    val asistenciaId: Long = 0,
    val estudianteId: Long = 0,
    val estado: String = "FALTA",
    val carnetEstudiante: Int = 0,
    val nombreEstudiante: String = "",
    val apellidoEstudiante: String = "",
    val bitmapIndexEstudiante: Int? = null,
    private val db: AttendanceDatabase? = null
) {
    private fun requireDb(): AttendanceDatabase = db ?: error("DetalleAsistenciaModel sin db")
    private val _asistenciaSeleccionadaId = MutableStateFlow<Long?>(null)
    val asistenciaSeleccionadaId: StateFlow<Long?> = _asistenciaSeleccionadaId
    private val _detallesAsistencia = MutableStateFlow<List<DetalleAsistenciaModel>>(emptyList())
    val detallesAsistencia: StateFlow<List<DetalleAsistenciaModel>> = _detallesAsistencia
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

    fun cargarDetallesTemporales(estudiantes: List<DetalleAsistenciaModel>) {
        _detallesAsistencia.value = estudiantes
    }

    fun limpiarEstadoAsistencia() {
        _asistenciaSeleccionadaId.value = null
        _detallesAsistencia.value = emptyList()
        _esNuevaAsistencia.value = false
    }

    fun insertar(detalle: DetalleAsistenciaModel) {
        val database = requireDb()

        try {
            database.detalleAsistenciaQueries.insertDetalle(
                asistencia_id = detalle.asistenciaId,
                estudiante_id = detalle.estudianteId,
                estado = detalle.estado
            )
        } catch (e: Exception) {
            throw e
        }
    }

    fun obtenerPorId(id: Long): DetalleAsistenciaModel? {
        val database = requireDb()
        return database.detalleAsistenciaQueries.getDetalleById(id)
            .executeAsOneOrNull()
            ?.let {
                DetalleAsistenciaModel(
                    id = it.id,
                    asistenciaId = it.asistencia_id,
                    estudianteId = it.estudiante_id,
                    estado = it.estado
                )
            }
    }

    fun obtenerPorAsistencia(asistenciaId: Long): List<DetalleAsistenciaModel> {
        val database = requireDb()

        val resultados = database.detalleAsistenciaQueries.getDetalleByAsistencia(asistenciaId)
            .executeAsList()

        return resultados.map {
            DetalleAsistenciaModel(
                id = it.id,
                asistenciaId = it.asistencia_id,
                estudianteId = it.estudiante_id,
                estado = it.estado,
                carnetEstudiante = it.carnet_identidad.toInt(),
                nombreEstudiante = it.nombre,
                apellidoEstudiante = it.apellido,
                bitmapIndexEstudiante = it.bitmap_index?.toInt()
            )
        }
    }

    fun actualizarEstado(asistenciaId: Long, estudianteId: Long, estado: String) {
        val database = requireDb()
        database.detalleAsistenciaQueries.updateEstado(
            estado = estado,
            asistencia_id = asistenciaId,
            estudiante_id = estudianteId
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
