package com.example.attendance.controller

import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation

class MateriaDocenteController(
    private val materiaModel: MateriaModel,
    private val navigator: AppNavigation,
) {
    fun cerrarSesion() {
        materiaModel.limpiarMaterias()
        navigator.irLoginView()
    }

    fun materiaSeleccionada(materiaId: Long) {
        val materia = materiaModel.materiasUsuario.value.firstOrNull { it.id == materiaId } ?: return
        navigator.irAsistenciaView(materia)
    }

    fun crearMateria(sigla: String, nombre: String, grupo: String, periodo: String): Boolean {
        val docente = materiaModel.docenteActual.value ?: return false
        if (materiaModel.obtenerPorFormacion(sigla, grupo, periodo) != null) return false

        val carnet = docente.carnetIdentidad
        materiaModel.insertar(
            MateriaModel(
                sigla = sigla,
                nombre = nombre,
                grupo = grupo,
                periodo = periodo,
                docenteCarnet = carnet
            )
        )
        // Recargar materias manteniendo el docente actual
        materiaModel.cargarMateriasUsuario(carnet.toInt(), esDocente = true, docente = docente)
        return true
    }
}