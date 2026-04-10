package com.example.attendance.ble

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

class BleTeacherService {
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

    fun iniciarBleDocente(
        sigla: String,
        grupo: String,
        totalEstudiantes: Int,
        presenciasIniciales: List<Pair<Int, Boolean>>,
        onMarcadoPresente: (bitmapIndex: Int, presentes: Int) -> Unit,
    ): String? {
        if (sigla.isBlank() || grupo.isBlank()) return "No hay materia activa para BLE"
        if (totalEstudiantes <= 0) return "No hay estudiantes para iniciar BLE"

        detenerBleDocente()

        val session = TeacherBleAttendanceSession(
            sigla = sigla,
            grupo = grupo,
            totalEstudiantes = totalEstudiantes,
        )

        presenciasIniciales.forEach { (idx, presente) ->
            session.setPresence(idx, presente)
        }

        bleSession = session
        _bleActivo.value = true
        _bleEstado.value = "Sesion BLE activa"
        bleWarmupStart = TimeSource.Monotonic.markNow()
        bitmapDirty = true
        frozenEmissionPayloads = emptyList()
        emissionCursor = 0

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
                        return@startScanning
                    }

                    val parsed = BlePacketCodec.decodeAny(payload)
                    val student = (parsed as? BlePacketCodec.ParsedPacket.Student)?.packet ?: return@startScanning
                    val result = session.onScannedPayload(payload)

                    if (result == TeacherBleAttendanceSession.ScanResult.MARCADO_PRESENTE) {
                        val ahora = bleClockStart.elapsedNow().inWholeMilliseconds
                        val ultimo = ultimoProcesadoPorIndice[student.indice]
                        if (ultimo != null && (ahora - ultimo) < 1200) {
                            return@startScanning
                        }

                        ultimoProcesadoPorIndice[student.indice] = ahora
                        bitmapDirty = true
                        val presentes = session.presentesCount()
                        onMarcadoPresente(student.indice, presentes)
                        _bleEstado.value = "Detectados $presentes presentes"
                    }
                },
                onError = { error ->
                    _bleEstado.value = error
                },
            )
        }

        batchEmitJob?.cancel()
        batchEmitJob = scope.launch {
            while (_bleActivo.value) {
                delay(BleConfig.BLE_BATCH_EMIT_MS)
                val currentSession = bleSession ?: continue
                if (!bitmapDirty && frozenEmissionPayloads.isNotEmpty()) continue

                frozenEmissionPayloads = currentSession.buildFragmentPayloads()
                emissionCursor = 0
                bitmapDirty = false
            }
        }

        advertiseJob?.cancel()
        advertiseJob = scope.launch {
            while (_bleActivo.value) {
                val payloads = frozenEmissionPayloads
                if (payloads.isEmpty()) {
                    delay(BleConfig.BLE_FRAGMENT_ADV_MS)
                    continue
                }

                val payload = payloads[emissionCursor % payloads.size]
                emissionCursor = (emissionCursor + 1) % payloads.size
                bleTransceiver.startAdvertising(payload) { error ->
                    _bleEstado.value = error
                }
                delay(BleConfig.BLE_FRAGMENT_ADV_MS)
            }
        }

        return null
    }

    fun actualizarPresencia(bitmapIndex: Int, presente: Boolean) {
        val changed = bleSession?.setPresence(bitmapIndex, presente) == true
        if (changed) bitmapDirty = true
    }

    fun detenerBleDocente() {
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
}
