package com.example.attendance.controller

import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EstudianteController(private val db: AttendanceDatabase) {

    private val _estudiante = MutableStateFlow<Estudiante?>(null)
    val estudiante: StateFlow<Estudiante?> = _estudiante

    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materias: StateFlow<List<Materia>> = _materias

    private val _asistencias = MutableStateFlow<List<Asistencia>>(emptyList())
    val asistencias: StateFlow<List<Asistencia>> = _asistencias

    private val _detalleAsistencia = MutableStateFlow<List<DetalleAsistencia>>(emptyList())
    val detalleAsistencia: StateFlow<List<DetalleAsistencia>> = _detalleAsistencia

    // ========== INGRESO ==========

    fun ingresar(carnet: Int) {
        var estudiante = Estudiante.obtener(db, carnet)
        if (estudiante == null) {
            Estudiante.insertar(db, Estudiante(carnet, "", ""))
            estudiante = Estudiante.obtener(db, carnet)
        }
        _estudiante.value = estudiante
        cargarMaterias()
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

    // ========== ASISTENCIA ==========

    fun cargarAsistencias(materiaId: Long) {
        _asistencias.value = Asistencia.obtenerPorMateria(db, materiaId)
    }

    fun cargarDetalleAsistencia(asistenciaId: Long) {
        _detalleAsistencia.value = DetalleAsistencia.obtenerPorAsistencia(db, asistenciaId)
    }

    fun obtenerMiEstado(asistenciaId: Long): String {
        val estudianteActual = _estudiante.value ?: return "FALTA"
        val detalle = _detalleAsistencia.value.find {
            it.estudianteId == estudianteActual.carnetIdentidad
        }
        return detalle?.estado ?: "FALTA"
    }

    fun getCarnetParaBle(): Int? {
        return _estudiante.value?.carnetIdentidad
    }

    fun getMateriasParaBle(): List<Materia> {
        return _materias.value
    }
}