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
    private val _asistenciasMateria = MutableStateFlow<List<AsistenciaModel>>(emptyList())
    val asistenciasMateria: StateFlow<List<AsistenciaModel>> = _asistenciasMateria

    fun cargarAsistenciasMateria(materiaId: Long) {
        _asistenciasMateria.value = listar(materiaId)
    }

    fun crear(asistencia: AsistenciaModel): Long {
        val database = requireDb()
        database.asistenciaQueries.insertAsistencia(
            materia_id = asistencia.materiaId,
            fecha = asistencia.fecha
        )
        return database.asistenciaQueries.getLastInsertId().executeAsOne()
    }

    fun insertarConFechaActual(materiaId: Long): Long {
        val database = requireDb()
        return try {
            database.asistenciaQueries.transactionWithResult {
                database.asistenciaQueries.insertAsistenciaNow(materia_id = materiaId)
                val id = database.asistenciaQueries.getLastInsertId().executeAsOne()
                if (id == 0L) {
                    throw IllegalStateException("El INSERT no genero un ID valido")
                }
                id
            }
        } catch (e: Exception) {
            throw e
        }
    }


    fun listar(materiaId: Long): List<AsistenciaModel> {
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
}
