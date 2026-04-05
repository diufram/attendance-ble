package com.example.attendance.controller

import com.example.attendance.model.Asistencia
import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.DetalleAsistencia
import com.example.attendance.model.DetalleAsistenciaModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.Materia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AsistenciaViewController(
    private val estudianteModel: EstudianteModel,
    private val asistenciaModel: AsistenciaModel,
    private val detalleAsistenciaModel: DetalleAsistenciaModel
) {
    private val _materiaSeleccionada = MutableStateFlow<Materia?>(null)
    val materiaSeleccionada: StateFlow<Materia?> = _materiaSeleccionada

    private val _asistencias = MutableStateFlow<List<Asistencia>>(emptyList())
    val asistencias: StateFlow<List<Asistencia>> = _asistencias

    fun seleccionarMateria(materia: Materia) {
        _materiaSeleccionada.value = materia
        _asistencias.value = asistenciaModel.obtenerPorMateria(materia.id)
    }

    fun iniciarAsistenciaSeleccionada(): Long? {
        val materia = _materiaSeleccionada.value ?: return null
        val asistenciaId = asistenciaModel.insertarConFechaActual(materia.id)
        registrarDetallesIniciales(asistenciaId, materia.id)
        _asistencias.value = asistenciaModel.obtenerPorMateria(materia.id)
        return asistenciaId
    }

    fun limpiar() {
        _materiaSeleccionada.value = null
        _asistencias.value = emptyList()
    }

    private fun registrarDetallesIniciales(asistenciaId: Long, materiaId: Long) {
        val alumnos = estudianteModel.obtenerPorMateria(materiaId)
        for (alumno in alumnos) {
            detalleAsistenciaModel.insertar(
                DetalleAsistencia(
                    asistenciaId = asistenciaId,
                    estudianteId = alumno.carnetIdentidad,
                    estado = "FALTA"
                )
            )
        }
    }
}
