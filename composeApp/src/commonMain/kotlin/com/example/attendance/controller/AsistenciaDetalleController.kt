package com.example.attendance.controller

import com.example.attendance.IAsistenciaDetalleView
import com.example.attendance.ble.BleDebug
import com.example.attendance.ble.BlePacketCodec
import com.example.attendance.ble.BleTransceiver
import com.example.attendance.ble.TeacherBleAttendanceSession
import com.example.attendance.ble.toHexPreview
import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.DetalleAsistenciaModel
import com.example.attendance.model.EstudianteModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

class AsistenciaDetalleController(
    private val asistenciaModel: AsistenciaModel,
    private val estudianteModel: EstudianteModel,
    private val detalleAsistenciaModel: DetalleAsistenciaModel,
    private var view: IAsistenciaDetalleView,
) {
    private companion object {
        const val BLE_WARMUP_MS = 2500L
        const val BLE_BATCH_EMIT_MS = 2000L
        const val BLE_FRAGMENT_ADV_MS = 400L
        const val BLE_CACHE_FLUSH_MS = 250L
    }

    private val bleTransceiver = BleTransceiver()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _bleActivo = MutableStateFlow(false)
    val bleActivo: StateFlow<Boolean> = _bleActivo.asStateFlow()

    private val _bleEstado = MutableStateFlow("BLE inactivo")
    val bleEstado: StateFlow<String> = _bleEstado.asStateFlow()

    private var bleSession: TeacherBleAttendanceSession? = null
    private var advertiseJob: Job? = null
    private var batchEmitJob: Job? = null
    private var scanStartJob: Job? = null
    private val ultimoProcesadoPorIndice = mutableMapOf<Int, Long>()
    private val bleClockStart = TimeSource.Monotonic.markNow()
    private var bleWarmupStart: kotlin.time.TimeMark? = null
    private var bitmapDirty: Boolean = false
    private var frozenEmissionPayloads: List<ByteArray> = emptyList()
    private var emissionCursor: Int = 0

    fun setView(view: IAsistenciaDetalleView) {
        this.view = view
    }

    fun seleccionarAsistencia(asistenciaId: Long) {
        detenerBleDocente()
        detalleAsistenciaModel.setAsistenciaSeleccionada(asistenciaId, esNueva = false)
        detalleAsistenciaModel.cargarDetallesAsistencia(asistenciaId)
    }

    fun prepararNuevaAsistencia(materiaId: Long) {
        detenerBleDocente()
        detalleAsistenciaModel.setAsistenciaSeleccionada(-1L, esNueva = true)

        val alumnos = estudianteModel.obtenerPorMateria(materiaId)

        val detallesTemporales = alumnos.mapIndexed { index, alumno ->
            DetalleAsistenciaModel(
                id = index.toLong(),
                asistenciaId = -1L,
                estudianteId = alumno.id,
                estado = "FALTA",
                carnetEstudiante = alumno.carnetIdentidad,
                nombreEstudiante = alumno.nombre,
                apellidoEstudiante = alumno.apellido,
                bitmapIndexEstudiante = index
            )
        }

        detalleAsistenciaModel.cargarDetallesTemporales(detallesTemporales)
    }

    fun guardarAsistencia(): Boolean {
        val materia = asistenciaModel.materiaSeleccionada.value ?: run {
            return false
        }

        return try {
            val asistenciaId = asistenciaModel.insertarConFechaActual(materia.id)

            val detallesActuales = detalleAsistenciaModel.detallesAsistencia.value

            detallesActuales.forEach { detalle ->
                detalleAsistenciaModel.insertar(
                    DetalleAsistenciaModel(
                        asistenciaId = asistenciaId,
                        estudianteId = detalle.estudianteId,
                        estado = detalle.estado
                    )
                )
            }

            detalleAsistenciaModel.setAsistenciaSeleccionada(asistenciaId, esNueva = false)
            detalleAsistenciaModel.cargarDetallesAsistencia(asistenciaId)
            asistenciaModel.cargarAsistenciasMateria(materia.id)

            true
        } catch (e: Exception) {
            false
        }
    }

    fun alternarEstado(estudianteId: Long, estadoActual: String) {
        val asistenciaId = detalleAsistenciaModel.asistenciaSeleccionadaId.value ?: return
        val esNueva = detalleAsistenciaModel.esNuevaAsistencia.value
        val nuevoEstado = if (estadoActual == "PRESENTE") "FALTA" else "PRESENTE"

        if (esNueva) {
            val detallesActuales = detalleAsistenciaModel.detallesAsistencia.value
            val detallesActualizados = detallesActuales.map { detalle ->
                if (detalle.estudianteId == estudianteId) {
                    detalle.copy(estado = nuevoEstado)
                } else {
                    detalle
                }
            }
            detalleAsistenciaModel.cargarDetallesTemporales(detallesActualizados)

            val detalle = detallesActuales.firstOrNull { it.estudianteId == estudianteId }
            val bitmapIndex = detalle?.bitmapIndexEstudiante
            if (bitmapIndex != null) {
                val changed = bleSession?.setPresence(bitmapIndex, nuevoEstado == "PRESENTE") == true
                if (changed) bitmapDirty = true
            }
        } else {
            detalleAsistenciaModel.actualizarEstado(asistenciaId, estudianteId, nuevoEstado)
            detalleAsistenciaModel.cargarDetallesAsistencia(asistenciaId)

            val detalle = detalleAsistenciaModel.detallesAsistencia.value.firstOrNull { it.estudianteId == estudianteId }
            val bitmapIndex = detalle?.bitmapIndexEstudiante
            if (bitmapIndex != null) {
                val changed = bleSession?.setPresence(bitmapIndex, nuevoEstado == "PRESENTE") == true
                if (changed) bitmapDirty = true
            }
        }
    }

    fun limpiar() {
        detenerBleDocente()
        detalleAsistenciaModel.limpiarEstadoAsistencia()
    }

    fun iniciarBleDocente(sigla: String, grupo: String): String? {
        if (sigla.isBlank() || grupo.isBlank()) return "No hay materia activa para BLE"
        val asistenciaId = detalleAsistenciaModel.asistenciaSeleccionadaId.value
            ?: return "No hay asistencia seleccionada"

        val detalles = detalleAsistenciaModel.detallesAsistencia.value
        if (detalles.isEmpty()) return "No hay estudiantes para iniciar BLE"

        BleDebug.log(
            "DOCENTE",
            "Iniciando BLE sigla=$sigla grupo=$grupo asistenciaId=$asistenciaId estudiantes=${detalles.size}"
        )
        BleDebug.log(
            "DOCENTE",
            "Mapa bitmapIndex->estudianteId=" + detalles.joinToString { "${it.bitmapIndexEstudiante}:${it.estudianteId}" }
        )

        detenerBleDocente()

        val session = TeacherBleAttendanceSession(
            sigla = sigla,
            grupo = grupo,
            totalEstudiantes = detalles.size
        )

        detalles.forEach { detalle ->
            val idx = detalle.bitmapIndexEstudiante ?: return@forEach
            session.setPresence(idx, detalle.estado == "PRESENTE")
        }

        bleSession = session
        _bleActivo.value = true
        _bleEstado.value = "Sesion BLE activa"
        bleWarmupStart = TimeSource.Monotonic.markNow()
        bitmapDirty = true
        frozenEmissionPayloads = emptyList()
        emissionCursor = 0

        BleDebug.log("DOCENTE", "Limpieza estricta de cache BLE previa a iniciar")
        bleTransceiver.stopAll()
        scope.launch {
            delay(BLE_CACHE_FLUSH_MS)
            bleTransceiver.stopAll()
        }

        scanStartJob?.cancel()
        scanStartJob = scope.launch {
            delay(BLE_CACHE_FLUSH_MS)
            bleTransceiver.startScanning(
                onPayload = { payload ->
                    val warmup = bleWarmupStart
                    if (warmup != null && warmup.elapsedNow().inWholeMilliseconds < BLE_WARMUP_MS) {
                        BleDebug.log("DOCENTE-SCAN", "Ignorado durante warmup")
                        return@startScanning
                    }
                    BleDebug.log("DOCENTE-SCAN", "Payload recibido ${payload.toHexPreview()}")
                    val parsed = BlePacketCodec.decodeAny(payload)
                    if (parsed == null) {
                        BleDebug.log("DOCENTE-SCAN", "Payload no parseable")
                    }
                    val student = (parsed as? BlePacketCodec.ParsedPacket.Student)?.packet ?: return@startScanning
                    BleDebug.log(
                        "DOCENTE-SCAN",
                        "StudentAdv sigla=${student.sigla} grupo=${student.grupo} indice=${student.indice}"
                    )
                    val result = session.onScannedPayload(payload)
                    BleDebug.log("DOCENTE-SCAN", "Resultado session=$result")
                    if (result == TeacherBleAttendanceSession.ScanResult.MARCADO_PRESENTE) {
                        val ahora = bleClockStart.elapsedNow().inWholeMilliseconds
                        val ultimo = ultimoProcesadoPorIndice[student.indice]
                        if (ultimo != null && (ahora - ultimo) < 1200) {
                            BleDebug.log("DOCENTE-SCAN", "Ignorado por dedupe indice=${student.indice}")
                            return@startScanning
                        }
                        ultimoProcesadoPorIndice[student.indice] = ahora
                        bitmapDirty = true

                        val detalle = detalleAsistenciaModel.detallesAsistencia.value.firstOrNull {
                            it.bitmapIndexEstudiante == student.indice
                        }
                        if (detalle == null) {
                            BleDebug.log(
                                "DOCENTE-SCAN",
                                "No se encontro detalle para bitmapIndex=${student.indice}. Revisar indices de inscritos"
                            )
                            return@startScanning
                        }

                        if (detalleAsistenciaModel.esNuevaAsistencia.value) {
                            val detallesActuales = detalleAsistenciaModel.detallesAsistencia.value
                            val detallesActualizados = detallesActuales.map { item ->
                                if (item.estudianteId == detalle.estudianteId) {
                                    item.copy(estado = "PRESENTE")
                                } else {
                                    item
                                }
                            }
                            detalleAsistenciaModel.cargarDetallesTemporales(detallesActualizados)
                        } else {
                            detalleAsistenciaModel.actualizarEstado(asistenciaId, detalle.estudianteId, "PRESENTE")
                            detalleAsistenciaModel.cargarDetallesAsistencia(asistenciaId)
                        }
                        BleDebug.log(
                            "DOCENTE-SCAN",
                            "Marcado PRESENTE estudianteId=${detalle.estudianteId} bitmapIndex=${student.indice} presentes=${session.presentesCount()} bitmap=${session.currentBitmap().toHexPreview()}"
                        )
                        _bleEstado.value = "Detectados ${session.presentesCount()} presentes"
                    }
                },
                onError = { error ->
                    BleDebug.log("DOCENTE-SCAN", "Error scan: $error")
                    _bleEstado.value = error
                }
            )
        }

        batchEmitJob?.cancel()
        batchEmitJob = scope.launch {
            while (_bleActivo.value) {
                delay(BLE_BATCH_EMIT_MS)
                val currentSession = bleSession ?: continue
                if (!bitmapDirty && frozenEmissionPayloads.isNotEmpty()) continue

                val nextPayloads = currentSession.buildFragmentPayloads()
                frozenEmissionPayloads = nextPayloads
                emissionCursor = 0
                bitmapDirty = false

                BleDebug.log(
                    "DOCENTE-BATCH",
                    "Nuevo lote bitmap=${currentSession.currentBitmap().toHexPreview()} fragments=${nextPayloads.size}"
                )
            }
        }

        advertiseJob = scope.launch {
            while (_bleActivo.value) {
                val payloads = frozenEmissionPayloads
                if (payloads.isEmpty()) {
                    delay(BLE_FRAGMENT_ADV_MS)
                    continue
                }
                val payload = payloads[emissionCursor % payloads.size]
                emissionCursor = (emissionCursor + 1) % payloads.size
                val teacher = BlePacketCodec.decodeTeacherFragment(payload)
                if (teacher != null) {
                    BleDebug.log(
                        "DOCENTE-ADV",
                        "Emitiendo fragmento ark=${teacher.ark} len=${teacher.fragmentoBitmap.size} payload=${payload.toHexPreview()}"
                    )
                }
                bleTransceiver.startAdvertising(payload) { error ->
                    BleDebug.log("DOCENTE-ADV", "Error advertising: $error")
                    _bleEstado.value = error
                }
                delay(BLE_FRAGMENT_ADV_MS)
            }
        }

        return null
    }

    fun detenerBleDocente() {
        BleDebug.log("DOCENTE", "Deteniendo BLE docente")
        scanStartJob?.cancel()
        scanStartJob = null
        batchEmitJob?.cancel()
        batchEmitJob = null
        advertiseJob?.cancel()
        advertiseJob = null
        bleSession = null
        bleWarmupStart = null
        bitmapDirty = false
        frozenEmissionPayloads = emptyList()
        emissionCursor = 0
        ultimoProcesadoPorIndice.clear()
        bleTransceiver.stopAll()
        scope.launch {
            delay(BLE_CACHE_FLUSH_MS)
            bleTransceiver.stopAll()
        }
        _bleActivo.value = false
        _bleEstado.value = "BLE inactivo"
    }

    fun volver() {
        detenerBleDocente()
        view.irVolver()
    }

    fun destroy() {
        detenerBleDocente()
        scope.cancel()
    }
}
