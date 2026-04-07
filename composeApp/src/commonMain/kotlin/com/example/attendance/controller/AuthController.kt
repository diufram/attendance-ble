package com.example.attendance.controller

import com.example.attendance.model.DocenteModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation

class AuthController(
    private val docenteModel: DocenteModel,
    private val estudianteModel: EstudianteModel,
    private val materiaModel: MateriaModel,
    private val navigator: AppNavigation
) {
    fun login(carnet: String): String? {
        if (carnet.isBlank()) return "Ingresa un carnet"

        val carnet = carnet.toIntOrNull() ?: return "Carnet debe ser numérico"

        val docente = docenteModel.obtenerPorCarnet(carnet)
        if (docente != null) {
            materiaModel.setDocenteActual(docente)
            materiaModel.cargarMateriasDocente(docente.carnetIdentidad.toLong())
            materiaModel.limpiarMateriasEstudiante()
            materiaModel.setEstudianteActualCarnet(null)
            navigator.irMateriaDocenteView()
            return null
        }

        val estudiante = estudianteModel.obtenerPorCarnet(carnet)
        if (estudiante != null) {
            materiaModel.cargarMateriasEstudiante(carnet)
            materiaModel.setEstudianteActualCarnet(carnet)
            materiaModel.setDocenteActual(null)
            materiaModel.limpiarMateriasDocente()
            navigator.irMateriaEstudianteView()
            return null
        }

        return "Usuario no encontrado. Regístrate primero"
    }

    fun registrar(carnet: String, nombre: String, apellido: String, esDocente: Boolean): String? {
        if (carnet.isBlank()) return "Ingresa un carnet"
        if (nombre.isBlank()) return "Ingresa el nombre"
        if (apellido.isBlank()) return "Ingresa el apellido"

        val carnet = carnet.toIntOrNull() ?: return "Carnet debe ser numérico"

        if (esDocente) {
            var docente = docenteModel.obtenerPorCarnet(carnet)
            if (docente == null) {
                val docenteId = docenteModel.insertar(
                    DocenteModel(
                        carnetIdentidad = carnet.toLong(),
                        nombre = nombre.trim(),
                        apellido = apellido.trim()
                    )
                )
                docente = docenteModel.obtenerPorId(docenteId)
            }

            if (docente == null) return "No se pudo registrar el docente"

            materiaModel.setDocenteActual(docente)
            materiaModel.cargarMateriasDocente(docente.carnetIdentidad.toLong())
            materiaModel.limpiarMateriasEstudiante()
            materiaModel.setEstudianteActualCarnet(null)

            navigator.irMateriaDocenteView()
        } else {
            var estudiante = estudianteModel.obtenerPorCarnet(carnet)
            if (estudiante == null) {
                val id = estudianteModel.insertar(
                    EstudianteModel(
                        carnetIdentidad = carnet.toLong(),
                        nombre = nombre.trim(),
                        apellido = apellido.trim()
                    )
                )
                estudiante = estudianteModel.obtenerPorId(id)
            }

            if (estudiante == null) return "No se pudo registrar el estudiante"

            materiaModel.cargarMateriasEstudiante(carnet)
            materiaModel.setEstudianteActualCarnet(carnet)
            materiaModel.setDocenteActual(null)
            materiaModel.limpiarMateriasDocente()

            navigator.irMateriaEstudianteView()
        }

        return null
    }
}