package com.example.attendance.controller

import com.example.attendance.IMateriaDocenteView
import com.example.attendance.model.MateriaModel

class MateriaDocenteController(
    private val materiaModel: MateriaModel,
    private var view: IMateriaDocenteView,
) {
    fun setView(view: IMateriaDocenteView) {
        this.view = view
    }

    fun cerrarSesion() {
        materiaModel.limpiarMateriasDocente()
        view.irLogin()
    }

    fun seleccionarMateria(materiaId: Long) {
        val materia = materiaModel.materiasDocente.value.firstOrNull { it.id == materiaId } ?: return
        view.irAsistencia(materia)
    }

    fun crearMateria(sigla: String, nombre: String, grupo: String, periodo: String): Boolean {
        val docente = materiaModel.docenteActual.value ?: return false
        if (materiaModel.obtenerPorFormacion(sigla, grupo, periodo) != null) return false

        val docenteNombre = listOf(docente.nombre, docente.apellido)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Docente ${docente.carnetIdentidad}" }

        materiaModel.insertar(
            MateriaModel(
                sigla = sigla,
                nombre = nombre,
                grupo = grupo,
                periodo = periodo,
                docenteNombre = docenteNombre,
                docenteId = docente.carnetIdentidad
            )
        )
        materiaModel.cargarMateriasDocente(docente.carnetIdentidad)
        return true
    }
}
