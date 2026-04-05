package com.example.attendance.controller

import com.example.attendance.ILoginView
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.MateriaModel

class LoginController(
    private val docenteModel: DocenteModel,
    private val estudianteModel: EstudianteModel,
    private val materiaModel: MateriaModel,
    private var view: ILoginView,
) {
    fun setView(view: ILoginView) {
        this.view = view
    }

    fun iniciarSesion(carnetInput: String, esDocente: Boolean): String? {
        val carnet = carnetInput.toIntOrNull() ?: return "Ingresa un carnet valido"

        if (esDocente) {
            var docente = docenteModel.obtener(carnet)
            if (docente == null) {
                docenteModel.insertar(DocenteModel(carnetIdentidad = carnet, nombre = "", apellido = ""))
                docente = docenteModel.obtener(carnet)
            }
            if (docente == null) return "No se pudo iniciar sesion"
            materiaModel.setDocenteActual(docente)
            materiaModel.cargarMateriasDocente(carnet)
            materiaModel.limpiarMateriasEstudiante()
            materiaModel.setEstudianteActualCarnet(null)
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
        }

        if (esDocente) {
            view.onMateriaDocenteView(carnet)
        } else {
            view.onMateriaEstudianteView(carnet)
        }

        return null
    }
}
