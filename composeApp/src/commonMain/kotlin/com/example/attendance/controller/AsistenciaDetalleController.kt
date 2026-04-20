package com.example.attendance.controller

import com.example.attendance.ble.BleTeacherService
import com.example.attendance.model.AsistenciaDetalleModel
import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.MateriaModel
import com.example.attendance.view.IAsistenciaDetalleView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AsistenciaDetalleController(
    private val asistenciaModel: AsistenciaModel,
    private val asistenciaDetalleModel: AsistenciaDetalleModel,
    private val estudianteModel: EstudianteModel,
    private val view: IAsistenciaDetalleView,
) {
    private val bleService = BleTeacherService()
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            bleService.bleActivo.collect { activo ->
                view.onBleActivo(activo)
            }
        }
        scope.launch {
            bleService.bleEstado.collect { estado ->
                view.onBleEstado(estado)
            }
        }
    }

    fun iniciar(asistenciaId: Long, esNueva: Boolean, materiaId: Long) {
        if (esNueva) {
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
            view.setDetalles(MutableStateFlow(detallesTemporales))
        } else {
            asistenciaDetalleModel.cargarDetallesAsistencia(asistenciaId)
            view.setDetalles(asistenciaDetalleModel.detallesAsistencia)
        }
    }

    fun guardar(materiaId: Long, asistenciaId: Long, esNueva: Boolean): Boolean {
        return try {
            val detallesActuales = view.detalles.value

            val database = asistenciaModel.db ?: return false
            var asistenciaObjetivoId = asistenciaId

            database.transaction {
                if (esNueva) {
                    asistenciaObjetivoId = asistenciaModel.guardar(
                        AsistenciaModel(materiaId = materiaId)
                    )
                    detallesActuales.forEach { detalle ->
                        asistenciaDetalleModel.crear(
                            AsistenciaDetalleModel(
                                asistenciaId = asistenciaObjetivoId,
                                carnetIdentidad = detalle.carnetIdentidad,
                                estado = detalle.estado,
                            )
                        )
                    }
                } else {
                    detallesActuales.forEach { detalle ->
                        asistenciaDetalleModel.actualizarEstado(
                            asistenciaId = asistenciaObjetivoId,
                            carnetIdentidad = detalle.carnetIdentidad,
                            estado = detalle.estado,
                        )
                    }
                }
            }

            asistenciaDetalleModel.cargarDetallesAsistencia(asistenciaObjetivoId)

            view.setDetalles(asistenciaDetalleModel.detallesAsistencia)
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
        view.setDetalles(MutableStateFlow(detallesActualizados))

        val detalle = detallesActuales.firstOrNull { it.carnetIdentidad == detalleSeleccionado.carnetIdentidad }
        val bitmapIndex = detalle?.bitmapIndexEstudiante
        if (bitmapIndex != null) {
            bleService.actualizarPresencia(bitmapIndex, nuevoEstado == "PRESENTE")
        }
    }

    fun iniciarEscaneo(materia: MateriaModel): String? {
        val detalles = view.detalles.value
        if (detalles.isEmpty()) return "No hay estudiantes para iniciar BLE"

        val presenciasIniciales = detalles.mapNotNull { detalle ->
            val idx = detalle.bitmapIndexEstudiante ?: return@mapNotNull null
            idx to (detalle.estado == "PRESENTE")
        }

        val error = bleService.iniciarBleDocente(
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
                    view.setDetalles(MutableStateFlow(actualizados))
                }
            },
        )
        if (error != null) {
            view.onBleActivo(false)
            view.onBleEstado(error)
        }
        return error
    }

    fun detenerEscaneo() {
        bleService.detenerBleDocente()
    }
}
