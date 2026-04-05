package com.example.attendance.controller

import com.example.attendance.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EstudianteHomeController(
    private val estudianteModel: EstudianteModel,
    private val materiaModel: MateriaModel,
) {

    private val _estudiante = MutableStateFlow<Estudiante?>(null)
    val estudiante: StateFlow<Estudiante?> = _estudiante

    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materias: StateFlow<List<Materia>> = _materias

    fun cargarEstudiante(carnet: Int): Boolean {
        val estudiante = estudianteModel.obtener(carnet) ?: return false
        _estudiante.value = estudiante
        cargarMaterias()
        return true
    }

    fun cerrarSesion() {
        _estudiante.value = null
        _materias.value = emptyList()
    }

    fun actualizarPerfil(nombre: String, apellido: String) {
        val estudianteActual = _estudiante.value ?: return
        val actualizado = estudianteActual.copy(nombre = nombre, apellido = apellido)
        estudianteModel.actualizar(actualizado)
        _estudiante.value = actualizado
    }

    // ========== MATERIAS ==========

    fun cargarMaterias() {
        val estudianteActual = _estudiante.value ?: return
        _materias.value = materiaModel.obtenerPorEstudiante(estudianteActual.carnetIdentidad)
    }
}
