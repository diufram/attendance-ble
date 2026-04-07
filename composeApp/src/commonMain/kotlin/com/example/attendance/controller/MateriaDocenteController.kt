package com.example.attendance.controller

import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation

class MateriaDocenteController(
    private val materiaModel: MateriaModel,
    private val navigator: AppNavigation,
) {
    fun cerrarSesion() {
        materiaModel.limpiarMateriasDocente()
        navigator.irLoginView()
    }

    fun materiaSeleccionada(materiaId: Long) {
        val materia = materiaModel.materiasDocente.value.firstOrNull { it.id == materiaId } ?: return
        navigator.irAsistenciaView(materia)
    }

    fun crearMateria(sigla: String, nombre: String, grupo: String, periodo: String): Boolean {
        val docente = materiaModel.docenteActual.value ?: return false
        if (materiaModel.obtenerPorFormacion(sigla, grupo, periodo) != null) return false

        materiaModel.insertar(
            MateriaModel(
                sigla = sigla,
                nombre = nombre,
                grupo = grupo,
                periodo = periodo,
                docenteCarnet = docente.carnetIdentidad.toLong()
            )
        )
        materiaModel.cargarMateriasDocente(docente.carnetIdentidad.toLong())
        return true
    }
}
