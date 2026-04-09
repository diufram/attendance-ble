package com.example.attendance.controller

import com.example.attendance.ble.BleConfig
import com.example.attendance.ble.BleDebug
import com.example.attendance.ble.BlePacketCodec
import com.example.attendance.ble.BleTransceiver
import com.example.attendance.ble.TeacherBleAttendanceSession
import com.example.attendance.ble.toHexPreview
import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.AsistenciaDetalleModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.navigation.AppNavigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

class AsistenciaDetalleController(
    private val asistenciaModel: AsistenciaModel,
    private val estudianteModel: EstudianteModel,
    private val asistenciaDetalleModel: AsistenciaDetalleModel,
    private val navigator: AppNavigation,
) {
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

    fun guardarAsistencia(materiaId: Long, asistenciaId: Long, esNueva: Boolean): Boolean {
        return try {
            val detallesActuales = asistenciaDetalleModel.detallesAsistencia.value

            if (esNueva) {
                // Crear nueva asistencia con sus detalles
                val nuevaAsistenciaId = asistenciaModel.insertarConFechaActual(materiaId)
                detallesActuales.forEach { detalle ->
                    asistenciaDetalleModel.insertar(
                        AsistenciaDetalleModel(
                            asistenciaId = nuevaAsistenciaId,
                            carnetIdentidad = detalle.carnetIdentidad,
                            estado = detalle.estado
                        )
                    )
                }
                asistenciaDetalleModel.cargarDetallesAsistencia(nuevaAsistenciaId)
            } else {
                // Actualizar detalles de asistencia existente
                detallesActuales.forEach { detalle ->
                    asistenciaDetalleModel.actualizarEstado(
                        asistenciaId = asistenciaId,
                        carnetIdentidad = detalle.carnetIdentidad,
                        estado = detalle.estado
                    )
                }
                asistenciaDetalleModel.cargarDetallesAsistencia(asistenciaId)
            }

            asistenciaModel.cargarAsistenciasMateria(materiaId)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun alternarEstado(carnetIdentidad: Long, estadoActual: String) {
        val nuevoEstado = if (estadoActual == "PRESENTE") "FALTA" else "PRESENTE"

        // Siempre actualizar solo en memoria, sin guardar en BD
        val detallesActuales = asistenciaDetalleModel.detallesAsistencia.value
        val detallesActualizados = detallesActuales.map { detalle ->
            if (detalle.carnetIdentidad == carnetIdentidad) {
                detalle.copy(estado = nuevoEstado)
            } else {
                detalle
            }
        }
        asistenciaDetalleModel.cargarDetallesTemporales(detallesActualizados)

        // Actualizar sesión BLE si está activa
        val detalle = detallesActuales.firstOrNull { it.carnetIdentidad == carnetIdentidad }
        val bitmapIndex = detalle?.bitmapIndexEstudiante
        if (bitmapIndex != null) {
            val changed = bleSession?.setPresence(bitmapIndex, nuevoEstado == "PRESENTE") == true
            if (changed) bitmapDirty = true
        }
    }

    fun iniciarBleDocente(sigla: String, grupo: String, asistenciaId: Long, esNueva: Boolean): String? {
        if (sigla.isBlank() || grupo.isBlank()) return "No hay materia activa para BLE"

        val detalles = asistenciaDetalleModel.detallesAsistencia.value
        if (detalles.isEmpty()) return "No hay estudiantes para iniciar BLE"

        BleDebug.log(
            "DOCENTE",
            "Iniciando BLE sigla=$sigla grupo=$grupo asistenciaId=$asistenciaId estudiantes=${detalles.size}"
        )
        BleDebug.log(
            "DOCENTE",
            "Mapa bitmapIndex->estudianteId=" + detalles.joinToString { "${it.bitmapIndexEstudiante}:${it.carnetIdentidad}" }
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
            delay(BleConfig.BLE_CACHE_FLUSH_MS)
            bleTransceiver.stopAll()
        }

        scanStartJob?.cancel()
        scanStartJob = scope.launch {
            delay(BleConfig.BLE_CACHE_FLUSH_MS)
            bleTransceiver.startScanning(
                onPayload = { payload ->
                    val warmup = bleWarmupStart
                    if (warmup != null && warmup.elapsedNow().inWholeMilliseconds < BleConfig.BLE_WARMUP_MS) {
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

                        val detalle = asistenciaDetalleModel.detallesAsistencia.value.firstOrNull {
                            it.bitmapIndexEstudiante == student.indice
                        }
                        if (detalle == null) {
                            BleDebug.log(
                                "DOCENTE-SCAN",
                                "No se encontro detalle para bitmapIndex=${student.indice}. Revisar indices de inscritos"
                            )
                            return@startScanning
                        }

                        // Usar el parámetro esNueva en lugar de leer del model
                        if (esNueva) {
                            val detallesActuales = asistenciaDetalleModel.detallesAsistencia.value
                            val detallesActualizados = detallesActuales.map { item ->
                                if (item.carnetIdentidad == detalle.carnetIdentidad) {
                                    item.copy(estado = "PRESENTE")
                                } else {
                                    item
                                }
                            }
                            asistenciaDetalleModel.cargarDetallesTemporales(detallesActualizados)
                        } else {
                            asistenciaDetalleModel.actualizarEstado(asistenciaId, detalle.carnetIdentidad, "PRESENTE")
                            asistenciaDetalleModel.cargarDetallesAsistencia(asistenciaId)
                        }
                        BleDebug.log(
                            "DOCENTE-SCAN",
                            "Marcado PRESENTE carnet=${detalle.carnetIdentidad} bitmapIndex=${student.indice} presentes=${session.presentesCount()} bitmap=${session.currentBitmap().toHexPreview()}"
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
                delay(BleConfig.BLE_BATCH_EMIT_MS)
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
                    delay(BleConfig.BLE_FRAGMENT_ADV_MS)
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
                delay(BleConfig.BLE_FRAGMENT_ADV_MS)
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
            delay(BleConfig.BLE_CACHE_FLUSH_MS)
            bleTransceiver.stopAll()
        }
        _bleActivo.value = false
        _bleEstado.value = "BLE inactivo"
    }

    fun volver() {
        detenerBleDocente()
        navigator.volver()
    }
}