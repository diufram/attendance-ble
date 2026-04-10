package com.example.attendance.model

import com.example.attendance.db.Database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MateriaModel(
    val id: Long = 0,
    val nombre: String = "",
    val sigla: String = "",
    val grupo: String = "",
    val periodo: String = "",
    val docenteCarnet: Long? = null,
    val bitmapIndexEstudiante: Int? = null,
    private val db: Database? = null
) {
    private fun requireDb(): Database = db ?: error("MateriaModel sin db")
    private val _usuarioCarnet = MutableStateFlow<Long?>(null)
    val usuarioCarnet: StateFlow<Long?> = _usuarioCarnet
    private val _materiasUsuario = MutableStateFlow<List<MateriaModel>>(emptyList())
    val materiasUsuario: StateFlow<List<MateriaModel>> = _materiasUsuario


    fun crear(materia: MateriaModel): MateriaModel? {
        val database = requireDb()

        val existente = database.materiaQueries.getMateriaByFormacion(
            sigla = materia.sigla,
            grupo = materia.grupo,
            periodo = materia.periodo,
        ).executeAsOneOrNull()

        if (existente != null) {
            return MateriaModel(
                id = existente.id,
                sigla = existente.sigla,
                nombre = existente.nombre,
                grupo = existente.grupo,
                periodo = existente.periodo,
                docenteCarnet = existente.docente_carnet,
            )
        }

        database.materiaQueries.insertMateriaOrIgnore(
            sigla = materia.sigla,
            nombre = materia.nombre,
            grupo = materia.grupo,
            periodo = materia.periodo,
            docente_carnet = materia.docenteCarnet
        )

        return database.materiaQueries.getMateriaByFormacion(
            sigla = materia.sigla,
            grupo = materia.grupo,
            periodo = materia.periodo,
        ).executeAsOneOrNull()?.let {
            MateriaModel(
                id = it.id,
                sigla = it.sigla,
                nombre = it.nombre,
                grupo = it.grupo,
                periodo = it.periodo,
                docenteCarnet = it.docente_carnet,
            )
        }
    }

    fun editar(materia: MateriaModel): Boolean {
        if (materia.id <= 0L) return false
        val database = requireDb()

        val existente = database.materiaQueries.getMateriaByFormacion(
            sigla = materia.sigla,
            grupo = materia.grupo,
            periodo = materia.periodo,
        ).executeAsOneOrNull()

        if (existente != null && existente.id != materia.id) {
            return false
        }

        database.materiaQueries.updateMateria(
            sigla = materia.sigla,
            nombre = materia.nombre,
            grupo = materia.grupo,
            periodo = materia.periodo,
            id = materia.id,
        )
        return true
    }

    fun eliminar(materia: MateriaModel): Boolean {
        if (materia.id <= 0L) return false
        val database = requireDb()
        return runCatching {
            database.materiaQueries.deleteMateria(materia.id)
        }.isSuccess
    }
    
    fun cargarMaterias(carnet: Long, esDocente: Boolean) {
        val database = requireDb()
        _usuarioCarnet.value = carnet

        _materiasUsuario.value = if (esDocente) {
            database.materiaQueries.getMateriasByDocente(carnet.toLong())
                .executeAsList()
                .map {
                    MateriaModel(
                        id = it.id,
                        sigla = it.sigla,
                        nombre = it.nombre,
                        grupo = it.grupo,
                        periodo = it.periodo,
                        docenteCarnet = it.docente_carnet
                    )
                }
        } else {
            database.inscritoQueries.getMateriasByEstudiante(carnet.toLong())
                .executeAsList()
                .map {
                    MateriaModel(
                        id = it.id,
                        sigla = it.sigla,
                        nombre = it.nombre,
                        grupo = it.grupo,
                        periodo = it.periodo,
                        docenteCarnet = it.docente_carnet,
                        bitmapIndexEstudiante = it.bitmap_index_estudiante.toInt()
                    )
                }
        }
    }

    fun limpiarMaterias() {
        _materiasUsuario.value = emptyList()
        _usuarioCarnet.value = null
    }

}
