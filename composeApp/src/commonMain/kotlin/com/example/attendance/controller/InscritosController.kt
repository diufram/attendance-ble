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
        try {
            println("=== INICIO agregarEstudiante ===")
            println("Params: carnet='$carnetInput', nombre='$nombre', apellido='$apellido'")

            val materiaId = inscritoModel.materiaSeleccionada.value?.id
            println("Materia seleccionada ID: $materiaId")

            if (materiaId == null) {
                println("ERROR: No hay materia seleccionada")
                return false
            }

            val carnet = carnetInput.toIntOrNull()
            println("Carnet parseado: $carnet")

            if (carnet == null) {
                println("ERROR: Carnet inválido")
                return false
            }

            println("Buscando estudiante existente con carnet: $carnet")
            val estudianteExistente = estudianteModel.obtenerPorCarnet(carnet)
            println("Estudiante existente: $estudianteExistente")

            val estudianteId = if (estudianteExistente != null) {
                println("Estudiante ya existe, ID: ${estudianteExistente.id}")
                if (estudianteExistente.nombre != nombre.trim() || estudianteExistente.apellido != apellido.trim()) {
                    println("Actualizando datos del estudiante...")
                    estudianteModel.actualizar(
                        EstudianteModel(
                            id = estudianteExistente.id,
                            carnetIdentidad = estudianteExistente.carnetIdentidad,
                            nombre = nombre.trim(),
                            apellido = apellido.trim()
                        )
                    )
                    println("Datos actualizados")
                }
                estudianteExistente.id
            } else {
                println("Estudiante NO existe, insertando nuevo...")
                val nuevoEstudiante = EstudianteModel(
                    carnetIdentidad = carnet,
                    nombre = nombre.trim(),
                    apellido = apellido.trim()
                )
                println("Modelo creado: $nuevoEstudiante")

                val newId = estudianteModel.insertar(nuevoEstudiante)
                println("Estudiante insertado con ID: $newId")
                newId
            }

            println("Creando inscripción para materiaId=$materiaId, estudianteId=$estudianteId")
            inscritoModel.insertar(InscritoModel(materiaId = materiaId, estudianteId = estudianteId))
            println("Inscripción creada")

            println("Recargando lista de inscritos...")
            inscritoModel.cargarInscritosMateria(materiaId)
            println("Lista recargada")

            println("=== FIN agregarEstudiante SUCCESS ===")
            return true
        } catch (e: Exception) {
            println("=== EXCEPCIÓN CAPTURADA EN agregarEstudiante ===")
            println("Tipo: ${e::class.simpleName}")
            println("Mensaje: ${e.message}")
            println("Stack trace:")
            e.printStackTrace()
            println("=== FIN EXCEPCIÓN ===")
            return false
        }
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

            runCatching {
                // Verificar si el estudiante ya existe por carnet
                val estudianteExistente = estudianteModel.obtenerPorCarnet(carnet)
                val estudianteId = if (estudianteExistente != null) {
                    // Si existe, actualizar datos si son diferentes
                    if (estudianteExistente.nombre != nombre.trim() || estudianteExistente.apellido != apellido.trim()) {
                        estudianteModel.actualizar(
                            EstudianteModel(
                                id = estudianteExistente.id,
                                carnetIdentidad = estudianteExistente.carnetIdentidad,
                                nombre = nombre.trim(),
                                apellido = apellido.trim()
                            )
                        )
                    }
                    estudianteExistente.id
                } else {
                    // Si no existe, insertarlo
                    estudianteModel.insertar(
                        EstudianteModel(carnetIdentidad = carnet, nombre = nombre.trim(), apellido = apellido.trim())
                    )
                }
                // Insertar inscripción (el método ya verifica duplicados)
                inscritoModel.insertar(InscritoModel(materiaId = materiaId, estudianteId = estudianteId))
            }
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
