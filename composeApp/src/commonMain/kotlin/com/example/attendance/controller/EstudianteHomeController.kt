package com.example.attendance.controller

import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EstudianteHomeController(
    private val db: AttendanceDatabase,
) {
    sealed class NavigationEvent {
        data object IrLogin : NavigationEvent()
    }

    private val _estudiante = MutableStateFlow<Estudiante?>(null)
    val estudiante: StateFlow<Estudiante?> = _estudiante

    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materias: StateFlow<List<Materia>> = _materias

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun cargarEstudiante(carnet: Int): Boolean {
        val estudiante = Estudiante.obtenerPorCarnet(db, carnet) ?: return false
        _estudiante.value = estudiante
        cargarMaterias()
        return true
    }

    fun cerrarSesion() {
        _estudiante.value = null
        _materias.value = emptyList()
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
        val actualizado = estudianteActual.copy(nombre = nombre, apellido = apellido)
        Estudiante.actualizar(db, actualizado)
        _estudiante.value = actualizado
    }

    // ========== MATERIAS ==========

    fun cargarMaterias() {
        val estudianteActual = _estudiante.value ?: return
        _materias.value = Materia.obtenerPorEstudiante(db, estudianteActual.carnetIdentidad)
    }
}
