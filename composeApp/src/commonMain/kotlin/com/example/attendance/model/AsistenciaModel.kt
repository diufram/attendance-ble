package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AsistenciaModel(
    val id: Long = 0,
    val materiaId: Long = 0,
    val fecha: String = "",
    private val db: AttendanceDatabase? = null
) {
    private fun requireDb(): AttendanceDatabase = db ?: error("AsistenciaModel sin db")
    private val _materiaSeleccionada = MutableStateFlow<MateriaModel?>(null)
    val materiaSeleccionada: StateFlow<MateriaModel?> = _materiaSeleccionada
    private val _asistenciasMateria = MutableStateFlow<List<AsistenciaModel>>(emptyList())
    val asistenciasMateria: StateFlow<List<AsistenciaModel>> = _asistenciasMateria

    fun setMateriaSeleccionada(materia: MateriaModel?) {
        _materiaSeleccionada.value = materia
    }

    fun cargarAsistenciasMateria(materiaId: Long) {
        _asistenciasMateria.value = obtenerPorMateria(materiaId)
    }

    fun limpiarEstadoMateria() {
        _materiaSeleccionada.value = null
        _asistenciasMateria.value = emptyList()
    }

    fun insertar(asistencia: AsistenciaModel): Long {
        val database = requireDb()
        database.asistenciaQueries.insertAsistencia(
            materia_id = asistencia.materiaId,
            fecha = asistencia.fecha
        )
        return database.asistenciaQueries.getLastInsertId().executeAsOne()
    }

    fun insertarConFechaActual(materiaId: Long): Long {
        val database = requireDb()
        database.asistenciaQueries.insertAsistenciaNow(materia_id = materiaId)
        return database.asistenciaQueries.getLastInsertId().executeAsOne()
    }

    fun obtenerPorId(id: Long): AsistenciaModel? {
        val database = requireDb()
        return database.asistenciaQueries.getAsistenciaById(id)
            .executeAsOneOrNull()
            ?.let {
                AsistenciaModel(
                    id = it.id,
                    materiaId = it.materia_id,
                    fecha = it.fecha
                )
            }
    }

    fun obtenerTodos(): List<AsistenciaModel> {
        val database = requireDb()
        return database.asistenciaQueries.getAllAsistencias()
            .executeAsList()
            .map {
                AsistenciaModel(
                    id = it.id,
                    materiaId = it.materia_id,
                    fecha = it.fecha
                )
            }
    }

    fun obtenerPorMateria(materiaId: Long): List<AsistenciaModel> {
        val database = requireDb()
        return database.asistenciaQueries.getAsistenciasByMateria(materiaId)
            .executeAsList()
            .map {
                AsistenciaModel(
                    id = it.id,
                    materiaId = it.materia_id,
                    fecha = it.fecha
                )
            }
    }

    fun actualizar(asistencia: AsistenciaModel) {
        val database = requireDb()
        database.asistenciaQueries.updateAsistencia(
            fecha = asistencia.fecha,
            id = asistencia.id
        )
    }

    fun eliminar(id: Long) {
        val database = requireDb()
        database.detalleAsistenciaQueries.deleteDetalleByAsistencia(id)
        database.asistenciaQueries.deleteAsistencia(id)
    }
}
