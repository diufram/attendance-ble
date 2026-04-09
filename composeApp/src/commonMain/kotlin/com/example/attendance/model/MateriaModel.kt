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
    val docenteCarnet: Long? = null,
    val bitmapIndexEstudiante: Int? = null,
    private val db: AttendanceDatabase? = null
) {
    private fun requireDb(): AttendanceDatabase = db ?: error("MateriaModel sin db")
    
    // Información de sesión del usuario actual
    private val _usuarioCarnet = MutableStateFlow<Int?>(null)
    val usuarioCarnet: StateFlow<Int?> = _usuarioCarnet
    
    private val _esDocente = MutableStateFlow<Boolean?>(null)
    val esDocente: StateFlow<Boolean?> = _esDocente
    
    private val _docenteActual = MutableStateFlow<DocenteModel?>(null)
    val docenteActual: StateFlow<DocenteModel?> = _docenteActual
    
    // Materias del usuario actual (unificado)
    private val _materiasUsuario = MutableStateFlow<List<MateriaModel>>(emptyList())
    val materiasUsuario: StateFlow<List<MateriaModel>> = _materiasUsuario

    fun insertar(materia: MateriaModel) {
        val database = requireDb()
        database.materiaQueries.insertMateria(
            sigla = materia.sigla,
            nombre = materia.nombre,
            grupo = materia.grupo,
            periodo = materia.periodo,
            docente_carnet = materia.docenteCarnet
        )
    }
    
    fun cargarMateriasUsuario(carnet: Int, esDocente: Boolean, docente: DocenteModel? = null) {
        val database = requireDb()
        _usuarioCarnet.value = carnet
        _esDocente.value = esDocente
        _docenteActual.value = docente

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
        _esDocente.value = null
        _docenteActual.value = null
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
                    docenteCarnet = it.docente_carnet
                )
            }
    }
}
