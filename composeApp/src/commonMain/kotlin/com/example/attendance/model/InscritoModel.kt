package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InscritoModel(
    val id: Long = 0,
    val materiaId: Long = 0,
    val carnetIdentidad: Long = 0,
    val bitMapIndex: Int = 0,
    private val db: AttendanceDatabase? = null
) {
    private fun requireDb(): AttendanceDatabase = db ?: error("InscritoModel sin db")
    private val _inscritosMateria = MutableStateFlow<List<EstudianteModel>>(emptyList())
    val inscritosMateria: StateFlow<List<EstudianteModel>> = _inscritosMateria

    fun cargarInscritosMateria(materiaId: Long) {
        val database = requireDb()
        val lista = database.inscritoQueries.getAlumnosByMateria(materiaId)
            .executeAsList()
            .map {
                EstudianteModel(
                    id = it.id,
                    carnetIdentidad = it.carnet_identidad,
                    nombre = it.nombre,
                    apellido = it.apellido
                )
            }
        _inscritosMateria.value = lista
    }

    fun obtenerPorMateria(materiaId: Long): List<InscritoModel> {
        val database = requireDb()
        return database.inscritoQueries.getInscritosByMateria(materiaId)
            .executeAsList()
            .map {
                InscritoModel(
                    id = it.id,
                    materiaId = it.materia_id,
                    carnetIdentidad = it.carnet_identidad,
                    bitMapIndex = it.bitmap_index.toInt(),
                )
            }
    }

    fun insertar(inscrito: InscritoModel) {
        val database = requireDb()

        val existente = database.inscritoQueries.getInscritoByMateriaEstudiante(
            materia_id = inscrito.materiaId,
            carnet_identidad = inscrito.carnetIdentidad
        ).executeAsOneOrNull()

        if (existente != null) return

        val nextIndex = database.inscritoQueries.getNextBitmapIndexByMateria(inscrito.materiaId)
            .executeAsOne()
            .toInt()

        database.inscritoQueries.insertInscrito(
            materia_id = inscrito.materiaId,
            carnet_identidad = inscrito.carnetIdentidad,
            bitmap_index = nextIndex.toLong()
        )
    }
    fun guardarInscripcionConBitmap(materiaId: Long, carnetIdentidad: Long, bitmapIndex: Int) {
        val database = requireDb()
        val existente = database.inscritoQueries.getInscritoByMateriaEstudiante(materiaId, carnetIdentidad)
            .executeAsOneOrNull()

        if (existente == null) {
            database.inscritoQueries.insertInscritoConBitmap(
                materia_id = materiaId,
                carnet_identidad = carnetIdentidad,
                bitmap_index = bitmapIndex.toLong()
            )
            return
        }

        if (existente.bitmap_index.toInt() != bitmapIndex) {
            database.inscritoQueries.updateBitmapIndexById(
                bitmap_index = bitmapIndex.toLong(),
                id = existente.id
            )
        }
    }
}