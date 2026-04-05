package com.example.attendance.controller

import com.example.attendance.model.DocenteModel
import com.example.attendance.model.MateriaModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MateriaDocenteController(
    private val docenteModel: DocenteModel,
    private val materiaModel: MateriaModel,
) {
    sealed class NavigationEvent {
        data class IrAsistencia(val materia: MateriaModel) : NavigationEvent()
        data object IrLogin : NavigationEvent()
    }

    private val _docente = MutableStateFlow<DocenteModel?>(null)
    val docente: StateFlow<DocenteModel?> = _docente

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun cargarDocente(carnet: Int): Boolean {
        val docente = docenteModel.obtener(carnet) ?: return false
        _docente.value = docente
        cargarMaterias()
        return true
    }

    fun cerrarSesion() {
        _docente.value = null
        materiaModel.limpiarMateriasDocente()
    }

    fun solicitarCerrarSesion() {
        cerrarSesion()
        _navigationEvent.value = NavigationEvent.IrLogin
    }

    fun seleccionarMateria(materiaId: Long) {
        val materia = materiaModel.materiasDocente.value.firstOrNull { it.id == materiaId } ?: return
        _navigationEvent.value = NavigationEvent.IrAsistencia(materia)
    }

    fun limpiarNavegacion() {
        _navigationEvent.value = null
    }

    fun crearMateria(sigla: String, nombre: String, grupo: String, periodo: String): Boolean {
        val docenteActual = _docente.value ?: return false
        if (materiaModel.obtenerPorFormacion(sigla, grupo, periodo) != null) return false

        val docenteNombre = listOf(docenteActual.nombre, docenteActual.apellido)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Docente ${docenteActual.carnetIdentidad}" }

        materiaModel.insertar(
            MateriaModel(
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
        materiaModel.cargarMateriasDocente(docenteActual.carnetIdentidad)
    }
}
