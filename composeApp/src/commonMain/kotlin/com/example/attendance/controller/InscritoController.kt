package com.example.attendance.controller

import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.view.InscritoView

class InscritoController(
    private val estudianteModel: EstudianteModel,
    private val inscritoModel: InscritoModel,
    private val view: InscritoView,
) {
    fun iniciar(materiaId: Long) {
        inscritoModel.cargarInscritosMateria(materiaId)
        view.setInscritos(inscritoModel.inscritosMateria)
    }

    fun agregar(materiaId: Long): Boolean {
        try {
            val carnet = view.carnet.value.toLongOrNull() ?: return false
            val nombre = view.nombre.value.trim()
            val apellido = view.apellido.value.trim()

            if (carnet <= 0 || nombre.isBlank() || apellido.isBlank()) {
                return false
            }

            val estudianteExistente = estudianteModel.obtenerPorCarnet(carnet)

            if (estudianteExistente != null) {
                if (estudianteExistente.nombre != nombre || estudianteExistente.apellido != apellido) {
                    estudianteModel.actualizar(
                        EstudianteModel(
                            id = estudianteExistente.id,
                            carnetIdentidad = estudianteExistente.carnetIdentidad,
                            nombre = nombre,
                            apellido = apellido,
                        )
                    )
                }
            } else {
                val nuevoEstudiante = EstudianteModel(
                    carnetIdentidad = carnet,
                    nombre = nombre,
                    apellido = apellido,
                )
                estudianteModel.crear(nuevoEstudiante)
            }

            inscritoModel.crear(
                InscritoModel(
                    materiaId = materiaId,
                    carnetIdentidad = carnet,
                )
            )
            inscritoModel.cargarInscritosMateria(materiaId)
            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun eliminar(materiaId: Long): Boolean {
        val estudiante = view.estudianteSeleccionado.value ?: return false
        return try {
            inscritoModel.eliminar(
                InscritoModel(
                    materiaId = materiaId,
                    carnetIdentidad = estudiante.carnetIdentidad,
                )
            )
            inscritoModel.cargarInscritosMateria(materiaId)
            true
        } catch (_: Exception) {
            false
        }
    }
}
