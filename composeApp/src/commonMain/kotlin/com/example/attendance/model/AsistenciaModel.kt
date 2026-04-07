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
        println("[AsistenciaModel.insertarConFechaActual] Insertando asistencia para materia: $materiaId")
        val database = requireDb()

        return try {
            database.asistenciaQueries.transactionWithResult {
                println("[AsistenciaModel.insertarConFechaActual] Iniciando transacción...")

                database.asistenciaQueries.insertAsistenciaNow(materia_id = materiaId)
                println("[AsistenciaModel.insertarConFechaActual] INSERT ejecutado")

                val id = database.asistenciaQueries.getLastInsertId().executeAsOne()
                println("[AsistenciaModel.insertarConFechaActual] ID retornado: $id")

                if (id == 0L) {
                    println("[AsistenciaModel.insertarConFechaActual] ERROR: ID retornado es 0")
                    throw IllegalStateException("El INSERT no generó un ID válido")
                }

                println("[AsistenciaModel.insertarConFechaActual] Transacción completada")
                id
            }
        } catch (e: Exception) {
            println("[AsistenciaModel.insertarConFechaActual] ERROR: ${e.message}")
            e.printStackTrace()
            throw e
        }
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
