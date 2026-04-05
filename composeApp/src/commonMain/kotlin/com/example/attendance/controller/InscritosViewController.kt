package com.example.attendance.controller

import com.example.attendance.model.Estudiante
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.Inscrito
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.Materia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InscritosViewController(
    private val estudianteModel: EstudianteModel,
    private val inscritoModel: InscritoModel
) {
    private val _materiaSeleccionada = MutableStateFlow<Materia?>(null)
    val materiaSeleccionada: StateFlow<Materia?> = _materiaSeleccionada

    private val _inscritos = MutableStateFlow<List<Estudiante>>(emptyList())
    val inscritos: StateFlow<List<Estudiante>> = _inscritos

    fun seleccionarMateria(materia: Materia) {
        _materiaSeleccionada.value = materia
        _inscritos.value = estudianteModel.obtenerPorMateria(materia.id)
    }

    fun agregarEstudiante(carnetInput: String, nombre: String, apellido: String): Boolean {
        val materiaId = _materiaSeleccionada.value?.id ?: return false
        val carnet = carnetInput.toIntOrNull() ?: return false
        if (nombre.isBlank() || apellido.isBlank()) return false

        if (estudianteModel.obtener(carnet) == null) {
            estudianteModel.insertar(Estudiante(carnet, nombre, apellido))
        }
        inscritoModel.insertar(Inscrito(materiaId = materiaId, estudianteId = carnet))
        _inscritos.value = estudianteModel.obtenerPorMateria(materiaId)
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

            if (estudianteModel.obtener(carnet) == null) {
                estudianteModel.insertar(Estudiante(carnet, nombre, apellido))
            }
            inscritoModel.insertar(Inscrito(materiaId = materiaId, estudianteId = carnet))
        }
        _inscritos.value = estudianteModel.obtenerPorMateria(materiaId)
    }

    fun limpiar() {
        _materiaSeleccionada.value = null
        _inscritos.value = emptyList()
    }
}
