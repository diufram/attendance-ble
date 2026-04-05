package com.example.attendance.controller

import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.model.Estudiante
import com.example.attendance.model.Inscrito
import com.example.attendance.model.Materia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InscritosViewController(
    private val db: AttendanceDatabase
) {
    sealed class NavigationEvent {
        data object Volver : NavigationEvent()
    }

    private val _materiaSeleccionada = MutableStateFlow<Materia?>(null)
    val materiaSeleccionada: StateFlow<Materia?> = _materiaSeleccionada

    private val _inscritos = MutableStateFlow<List<Estudiante>>(emptyList())
    val inscritos: StateFlow<List<Estudiante>> = _inscritos

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun seleccionarMateria(materia: Materia) {
        _materiaSeleccionada.value = materia
        _inscritos.value = Estudiante.obtenerPorMateria(db, materia.id)
    }

    fun agregarEstudiante(carnetInput: String, nombre: String, apellido: String): Boolean {
        val materiaId = _materiaSeleccionada.value?.id ?: return false
        val carnet = carnetInput.toIntOrNull() ?: return false
        if (nombre.isBlank() || apellido.isBlank()) return false

        val insertOk = runCatching {
            val estudianteId = Estudiante.insertar(
                db,
                Estudiante(carnetIdentidad = carnet, nombre = nombre, apellido = apellido)
            )
            Inscrito.insertar(db, Inscrito(materiaId = materiaId, estudianteId = estudianteId))
        }.isSuccess

        if (!insertOk) return false

        _inscritos.value = Estudiante.obtenerPorMateria(db, materiaId)
        return true
    }

    fun importarDesdeCsv(contenidoCsv: String) {
        val materiaId = _materiaSeleccionada.value?.id ?: return
        val lineas = contenidoCsv.lines().drop(1)
        for (linea in lineas) {
            if (linea.isBlank()) continue
            val campos = linea.split(",").map { it.trim() }
            if (campos.size < 3) continue

            val carnet = campos[0].toIntOrNull() ?: continue
            val nombre = campos[1]
            val apellido = campos[2]

            val estudianteId = Estudiante.insertar(
                db,
                Estudiante(carnetIdentidad = carnet, nombre = nombre, apellido = apellido)
            )
            Inscrito.insertar(db, Inscrito(materiaId = materiaId, estudianteId = estudianteId))
        }
        _inscritos.value = Estudiante.obtenerPorMateria(db, materiaId)
    }

    fun limpiar() {
        _materiaSeleccionada.value = null
        _inscritos.value = emptyList()
    }

    fun solicitarVolver() {
        _navigationEvent.value = NavigationEvent.Volver
    }

    fun limpiarNavegacion() {
        _navigationEvent.value = null
    }
}
