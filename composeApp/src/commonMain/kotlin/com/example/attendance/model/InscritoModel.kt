package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InscritoModel(
    val id: Long = 0,
    val materiaId: Long = 0,
    val estudianteId: Long = 0,
    val bitMapIndex: Int = 0,
    private val db: AttendanceDatabase? = null
) {
    private fun requireDb(): AttendanceDatabase = db ?: error("InscritoModel sin db")
    private val _materiaSeleccionada = MutableStateFlow<MateriaModel?>(null)
    val materiaSeleccionada: StateFlow<MateriaModel?> = _materiaSeleccionada
    private val _inscritosMateria = MutableStateFlow<List<EstudianteModel>>(emptyList())
    val inscritosMateria: StateFlow<List<EstudianteModel>> = _inscritosMateria

    fun setMateriaSeleccionada(materia: MateriaModel?) {
        _materiaSeleccionada.value = materia
    }

    fun cargarInscritosMateria(materiaId: Long) {
        val database = requireDb()
        _inscritosMateria.value = database.inscritoQueries.getAlumnosByMateria(materiaId)
            .executeAsList()
            .map {
                EstudianteModel(
                    id = it.id,
                    carnetIdentidad = it.carnet_identidad.toInt(),
                    nombre = it.nombre,
                    apellido = it.apellido
                )
            }
    }

    fun obtenerPorMateria(materiaId: Long): List<InscritoModel> {
        val database = requireDb()
        return database.inscritoQueries.getInscritosByMateria(materiaId)
            .executeAsList()
            .map {
                InscritoModel(
                    id = it.id,
                    materiaId = it.materia_id,
                    estudianteId = it.estudiante_id,
                    bitMapIndex = it.bitmap_index.toInt(),
                )
            }
    }

    fun construirPayloadQrMateria(materia: MateriaModel): String {
        val header = listOf(materia.nombre, materia.sigla, materia.grupo, materia.periodo)
            .joinToString("|")

        val inscritos = obtenerPorMateria(materia.id)
            .mapNotNull { inscrito ->
                val estudiante = requireDb().estudianteQueries.getEstudianteById(inscrito.estudianteId)
                    .executeAsOneOrNull()
                if (estudiante == null) null else "${estudiante.carnet_identidad}|${inscrito.bitMapIndex}"
            }
            .joinToString(";")

        return if (inscritos.isBlank()) header else "$header;$inscritos"
    }

    fun limpiarEstadoMateria() {
        _materiaSeleccionada.value = null
        _inscritosMateria.value = emptyList()
    }

    fun insertar(inscrito: InscritoModel) {
        val database = requireDb()
        val nextIndex = database.inscritoQueries.getNextBitmapIndexByMateria(inscrito.materiaId)
            .executeAsOne()
            .toInt()
        database.inscritoQueries.insertInscrito(
            materia_id = inscrito.materiaId,
            estudiante_id = inscrito.estudianteId,
            bitmap_index = nextIndex.toLong()
        )
    }

    fun guardarInscripcionConBitmap(materiaId: Long, estudianteId: Long, bitmapIndex: Int) {
        val database = requireDb()
        val existente = database.inscritoQueries.getInscritoByMateriaEstudiante(materiaId, estudianteId)
            .executeAsOneOrNull()

        if (existente == null) {
            database.inscritoQueries.insertInscritoConBitmap(
                materia_id = materiaId,
                estudiante_id = estudianteId,
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

    fun obtenerPorId(id: Long): InscritoModel? {
        val database = requireDb()
        return database.inscritoQueries.getInscritoById(id)
            .executeAsOneOrNull()
            ?.let {
                InscritoModel(
                    id = it.id,
                    materiaId = it.materia_id,
                    estudianteId = it.estudiante_id,
                    bitMapIndex = it.bitmap_index.toInt()
                )
            }
    }

    fun obtenerTodos(): List<InscritoModel> {
        val database = requireDb()
        return database.inscritoQueries.getAllInscritos()
            .executeAsList()
            .map {
                InscritoModel(
                    id = it.id,
                    materiaId = it.materia_id,
                    estudianteId = it.estudiante_id,
                    bitMapIndex = it.bitmap_index.toInt()
                )
            }
    }

    fun eliminar(id: Long) {
        val database = requireDb()
        database.inscritoQueries.deleteInscrito(id)
    }

    fun eliminarPorMateriaEstudiante(materiaId: Long, estudianteId: Long) {
        val database = requireDb()
        database.inscritoQueries.deleteInscritoByMateriaEstudiante(
            materia_id = materiaId,
            estudiante_id = estudianteId
        )
    }
}
