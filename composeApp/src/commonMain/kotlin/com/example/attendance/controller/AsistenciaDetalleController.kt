package com.example.attendance.controller

import com.example.attendance.ble.BleTeacherService
import com.example.attendance.model.AsistenciaDetalleModel
import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.view.IAsistenciaDetalleView
import kotlinx.coroutines.flow.StateFlow

class AsistenciaDetalleController(
    private val asistenciaModel: AsistenciaModel,
    private val asistenciaDetalleModel: AsistenciaDetalleModel,
    private val estudianteModel: EstudianteModel,
    private val view: IAsistenciaDetalleView,
) {
    private val bleService = BleTeacherService()

    val bleActivo: StateFlow<Boolean> = bleService.bleActivo
    val bleEstado: StateFlow<String> = bleService.bleEstado

    fun iniciar(asistenciaId: Long, esNueva: Boolean, materiaId: Long) {
        if (esNueva) {
            // Cargar estudiantes inscritos para nueva asistencia
            val alumnos = estudianteModel.obtenerPorMateria(materiaId)
            val detallesTemporales = alumnos.mapIndexed { index, alumno ->
                AsistenciaDetalleModel(
                    id = index.toLong(),
                    asistenciaId = -1L,
                    carnetIdentidad = alumno.carnetIdentidad,
                    estado = "FALTA",
                    nombreEstudiante = alumno.nombre,
                    apellidoEstudiante = alumno.apellido,
                    bitmapIndexEstudiante = index,
                )
            }
            view.setDetalles(detallesTemporales)
        } else {
            asistenciaDetalleModel.cargarDetallesAsistencia(asistenciaId)
            view.setDetalles(asistenciaDetalleModel.detallesAsistencia.value)
        }
    }

    fun guardar(materiaId: Long, asistenciaId: Long, esNueva: Boolean): Boolean {
        return try {
            val detallesActuales = view.detalles.value

            if (esNueva) {
                val nuevaAsistenciaId = asistenciaModel.guardar(
                    AsistenciaModel(materiaId = materiaId)
                )
                detallesActuales.forEach { detalle ->
                    asistenciaDetalleModel.crear(
                        AsistenciaDetalleModel(
                            asistenciaId = nuevaAsistenciaId,
                            carnetIdentidad = detalle.carnetIdentidad,
                            estado = detalle.estado,
                        )
                    )
                }
                asistenciaDetalleModel.cargarDetallesAsistencia(nuevaAsistenciaId)
            } else {
                detallesActuales.forEach { detalle ->
                    asistenciaDetalleModel.actualizarEstado(
                        asistenciaId = asistenciaId,
                        carnetIdentidad = detalle.carnetIdentidad,
                        estado = detalle.estado,
                    )
                }
                asistenciaDetalleModel.cargarDetallesAsistencia(asistenciaId)
            }

            view.setDetalles(asistenciaDetalleModel.detallesAsistencia.value)
            asistenciaModel.cargarAsistenciasMateria(materiaId)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun alternarEstado(detalleSeleccionado: AsistenciaDetalleModel) {
        val nuevoEstado = if (detalleSeleccionado.estado == "PRESENTE") "FALTA" else "PRESENTE"

        val detallesActuales = view.detalles.value
        val detallesActualizados = detallesActuales.map { detalle ->
            if (detalle.carnetIdentidad == detalleSeleccionado.carnetIdentidad) {
                detalle.copy(estado = nuevoEstado)
            } else {
                detalle
            }
        }
        view.setDetalles(detallesActualizados)

        val detalle = detallesActuales.firstOrNull { it.carnetIdentidad == detalleSeleccionado.carnetIdentidad }
        val bitmapIndex = detalle?.bitmapIndexEstudiante
        if (bitmapIndex != null) {
            bleService.actualizarPresencia(bitmapIndex, nuevoEstado == "PRESENTE")
        }
    }

    fun onIniciarEscaneo(materia: MateriaModel): String? {
        val detalles = view.detalles.value
        if (detalles.isEmpty()) return "No hay estudiantes para iniciar BLE"

        val presenciasIniciales = detalles.mapNotNull { detalle ->
            val idx = detalle.bitmapIndexEstudiante ?: return@mapNotNull null
            idx to (detalle.estado == "PRESENTE")
        }

        return bleService.iniciarBleDocente(
            sigla = materia.sigla,
            grupo = materia.grupo,
            totalEstudiantes = detalles.size,
            presenciasIniciales = presenciasIniciales,
            onMarcadoPresente = { bitmapIndex, _ ->
                val detalle = view.detalles.value.firstOrNull {
                    it.bitmapIndexEstudiante == bitmapIndex
                }

                if (detalle != null && detalle.estado != "PRESENTE") {
                    val actualizados = view.detalles.value.map { item ->
                        if (item.carnetIdentidad == detalle.carnetIdentidad) {
                            item.copy(estado = "PRESENTE")
                        } else {
                            item
                        }
                    }
                    view.setDetalles(actualizados)
                }
            },
        )
    }

    fun detenerEscaneo() {
        bleService.detenerBleDocente()
    }
}
