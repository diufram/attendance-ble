package com.example.attendance.controller

import com.example.attendance.IInscritosView
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel

class InscritosController(
    private val estudianteModel: EstudianteModel,
    private val inscritoModel: InscritoModel,
    private var view: IInscritosView,
) {
    fun setView(view: IInscritosView) {
        this.view = view
    }

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
        val lineas = contenidoCsv.lines().map { it.trim() }.filter { it.isNotEmpty() }
        for ((index, linea) in lineas.withIndex()) {
            if (linea.isBlank()) continue
            val campos = linea.split(",").map { it.trim() }
            if (campos.size < 3) continue

            if (index == 0 && campos[0].toIntOrNull() == null) continue

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

    fun volver() {
        view.irVolver()
    }

}
