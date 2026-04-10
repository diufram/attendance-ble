package com.example.attendance.model

import com.example.attendance.db.Database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AsistenciaModel(
    val id: Long = 0,
    val materiaId: Long = 0,
    val fecha: String = "",
    private val db: Database? = null
) {
    private fun requireDb(): Database = db ?: error("AsistenciaModel sin db")
    private val _asistenciasMateria = MutableStateFlow<List<AsistenciaModel>>(emptyList())
    val asistenciasMateria: StateFlow<List<AsistenciaModel>> = _asistenciasMateria

    fun cargarAsistenciasMateria(materiaId: Long) {
        _asistenciasMateria.value = listar(materiaId)
    }

    fun guardar(asistencia: AsistenciaModel): Long {
        val database = requireDb()
        if (asistencia.fecha.isBlank()) {
            database.asistenciaQueries.insertAsistenciaNow(materia_id = asistencia.materiaId)
        } else {
            database.asistenciaQueries.insertAsistencia(
                materia_id = asistencia.materiaId,
                fecha = asistencia.fecha
            )
        }
        return database.asistenciaQueries.getLastInsertId().executeAsOne()
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

    fun eliminar(asistencia: AsistenciaModel): Boolean {
        val database = requireDb()
        return runCatching {
            database.detalleAsistenciaQueries.deleteDetalleByAsistencia(asistencia.id)
            database.asistenciaQueries.deleteAsistencia(asistencia.id)
        }.isSuccess
    }
}
