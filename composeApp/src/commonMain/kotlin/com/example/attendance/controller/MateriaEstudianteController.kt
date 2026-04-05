package com.example.attendance.controller

import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.MateriaModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MateriaEstudianteController(
    private val estudianteModel: EstudianteModel,
    private val materiaModel: MateriaModel,
) {
    sealed class NavigationEvent {
        data object IrLogin : NavigationEvent()
    }

    private val _estudiante = MutableStateFlow<EstudianteModel?>(null)
    val estudiante: StateFlow<EstudianteModel?> = _estudiante

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun cargarEstudiante(carnet: Int): Boolean {
        val estudiante = estudianteModel.obtenerPorCarnet(carnet) ?: return false
        _estudiante.value = estudiante
        cargarMaterias()
        return true
    }

    fun cerrarSesion() {
        _estudiante.value = null
        materiaModel.limpiarMateriasEstudiante()
    }

    fun solicitarCerrarSesion() {
        cerrarSesion()
        _navigationEvent.value = NavigationEvent.IrLogin
    }

    fun limpiarNavegacion() {
        _navigationEvent.value = null
    }

    fun actualizarPerfil(nombre: String, apellido: String) {
        val estudianteActual = _estudiante.value ?: return
        val actualizado = EstudianteModel(
            id = estudianteActual.id,
            carnetIdentidad = estudianteActual.carnetIdentidad,
            nombre = nombre,
            apellido = apellido
        )
        estudianteModel.actualizar(actualizado)
        _estudiante.value = actualizado
    }

    // ========== MATERIAS ==========

    fun cargarMaterias() {
        val estudianteActual = _estudiante.value ?: return
        materiaModel.cargarMateriasEstudiante(estudianteActual.carnetIdentidad)
    }
}
