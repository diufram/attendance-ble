package com.example.attendance.controller

import com.example.attendance.model.Docente
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.Materia
import com.example.attendance.model.MateriaModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MateriaDocenteController(
    private val docenteModel: DocenteModel,
    private val materiaModel: MateriaModel
) {
    private val _docente = MutableStateFlow<Docente?>(null)
    val docente: StateFlow<Docente?> = _docente

    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materias: StateFlow<List<Materia>> = _materias

    fun cargarDocente(carnet: Int): Boolean {
        val docente = docenteModel.obtener(carnet) ?: return false
        _docente.value = docente
        cargarMaterias()
        return true
    }

    fun cerrarSesion() {
        _docente.value = null
        _materias.value = emptyList()
    }

    fun crearMateria(sigla: String, nombre: String, grupo: String) {
        val docenteActual = _docente.value ?: return
        materiaModel.insertar(
            Materia(
                sigla = sigla,
                nombre = nombre,
                grupo = grupo,
                docenteId = docenteActual.carnetIdentidad
            )
        )
        cargarMaterias()
    }
    fun cargarMaterias() {
        val docenteActual = _docente.value ?: return
        _materias.value = materiaModel.obtenerPorDocente(docenteActual.carnetIdentidad)
    }
}
