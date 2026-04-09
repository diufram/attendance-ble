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

data class BleConfirmacion(
    val nombreMateria: String,
    val sigla: String,
    val grupo: String,
)

sealed class BleEstado {
    data object Inactivo : BleEstado()
    data class Escuchando(val mensaje: String) : BleEstado()
    data class Recibiendo(val progreso: Int) : BleEstado()
    data class Confirmado(val confirmacion: BleConfirmacion) : BleEstado()
    data class Error(val mensaje: String) : BleEstado()
}

class BleStudentService {
    private val bleTransceiver = BleTransceiver()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _estado = MutableStateFlow<BleEstado>(BleEstado.Inactivo)
    val estado: StateFlow<BleEstado> = _estado.asStateFlow()

    private var session: StudentBleAttendanceSession? = null
    private var advertiseJob: Job? = null
    private var scanJob: Job? = null
    private var warmupStart: kotlin.time.TimeMark? = null

    fun iniciarMarcadoAsistencia(
        nombreMateria: String,
        sigla: String,
        grupo: String,
        bitmapIndex: Int,
    ) {
        detenerMarcadoAsistencia()

        val nuevaSession = StudentBleAttendanceSession(sigla, grupo, bitmapIndex)
        session = nuevaSession
        warmupStart = TimeSource.Monotonic.markNow()
        _estado.value = BleEstado.Escuchando("Escuchando confirmacion BLE...")

        prepararBle()

        scanJob = scope.launch {
            delay(BleConfig.BLE_CACHE_FLUSH_MS)
            iniciarScanning(nombreMateria, sigla, grupo)
        }

        advertiseJob = scope.launch {
            while (estaActivo()) {
                val payload = session?.studentPayload() ?: break
                bleTransceiver.startAdvertising(payload) { error ->
                    _estado.value = BleEstado.Error(error)
                }
                delay(700)
            }
        }
    }

    fun detenerMarcadoAsistencia() {
        scanJob?.cancel()
        scanJob = null
        advertiseJob?.cancel()
        advertiseJob = null
        session = null
        warmupStart = null
        bleTransceiver.stopAll()

        scope.launch {
            delay(BleConfig.BLE_CACHE_FLUSH_MS)
            bleTransceiver.stopAll()
        }

        _estado.value = BleEstado.Inactivo
    }

    fun estaActivo(): Boolean = session != null

    private fun prepararBle() {
        bleTransceiver.stopAll()
        scope.launch {
            delay(BleConfig.BLE_CACHE_FLUSH_MS)
            bleTransceiver.stopAll()
        }
    }

    private fun iniciarScanning(nombreMateria: String, sigla: String, grupo: String) {
        val currentSession = session ?: return

        bleTransceiver.startScanning(
            onPayload = { payload ->
                if (enWarmup()) return@startScanning

                procesarPayload(payload, currentSession, nombreMateria, sigla, grupo)
            },
            onError = { error ->
                _estado.value = BleEstado.Error(error)
            }
        )
    }

    private fun enWarmup(): Boolean {
        val warmup = warmupStart ?: return false
        return warmup.elapsedNow().inWholeMilliseconds < BleConfig.BLE_WARMUP_MS
    }

    private fun procesarPayload(
        payload: ByteArray,
        session: StudentBleAttendanceSession,
        nombreMateria: String,
        sigla: String,
        grupo: String,
    ) {
        when (session.onScannedPayload(payload)) {
            StudentBleAttendanceSession.ScanResult.CONFIRMADO -> {
                _estado.value = BleEstado.Confirmado(
                    BleConfirmacion(nombreMateria, sigla, grupo)
                )
                detenerMarcadoAsistencia()
            }

            StudentBleAttendanceSession.ScanResult.FRAGMENTO_RECIBIDO -> {
                val progreso = session.receivedArks().size
                _estado.value = BleEstado.Recibiendo(progreso)
            }

            StudentBleAttendanceSession.ScanResult.INVALIDO,
            StudentBleAttendanceSession.ScanResult.IGNORADO -> Unit
        }
    }
}