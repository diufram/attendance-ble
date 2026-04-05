package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MateriaModel(
    val id: Long = 0,
    val sigla: String = "",
    val nombre: String = "",
    val grupo: String = "",
    val periodo: String = "",
    val docenteNombre: String = "",
    val docenteId: Int = 0,
    private val db: AttendanceDatabase? = null
) {
    private fun requireDb(): AttendanceDatabase = db ?: error("MateriaModel sin db")
    private val _docenteActual = MutableStateFlow<DocenteModel?>(null)
    val docenteActual: StateFlow<DocenteModel?> = _docenteActual
    private val _materiasDocente = MutableStateFlow<List<MateriaModel>>(emptyList())
    val materiasDocente: StateFlow<List<MateriaModel>> = _materiasDocente
    private val _materiasEstudiante = MutableStateFlow<List<MateriaModel>>(emptyList())
    val materiasEstudiante: StateFlow<List<MateriaModel>> = _materiasEstudiante

    fun insertar(materia: MateriaModel) {
        val database = requireDb()
        database.materiaQueries.insertMateria(
            sigla = materia.sigla,
            nombre = materia.nombre,
            grupo = materia.grupo,
            periodo = materia.periodo,
            docente_nombre = materia.docenteNombre,
            docente_id = materia.docenteId.toLong()
        )
    }

    fun obtenerPorId(id: Long): MateriaModel? {
        val database = requireDb()
        return database.materiaQueries.getMateriaById(id)
            .executeAsOneOrNull()
            ?.let {
                MateriaModel(
                    id = it.id,
                    sigla = it.sigla,
                    nombre = it.nombre,
                    grupo = it.grupo,
                    periodo = it.periodo,
                    docenteNombre = it.docente_nombre,
                    docenteId = it.docente_id.toInt()
                )
            }
    }

    fun obtenerTodos(): List<MateriaModel> {
        val database = requireDb()
        return database.materiaQueries.getAllMaterias()
            .executeAsList()
            .map {
                MateriaModel(
                    id = it.id,
                    sigla = it.sigla,
                    nombre = it.nombre,
                    grupo = it.grupo,
                    periodo = it.periodo,
                    docenteNombre = it.docente_nombre,
                    docenteId = it.docente_id.toInt()
                )
            }
    }

    fun obtenerPorDocente(docenteId: Int): List<MateriaModel> {
        val database = requireDb()
        return database.materiaQueries.getMateriasByDocente(docenteId.toLong())
            .executeAsList()
            .map {
                MateriaModel(
                    id = it.id,
                    sigla = it.sigla,
                    nombre = it.nombre,
                    grupo = it.grupo,
                    periodo = it.periodo,
                    docenteNombre = it.docente_nombre,
                    docenteId = it.docente_id.toInt()
                )
            }
    }

    fun cargarMateriasDocente(docenteId: Int) {
        _materiasDocente.value = obtenerPorDocente(docenteId)
    }

    fun setDocenteActual(docente: DocenteModel?) {
        _docenteActual.value = docente
    }

    fun limpiarMateriasDocente() {
        _materiasDocente.value = emptyList()
        _docenteActual.value = null
    }

    fun cargarMateriasEstudiante(carnet: Int) {
        _materiasEstudiante.value = obtenerPorEstudiante(carnet)
    }

    fun limpiarMateriasEstudiante() {
        _materiasEstudiante.value = emptyList()
    }

    fun obtenerPorFormacion(sigla: String, grupo: String, periodo: String): MateriaModel? {
        val database = requireDb()
        return database.materiaQueries.getMateriaByFormacion(sigla, grupo, periodo)
            .executeAsOneOrNull()
            ?.let {
                MateriaModel(
                    id = it.id,
                    sigla = it.sigla,
                    nombre = it.nombre,
                    grupo = it.grupo,
                    periodo = it.periodo,
                    docenteNombre = it.docente_nombre,
                    docenteId = it.docente_id.toInt()
                )
            }
    }

    fun obtenerPorEstudiante(carnet: Int): List<MateriaModel> {
        val database = requireDb()
        return database.inscritoQueries.getMateriasByEstudiante(carnet.toLong())
            .executeAsList()
            .map {
                MateriaModel(
                    id = it.id,
                    sigla = it.sigla,
                    nombre = it.nombre,
                    grupo = it.grupo,
                    periodo = it.periodo,
                    docenteNombre = it.docente_nombre,
                    docenteId = it.docente_id.toInt()
                )
            }
    }

    fun actualizar(materia: MateriaModel) {
        val database = requireDb()
        database.materiaQueries.updateMateria(
            sigla = materia.sigla,
            nombre = materia.nombre,
            grupo = materia.grupo,
            periodo = materia.periodo,
            docente_nombre = materia.docenteNombre,
            id = materia.id
        )
    }

    fun eliminar(id: Long) {
        val database = requireDb()
        database.materiaQueries.deleteMateria(id)
    }
}
