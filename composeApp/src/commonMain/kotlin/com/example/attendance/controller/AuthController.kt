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
    fun login(carnetInput: String): String? {
        if (carnetInput.isBlank()) return "Ingresa un carnet"

        val carnet = carnetInput.toIntOrNull() ?: return "Carnet debe ser numérico"

        val docente = docenteModel.obtenerPorCarnet(carnet)
        if (docente != null) {
            materiaModel.limpiarMaterias()
            materiaModel.cargarMateriasUsuario(carnet, esDocente = true, docente = docente)
            navigator.irMateriaDocenteView()
            return null
        }

        val estudiante = estudianteModel.obtenerPorCarnet(carnet)
        if (estudiante != null) {
            materiaModel.limpiarMaterias()
            materiaModel.cargarMateriasUsuario(carnet, esDocente = false, docente = null)
            navigator.irMateriaEstudianteView()
            return null
        }

        return "Usuario no encontrado. Regístrate primero"
    }

    fun registrar(carnetInput: String, nombre: String, apellido: String, esDocente: Boolean): String? {
        if (carnetInput.isBlank()) return "Ingresa un carnet"
        if (nombre.isBlank()) return "Ingresa el nombre"
        if (apellido.isBlank()) return "Ingresa el apellido"

        val carnet = carnetInput.toIntOrNull() ?: return "Carnet debe ser numérico"

        materiaModel.limpiarMaterias()
        
        if (esDocente) {
            val docente = docenteModel.obtenerPorCarnet(carnet) ?: run {
                val docenteId = docenteModel.insertar(
                    DocenteModel(
                        carnetIdentidad = carnet.toLong(),
                        nombre = nombre.trim(),
                        apellido = apellido.trim()
                    )
                )
                docenteModel.obtenerPorId(docenteId)
                    ?: return "No se pudo registrar el docente"
            }

            materiaModel.cargarMateriasUsuario(carnet, esDocente = true, docente = docente)
            navigator.irMateriaDocenteView()
        } else {
            materiaModel.cargarMateriasUsuario(carnet, esDocente = false, docente = null)
            navigator.irMateriaEstudianteView()
        }

        return null
    }
}