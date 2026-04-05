package com.example.attendance.controller

import com.example.attendance.model.Docente
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.Estudiante
import com.example.attendance.model.EstudianteModel

class LoginController(
    private val docenteModel: DocenteModel,
    private val estudianteModel: EstudianteModel
) {
    fun iniciarSesion(carnetInput: String, esDocente: Boolean): String? {
        val carnet = carnetInput.toIntOrNull() ?: return "Ingresa un carnet valido"

        if (esDocente) {
            var docente = docenteModel.obtener(carnet)
            if (docente == null) {
                docenteModel.insertar(Docente(carnet, "", ""))
                docente = docenteModel.obtener(carnet)
            }
            if (docente == null) return "No se pudo iniciar sesion"
        } else {
            var estudiante = estudianteModel.obtener(carnet)
            if (estudiante == null) {
                estudianteModel.insertar(Estudiante(carnet, "", ""))
                estudiante = estudianteModel.obtener(carnet)
            }
            if (estudiante == null) return "No se pudo iniciar sesion"
        }

        return null
    }
}
