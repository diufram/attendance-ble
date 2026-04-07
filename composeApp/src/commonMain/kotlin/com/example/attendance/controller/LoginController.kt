package com.example.attendance.controller

import com.example.attendance.model.DocenteModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation

class LoginController(
    private val docenteModel: DocenteModel,
    private val estudianteModel: EstudianteModel,
    private val materiaModel: MateriaModel,
    private val navigator: AppNavigation
) {
    private var loginInProgress = false

    fun iniciarSesion(carnetInput: String, esDocente: Boolean): String? {
        if (loginInProgress) return null
        loginInProgress = true

        try {
            val carnet = carnetInput.toIntOrNull() ?: return "Ingresa un carnet valido"

            if (esDocente) {
                var docente = docenteModel.obtenerPorCarnet(carnet)
                if (docente == null) {
                    val docenteId = docenteModel.insertar(DocenteModel(carnetIdentidad = carnet, nombre = "", apellido = ""))
                    docente = docenteModel.obtenerPorId(docenteId)
                }
                if (docente == null) return "No se pudo iniciar sesion"
                materiaModel.setDocenteActual(docente)
                materiaModel.cargarMateriasDocente(docente.id)
                materiaModel.limpiarMateriasEstudiante()
                materiaModel.setEstudianteActualCarnet(null)
                navigator.irMateriaDocenteView()
            } else {
                var estudiante = estudianteModel.obtenerPorCarnet(carnet)
                if (estudiante == null) {
                    estudianteModel.insertar(EstudianteModel(carnetIdentidad = carnet, nombre = "", apellido = ""))
                    estudiante = estudianteModel.obtenerPorCarnet(carnet)
                }
                if (estudiante == null) return "No se pudo iniciar sesion"
                materiaModel.cargarMateriasEstudiante(carnet)
                materiaModel.setEstudianteActualCarnet(carnet)
                materiaModel.setDocenteActual(null)
                materiaModel.limpiarMateriasDocente()
                navigator.irMateriaEstudianteView()
            }

            return null
        } finally {
            loginInProgress = false
        }
    }
}
