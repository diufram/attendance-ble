package com.example.attendance.controller

import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.navigation.AppNavigation

class InscritoController(
    private val estudianteModel: EstudianteModel,
    private val inscritoModel: InscritoModel,
    private val navigator: AppNavigation,
) {
    fun agregar(materiaId: Long, estudiante: EstudianteModel): Boolean {
        try {
            val carnet = estudiante.carnetIdentidad.toInt()
            val nombre = estudiante.nombre.trim()
            val apellido = estudiante.apellido.trim()

            if (carnet <= 0 || nombre.isBlank() || apellido.isBlank()) {
                return false
            }

            val estudianteExistente = estudianteModel.obtenerPorCarnet(carnet.toLong())

            if (estudianteExistente != null) {
                if (estudianteExistente.nombre != nombre.trim() || estudianteExistente.apellido != apellido.trim()) {
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
                    carnetIdentidad = carnet.toLong(),
                    nombre = nombre,
                    apellido = apellido,
                )
                estudianteModel.crear(nuevoEstudiante)
            }

            inscritoModel.crear(
                InscritoModel(
                    materiaId = materiaId,
                    carnetIdentidad = carnet.toLong(),
                )
            )
            inscritoModel.cargarInscritosMateria(materiaId)
            return true
        } catch (_: Exception) {
            return false
        }
    }
    fun eliminar(inscrito: InscritoModel): Boolean {
        return try {
            inscritoModel.eliminar(inscrito)
            inscritoModel.cargarInscritosMateria(inscrito.materiaId)
            true
        } catch (_: Exception) {
            false
        }
    }
    fun volver() {
        navigator.volver()
    }
}
