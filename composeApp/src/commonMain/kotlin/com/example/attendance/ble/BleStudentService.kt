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

    private val _bleEstado = MutableStateFlow("BLE inactivo")
    val bleEstado: StateFlow<String> = _bleEstado.asStateFlow()

    private val _bleActivoMateriaId = MutableStateFlow<Long?>(null)
    val bleActivoMateriaId: StateFlow<Long?> = _bleActivoMateriaId.asStateFlow()

    private val _bleConfirmacion = MutableStateFlow<BleConfirmacion?>(null)
    val bleConfirmacion: StateFlow<BleConfirmacion?> = _bleConfirmacion.asStateFlow()

    private var session: StudentBleAttendanceSession? = null
    private var advertiseJob: Job? = null
    private var scanJob: Job? = null
    private var warmupStart: kotlin.time.TimeMark? = null

    fun iniciarMarcadoAsistencia(
        materiaId: Long,
        nombreMateria: String,
        sigla: String,
        grupo: String,
        bitmapIndex: Int,
    ) {
        detenerMarcadoAsistencia()

        val nuevaSession = StudentBleAttendanceSession(sigla, grupo, bitmapIndex)
        session = nuevaSession
        _bleActivoMateriaId.value = materiaId
        _bleConfirmacion.value = null
        warmupStart = TimeSource.Monotonic.markNow()
        _estado.value = BleEstado.Escuchando("Escuchando confirmacion BLE...")
        _bleEstado.value = "Escuchando confirmacion BLE..."

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
                    _bleEstado.value = error
                }
                delay(700)
            }
        }
    }

    fun detenerMarcadoAsistencia() {
        detenerMarcadoAsistenciaInterno(resetEstado = true)
    }

    fun cerrarConfirmacionAsistencia() {
        _bleConfirmacion.value = null
        if (_bleActivoMateriaId.value == null) {
            _bleEstado.value = "BLE inactivo"
        }
    }

    private fun detenerMarcadoAsistenciaInterno(resetEstado: Boolean) {
        scanJob?.cancel()
        scanJob = null
        advertiseJob?.cancel()
        advertiseJob = null
        session = null
        warmupStart = null
        _bleActivoMateriaId.value = null
        bleTransceiver.stopAll()

        scope.launch {
            delay(BleConfig.BLE_CACHE_FLUSH_MS)
            bleTransceiver.stopAll()
        }

        _estado.value = BleEstado.Inactivo
        if (resetEstado) {
            _bleEstado.value = "BLE inactivo"
        }
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
                _bleEstado.value = error
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
                val confirmacion = BleConfirmacion(nombreMateria, sigla, grupo)
                _estado.value = BleEstado.Confirmado(confirmacion)
                _bleConfirmacion.value = confirmacion
                _bleEstado.value = "✓ Asistencia confirmada"
                detenerMarcadoAsistenciaInterno(resetEstado = false)
            }

            StudentBleAttendanceSession.ScanResult.FRAGMENTO_RECIBIDO -> {
                val progreso = session.receivedArks().size
                _estado.value = BleEstado.Recibiendo(progreso)
                _bleEstado.value = "Recibiendo confirmacion... ($progreso)"
            }

            StudentBleAttendanceSession.ScanResult.INVALIDO,
            StudentBleAttendanceSession.ScanResult.IGNORADO -> Unit
        }
    }
}
