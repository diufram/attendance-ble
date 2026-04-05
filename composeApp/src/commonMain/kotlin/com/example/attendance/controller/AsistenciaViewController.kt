package com.example.attendance.controller

import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.model.Asistencia
import com.example.attendance.model.DetalleAsistencia
import com.example.attendance.model.Materia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AsistenciaViewController(
    private val db: AttendanceDatabase
) {
    sealed class NavigationEvent {
        data object Volver : NavigationEvent()
        data class IrInscritos(val materia: Materia) : NavigationEvent()
        data class IrDetalle(val asistenciaId: Long) : NavigationEvent()
    }

    private val _materiaSeleccionada = MutableStateFlow<Materia?>(null)
    val materiaSeleccionada: StateFlow<Materia?> = _materiaSeleccionada

    private val _asistencias = MutableStateFlow<List<Asistencia>>(emptyList())
    val asistencias: StateFlow<List<Asistencia>> = _asistencias

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun seleccionarMateria(materia: Materia) {
        _materiaSeleccionada.value = materia
        _asistencias.value = Asistencia.obtenerPorMateria(db, materia.id)
    }

    fun iniciarAsistenciaSeleccionada(): Long? {
        val materia = _materiaSeleccionada.value ?: return null
        val asistenciaId = Asistencia.insertarConFechaActual(db, materia.id)
        registrarDetallesIniciales(asistenciaId, materia.id)
        _asistencias.value = Asistencia.obtenerPorMateria(db, materia.id)
        return asistenciaId
    }

    fun solicitarVolver() {
        _navigationEvent.value = NavigationEvent.Volver
    }

    fun abrirInscritos() {
        val materia = _materiaSeleccionada.value ?: return
        _navigationEvent.value = NavigationEvent.IrInscritos(materia)
    }

    fun iniciarAsistenciaYAbrirDetalle() {
        val asistenciaId = iniciarAsistenciaSeleccionada() ?: return
        _navigationEvent.value = NavigationEvent.IrDetalle(asistenciaId)
    }

    fun abrirDetalle(asistenciaId: Long) {
        _navigationEvent.value = NavigationEvent.IrDetalle(asistenciaId)
    }

    fun limpiarNavegacion() {
        _navigationEvent.value = null
    }

    fun limpiar() {
        _materiaSeleccionada.value = null
        _asistencias.value = emptyList()
    }

    private fun registrarDetallesIniciales(asistenciaId: Long, materiaId: Long) {
        val alumnos = com.example.attendance.model.Estudiante.obtenerPorMateria(db, materiaId)
        for (alumno in alumnos) {
            DetalleAsistencia.insertar(
                db,
                DetalleAsistencia(
                    asistenciaId = asistenciaId,
                    estudianteId = alumno.id,
                    estado = "FALTA"
                )
            )
        }
    }
}
