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
        println("[InscritoModel.cargarInscritosMateria] Cargando inscritos para materia: $materiaId")
        val database = requireDb()

        // Verificar cuántos estudiantes hay en total
        val totalEstudiantes = database.estudianteQueries.getAllEstudiantes().executeAsList().size
        println("[InscritoModel.cargarInscritosMateria] Total estudiantes en BD: $totalEstudiantes")

        // Verificar cuántas inscripciones hay para esta materia
        val inscripciones = database.inscritoQueries.getInscritosByMateria(materiaId).executeAsList()
        println("[InscritoModel.cargarInscritosMateria] Total inscripciones para materia $materiaId: ${inscripciones.size}")
        inscripciones.forEachIndexed { i, insc ->
            println("  Inscripción[$i]: id=${insc.id}, estudiante_id=${insc.estudiante_id}, bitmap_index=${insc.bitmap_index}")
        }

        // Ahora ejecutar la query con JOIN
        val lista = database.inscritoQueries.getAlumnosByMateria(materiaId)
            .executeAsList()
            .map {
                EstudianteModel(
                    id = it.id,
                    carnetIdentidad = it.carnet_identidad.toInt(),
                    nombre = it.nombre,
                    apellido = it.apellido
                )
            }
        println("[InscritoModel.cargarInscritosMateria] Resultado JOIN (inscritos cargados): ${lista.size} estudiantes")
        lista.forEachIndexed { index, est ->
            println("  [$index] ID:${est.id}, Carnet:${est.carnetIdentidad}, Nombre:'${est.nombre} ${est.apellido}'")
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
        println("[InscritoModel.insertar] Iniciando inserción de inscripción...")
        println("[InscritoModel.insertar] materiaId=${inscrito.materiaId}, estudianteId=${inscrito.estudianteId}")

        val database = requireDb()

        println("[InscritoModel.insertar] Verificando si ya existe inscripción...")
        val existente = database.inscritoQueries.getInscritoByMateriaEstudiante(
            materia_id = inscrito.materiaId,
            estudiante_id = inscrito.estudianteId
        ).executeAsOneOrNull()

        if (existente != null) {
            println("[InscritoModel.insertar] Ya existe inscripción, ID: ${existente.id}, bitmap_index: ${existente.bitmap_index}")
            return
        }
        println("[InscritoModel.insertar] No existe inscripción previa")

        println("[InscritoModel.insertar] Obteniendo siguiente bitmap_index...")
        val nextIndex = database.inscritoQueries.getNextBitmapIndexByMateria(inscrito.materiaId)
            .executeAsOne()
            .toInt()
        println("[InscritoModel.insertar] Siguiente bitmap_index: $nextIndex")

        try {
            database.inscritoQueries.transaction {
                println("[InscritoModel.insertar] Iniciando transacción de inscripción...")

                database.inscritoQueries.insertInscrito(
                    materia_id = inscrito.materiaId,
                    estudiante_id = inscrito.estudianteId,
                    bitmap_index = nextIndex.toLong()
                )
                println("[InscritoModel.insertar] INSERT de inscripción ejecutado")

                // Verificar que se insertó
                val verificacion = database.inscritoQueries.getInscritoByMateriaEstudiante(
                    materia_id = inscrito.materiaId,
                    estudiante_id = inscrito.estudianteId
                ).executeAsOneOrNull()

                if (verificacion == null) {
                    println("[InscritoModel.insertar] ERROR: No se encuentra la inscripción después del INSERT")
                    throw IllegalStateException("La inscripción no se insertó correctamente")
                }
                println("[InscritoModel.insertar] Verificación: Inscripción encontrada ID=${verificacion.id}, bitmap_index=${verificacion.bitmap_index}")
                println("[InscritoModel.insertar] Inscripción insertada exitosamente con bitmap_index=$nextIndex")
            }
        } catch (e: Exception) {
            println("[InscritoModel.insertar] ERROR al insertar: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    fun existeInscripcion(materiaId: Long, estudianteId: Long): Boolean {
        val database = requireDb()
        return database.inscritoQueries.getInscritoByMateriaEstudiante(materiaId, estudianteId)
            .executeAsOneOrNull() != null
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
