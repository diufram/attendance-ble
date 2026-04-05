package com.example.attendance.controller

import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InscritosController(
    private val estudianteModel: EstudianteModel,
    private val inscritoModel: InscritoModel
) {
    sealed class NavigationEvent {
        data object Volver : NavigationEvent()
    }

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun seleccionarMateria(materia: MateriaModel) {
        inscritoModel.setMateriaSeleccionada(materia)
        inscritoModel.cargarInscritosMateria(materia.id)
    }

    fun agregarEstudiante(carnetInput: String, nombre: String, apellido: String): Boolean {
        val materiaId = inscritoModel.materiaSeleccionada.value?.id ?: return false
        val carnet = carnetInput.toIntOrNull() ?: return false
        if (nombre.isBlank() || apellido.isBlank()) return false

        val insertOk = runCatching {
            val estudianteId = estudianteModel.insertar(
                EstudianteModel(carnetIdentidad = carnet, nombre = nombre, apellido = apellido)
            )
            inscritoModel.insertar(InscritoModel(materiaId = materiaId, estudianteId = estudianteId))
        }.isSuccess

        if (!insertOk) return false

        inscritoModel.cargarInscritosMateria(materiaId)
        return true
    }

    fun importarDesdeCsv(contenidoCsv: String) {
        val materiaId = inscritoModel.materiaSeleccionada.value?.id ?: return
        val lineas = contenidoCsv.lines().drop(1)
        for (linea in lineas) {
            if (linea.isBlank()) continue
            val campos = linea.split(",").map { it.trim() }
            if (campos.size < 3) continue

            val carnet = campos[0].toIntOrNull() ?: continue
            val nombre = campos[1]
            val apellido = campos[2]

            val estudianteId = estudianteModel.insertar(
                EstudianteModel(carnetIdentidad = carnet, nombre = nombre, apellido = apellido)
            )
            inscritoModel.insertar(InscritoModel(materiaId = materiaId, estudianteId = estudianteId))
        }
        inscritoModel.cargarInscritosMateria(materiaId)
    }

    fun limpiar() {
        inscritoModel.limpiarEstadoMateria()
    }

    fun solicitarVolver() {
        _navigationEvent.value = NavigationEvent.Volver
    }

    fun limpiarNavegacion() {
        _navigationEvent.value = null
    }
}
