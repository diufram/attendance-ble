package com.example.attendance.controller

import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation

class MateriaDocenteController(
    private val materiaModel: MateriaModel,
    private val navigator: AppNavigation,
) {
    fun materiaSeleccionada(materia: MateriaModel) {
        navigator.irAsistenciaView(materia.id)
    }

    fun crear(materia: MateriaModel): Boolean {
        val carnet = materiaModel.usuarioCarnet.value ?: return false
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

    fun editar(materia: MateriaModel): Boolean {
        val carnet = materiaModel.usuarioCarnet.value ?: return false
        val actualizado = materiaModel.editar(materia)
        if (!actualizado) return false
        materiaModel.cargarMaterias(carnet, esDocente = true)
        return true
    }

    fun eliminar(materia: MateriaModel): Boolean {
        val carnet = materiaModel.usuarioCarnet.value ?: return false
        val eliminado = materiaModel.eliminar(materia)
        if (!eliminado) return false
        materiaModel.cargarMaterias(carnet, esDocente = true)
        return true
    }

    fun cerrarSesion() {
        materiaModel.limpiarMaterias()
        navigator.irLoginView()
    }
}
