package com.example.attendance.controller

import com.example.attendance.ble.BleTeacherService
import com.example.attendance.model.AsistenciaDetalleModel
import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.navigation.AppNavigation
import kotlinx.coroutines.flow.StateFlow

class AsistenciaDetalleController(
    private val asistenciaModel: AsistenciaModel,
    private val asistenciaDetalleModel: AsistenciaDetalleModel,
    private val navigator: AppNavigation,
) {
    private val bleService = BleTeacherService()
    val bleActivo: StateFlow<Boolean> = bleService.bleActivo
    val bleEstado: StateFlow<String> = bleService.bleEstado

    fun guardar(asistencia: AsistenciaModel): Boolean {
        return try {
            val detallesActuales = asistenciaDetalleModel.detallesAsistencia.value
            val esNueva = asistencia.id <= 0L

            if (esNueva) {
                val nuevaAsistenciaId = asistenciaModel.guardar(
                    AsistenciaModel(materiaId = asistencia.materiaId)
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
                        asistenciaId = asistencia.id,
                        carnetIdentidad = detalle.carnetIdentidad,
                        estado = detalle.estado,
                    )
                }
                asistenciaDetalleModel.cargarDetallesAsistencia(asistencia.id)
            }

            asistenciaModel.cargarAsistenciasMateria(asistencia.materiaId)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun alternarEstado(detalleSeleccionado: AsistenciaDetalleModel) {
        val nuevoEstado = if (detalleSeleccionado.estado == "PRESENTE") "FALTA" else "PRESENTE"

        val detallesActuales = asistenciaDetalleModel.detallesAsistencia.value
        val detallesActualizados = detallesActuales.map { detalle ->
            if (detalle.carnetIdentidad == detalleSeleccionado.carnetIdentidad) {
                detalle.copy(estado = nuevoEstado)
            } else {
                detalle
            }
        }
        asistenciaDetalleModel.cargarDetallesTemporales(detallesActualizados)

        val detalle = detallesActuales.firstOrNull { it.carnetIdentidad == detalleSeleccionado.carnetIdentidad }
        val bitmapIndex = detalle?.bitmapIndexEstudiante
        if (bitmapIndex != null) {
            bleService.actualizarPresencia(bitmapIndex, nuevoEstado == "PRESENTE")
        }
    }

    fun onIniciarEscaneo(materia: MateriaModel): String? {
        val detalles = asistenciaDetalleModel.detallesAsistencia.value
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
                val detalle = asistenciaDetalleModel.detallesAsistencia.value.firstOrNull {
                    it.bitmapIndexEstudiante == bitmapIndex
                }

                if (detalle != null && detalle.estado != "PRESENTE") {
                    val actualizados = asistenciaDetalleModel.detallesAsistencia.value.map { item ->
                        if (item.carnetIdentidad == detalle.carnetIdentidad) {
                            item.copy(estado = "PRESENTE")
                        } else {
                            item
                        }
                    }
                    asistenciaDetalleModel.cargarDetallesTemporales(actualizados)
                }
            },
        )
    }

    fun detenerEscaneo() {
        bleService.detenerBleDocente()
    }

    fun volver() {
        detenerEscaneo()
        navigator.volver()
    }
}
