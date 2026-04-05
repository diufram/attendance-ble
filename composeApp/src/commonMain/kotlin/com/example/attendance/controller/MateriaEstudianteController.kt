package com.example.attendance.controller

import com.example.attendance.IMateriaEstudianteView
import com.example.attendance.model.MateriaModel

class MateriaEstudianteController(
    private val materiaModel: MateriaModel,
    private var view: IMateriaEstudianteView,
) {
    fun setView(view: IMateriaEstudianteView) {
        this.view = view
    }

    fun cerrarSesion() {
        materiaModel.limpiarMateriasEstudiante()
        view.irLogin()
    }
}
