package com.example.attendance.controller

import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.model.Docente
import com.example.attendance.model.Materia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MateriaDocenteController(
    private val db: AttendanceDatabase,
) {
    sealed class NavigationEvent {
        data class IrAsistencia(val materia: Materia) : NavigationEvent()
        data object IrLogin : NavigationEvent()
    }

    private val _docente = MutableStateFlow<Docente?>(null)
    val docente: StateFlow<Docente?> = _docente

    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materias: StateFlow<List<Materia>> = _materias

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun cargarDocente(carnet: Int): Boolean {
        val docente = Docente.obtener(db, carnet) ?: return false
        _docente.value = docente
        cargarMaterias()
        return true
    }

    fun cerrarSesion() {
        _docente.value = null
        _materias.value = emptyList()
    }

    fun solicitarCerrarSesion() {
        cerrarSesion()
        _navigationEvent.value = NavigationEvent.IrLogin
    }

    fun seleccionarMateria(materiaId: Long) {
        val materia = _materias.value.firstOrNull { it.id == materiaId } ?: return
        _navigationEvent.value = NavigationEvent.IrAsistencia(materia)
    }

    fun limpiarNavegacion() {
        _navigationEvent.value = null
    }

    fun crearMateria(sigla: String, nombre: String, grupo: String, periodo: String): Boolean {
        val docenteActual = _docente.value ?: return false
        if (Materia.obtenerPorFormacion(db, sigla, grupo, periodo) != null) {
            return false
        }

        val docenteNombre = listOf(docenteActual.nombre, docenteActual.apellido)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Docente ${docenteActual.carnetIdentidad}" }

        Materia.insertar(
            db,
            Materia(
                sigla = sigla,
                nombre = nombre,
                grupo = grupo,
                periodo = periodo,
                docenteNombre = docenteNombre,
                docenteId = docenteActual.carnetIdentidad
            )
        )
        cargarMaterias()
        return true
    }

    fun cargarMaterias() {
        val docenteActual = _docente.value ?: return
        _materias.value = Materia.obtenerPorDocente(db, docenteActual.carnetIdentidad)
    }
}
