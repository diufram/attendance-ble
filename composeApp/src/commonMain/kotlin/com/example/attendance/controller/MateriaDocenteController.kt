package com.example.attendance.controller

import com.example.attendance.model.MateriaModel
import com.example.attendance.view.IMateriaDocenteView

class MateriaDocenteController(
    private val materiaModel: MateriaModel,
    private val view: IMateriaDocenteView,
) {

    fun iniciar(carnet: Long) {
        materiaModel.cargarMaterias(carnet, esDocente = true)
        view.setMaterias(materiaModel.materiasUsuario.value)
    }

    fun crear() {
        val carnet = materiaModel.usuarioCarnet.value ?: run {
            view.setErrorMensaje("No hay sesión activa")
            return
        }

        val creada = materiaModel.crear(
            MateriaModel(
                sigla = view.sigla.value.trim(),
                nombre = view.nombre.value.trim(),
                grupo = view.grupo.value.trim(),
                periodo = view.periodo.value.trim(),
                docenteCarnet = carnet,
            )
        )

        if (creada == null) {
            view.setErrorMensaje("No se pudo crear la materia")
            return
        }

        materiaModel.cargarMaterias(carnet, esDocente = true)
        view.setMaterias(materiaModel.materiasUsuario.value)
        view.onCerrarModalCrear()
    }

    fun guardar() {
        val carnet = materiaModel.usuarioCarnet.value ?: run {
            view.setErrorMensaje("No hay sesión activa")
            return
        }

        val base = view.materiaSeleccionadaAccion.value ?: return
        val materia = MateriaModel(
            id = base.id,
            sigla = view.sigla.value.trim(),
            nombre = view.nombre.value.trim(),
            grupo = view.grupo.value.trim(),
            periodo = view.periodo.value.trim(),
            docenteCarnet = base.docenteCarnet,
            bitmapIndexEstudiante = base.bitmapIndexEstudiante,
        )

        val actualizado = materiaModel.editar(materia)
        if (!actualizado) {
            view.setErrorMensaje("No se pudo editar la materia")
            return
        }

        materiaModel.cargarMaterias(carnet, esDocente = true)
        view.setMaterias(materiaModel.materiasUsuario.value)
        view.onCerrarModalEditar()
    }

    fun eliminar() {
        val carnet = materiaModel.usuarioCarnet.value ?: run {
            view.setErrorMensaje("No hay sesión activa")
            return
        }

        val materia = view.materiaSeleccionadaAccion.value ?: return
        val eliminado = materiaModel.eliminar(materia)
        if (!eliminado) {
            view.setErrorMensaje("No se pudo eliminar la materia")
            return
        }

        materiaModel.cargarMaterias(carnet, esDocente = true)
        view.setMaterias(materiaModel.materiasUsuario.value)
        view.onCerrarModalEliminar()
    }
    fun cerrarSesion() {
        materiaModel.limpiarMaterias()
    }

}
