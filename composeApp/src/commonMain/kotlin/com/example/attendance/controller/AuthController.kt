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

        val carnet = carnet.toLongOrNull() ?: return "Carnet debe ser numérico"

        val docente = docenteModel.obtenerPorCarnet(carnet)
        if (docente != null) {
            materiaModel.limpiarMaterias()
            materiaModel.cargarMaterias(carnet, esDocente = true)
            navigator.irMateriaDocenteView()
            return null
        }

        val estudiante = estudianteModel.obtenerPorCarnet(carnet)
        if (estudiante != null) {
            materiaModel.limpiarMaterias()
            materiaModel.cargarMaterias(carnet, esDocente = false)
            navigator.irMateriaEstudianteView()
            return null
        }

        return "Usuario no encontrado. Regístrate primero"
    }

    fun registrar(carnet: String, nombre: String, apellido: String, esDocente: Boolean): String? {
        if (carnet.isBlank()) return "Ingresa un carnet"
        if (nombre.isBlank()) return "Ingresa el nombre"
        if (apellido.isBlank()) return "Ingresa el apellido"

        val carnet = carnet.toLongOrNull() ?: return "Carnet debe ser numérico"

        materiaModel.limpiarMaterias()
        
        if (esDocente) {
              docenteModel.obtenerPorCarnet(carnet) ?: run {
                docenteModel.crear(
                    DocenteModel(
                        carnetIdentidad = carnet.toLong(),
                        nombre = nombre.trim(),
                        apellido = apellido.trim()
                    )
                )
                docenteModel.obtenerPorCarnet(carnet)
                    ?: return "No se pudo registrar el docente"
            }

            materiaModel.cargarMaterias(carnet, esDocente = true)
            navigator.irMateriaDocenteView()
        } else {
            materiaModel.cargarMaterias(carnet, esDocente = false)
            navigator.irMateriaEstudianteView()
        }

        return null
    }
}
