package com.example.attendance.controller

import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation

class MateriaDocenteController(
    private val materiaModel: MateriaModel,
    private val navigator: AppNavigation,
) {
    fun materiaSeleccionada(materia: MateriaModel) {
        navigator.irAsistenciaView(materia)
    }

    fun crear(materia: MateriaModel): Boolean {
        val carnet = materiaModel.usuarioCarnet.value?.toLong() ?: return false
        materiaModel.crear(
            MateriaModel(
                sigla = materia.sigla,
                nombre = materia.nombre,
                grupo = materia.grupo,
                periodo = materia.periodo,
                docenteCarnet = carnet,
            )
        ) ?: return false

        materiaModel.cargarMaterias(carnet, esDocente = true)
        return true
    }
    fun cerrarSesion() {
        materiaModel.limpiarMaterias()
        navigator.irLoginView()
    }
}
