package com.example.attendance.controller

import com.example.attendance.ble.BleConfig
import com.example.attendance.ble.BleDebug
import com.example.attendance.ble.BlePacketCodec
import com.example.attendance.ble.BleTransceiver
import com.example.attendance.ble.StudentBleAttendanceSession
import com.example.attendance.ble.toHexPreview
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel
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

class MateriaEstudianteController(
    private val materiaModel: MateriaModel,
    private val docenteModel: DocenteModel,
    private val inscritoModel: InscritoModel,
    private val navigator: AppNavigation,
) {
    data class BleConfirmacionUi(
        val nombreMateria: String,
        val sigla: String,
        val grupo: String,
    )

    private val bleTransceiver = BleTransceiver()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _bleEstado = MutableStateFlow("BLE inactivo")
    val bleEstado: StateFlow<String> = _bleEstado.asStateFlow()

    private val _bleActivoMateriaId = MutableStateFlow<Long?>(null)
    val bleActivoMateriaId: StateFlow<Long?> = _bleActivoMateriaId.asStateFlow()

    private val _bleConfirmacion = MutableStateFlow<BleConfirmacionUi?>(null)
    val bleConfirmacion: StateFlow<BleConfirmacionUi?> = _bleConfirmacion.asStateFlow()

    private var studentBleSession: StudentBleAttendanceSession? = null
    private var advertiseJob: Job? = null
    private var scanStartJob: Job? = null
    private var bleWarmupStart: kotlin.time.TimeMark? = null

    fun cerrarSesion() {
        detenerMarcadoAsistencia()
        materiaModel.limpiarMateriasEstudiante()
        navigator.irLoginView()
    }

    fun marcarAsistencia(materia: MateriaModel): String? {
        val bitmapIndex = materia.bitmapIndexEstudiante
            ?: return "Esta materia no tiene bitmap index asignado"

        BleDebug.log(
            "ESTUDIANTE",
            "Iniciando BLE materiaId=${materia.id} sigla=${materia.sigla} grupo=${materia.grupo} bitmapIndex=$bitmapIndex"
        )

        detenerMarcadoAsistencia()
        _bleConfirmacion.value = null

        val session = StudentBleAttendanceSession(
            sigla = materia.sigla,
            grupo = materia.grupo,
            indice = bitmapIndex
        )
        studentBleSession = session
        _bleActivoMateriaId.value = materia.id
        _bleEstado.value = "Escuchando confirmacion BLE..."
        bleWarmupStart = TimeSource.Monotonic.markNow()

        BleDebug.log("ESTUDIANTE", "Limpieza estricta de cache BLE previa a iniciar")
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
                        BleDebug.log("ESTUDIANTE-SCAN", "Ignorado durante warmup")
                        return@startScanning
                    }
                    BleDebug.log("ESTUDIANTE-SCAN", "Payload recibido ${payload.toHexPreview()}")
                    when (val parsed = BlePacketCodec.decodeAny(payload)) {
                        is BlePacketCodec.ParsedPacket.TeacherFragment -> {
                            BleDebug.log(
                                "ESTUDIANTE-SCAN",
                                "TeacherFragment sigla=${parsed.packet.sigla} grupo=${parsed.packet.grupo} ark=${parsed.packet.ark} len=${parsed.packet.fragmentoBitmap.size}"
                            )
                        }

                        is BlePacketCodec.ParsedPacket.Student -> {
                            BleDebug.log(
                                "ESTUDIANTE-SCAN",
                                "StudentAdv de otro dispositivo sigla=${parsed.packet.sigla} grupo=${parsed.packet.grupo} indice=${parsed.packet.indice}"
                            )
                        }

                        null -> BleDebug.log("ESTUDIANTE-SCAN", "Payload no parseable")
                    }

                    when (session.onScannedPayload(payload)) {
                        StudentBleAttendanceSession.ScanResult.CONFIRMADO -> {
                            val bitmap = session.reconstructedBitmap()
                            BleDebug.log(
                                "ESTUDIANTE-SCAN",
                                "CONFIRMADO bitmap=${bitmap.toHexPreview()} arks=${session.receivedArks().sorted()}"
                            )
                            _bleConfirmacion.value = BleConfirmacionUi(
                                nombreMateria = materia.nombre,
                                sigla = materia.sigla,
                                grupo = materia.grupo,
                            )
                            _bleEstado.value = "✓ Asistencia confirmada"
                            detenerMarcadoAsistencia(status = true)
                        }

                        StudentBleAttendanceSession.ScanResult.FRAGMENTO_RECIBIDO -> {
                            BleDebug.log(
                                "ESTUDIANTE-SCAN",
                                "Fragmento recibido arks=${session.receivedArks().sorted()}"
                            )
                            _bleEstado.value = "Recibiendo confirmacion... (${session.receivedArks().size})"
                        }

                        StudentBleAttendanceSession.ScanResult.INVALIDO,
                        StudentBleAttendanceSession.ScanResult.IGNORADO -> Unit
                    }
                },
                onError = { error ->
                    BleDebug.log("ESTUDIANTE-SCAN", "Error scan: $error")
                    _bleEstado.value = error
                }
            )
        }

        advertiseJob = scope.launch {
            while (_bleActivoMateriaId.value != null) {
                val payload = studentBleSession?.studentPayload() ?: break
                BleDebug.log("ESTUDIANTE-ADV", "Emitiendo student payload ${payload.toHexPreview()}")
                bleTransceiver.startAdvertising(payload) { error ->
                    BleDebug.log("ESTUDIANTE-ADV", "Error advertising: $error")
                    _bleEstado.value = error
                }
                delay(700)
            }
        }

        return null
    }

    fun detenerMarcadoAsistencia(status: Boolean = false) {
        BleDebug.log("ESTUDIANTE", "Deteniendo BLE estudiante")
        scanStartJob?.cancel()
        scanStartJob = null
        advertiseJob?.cancel()
        advertiseJob = null
        studentBleSession = null
        bleWarmupStart = null
        _bleActivoMateriaId.value = null
        bleTransceiver.stopAll()
        scope.launch {
            delay(BleConfig.BLE_CACHE_FLUSH_MS)
            bleTransceiver.stopAll()
        }
        if (!status) {
            _bleEstado.value = "BLE inactivo"
        }
    }

    fun cerrarConfirmacionAsistencia() {
        _bleConfirmacion.value = null
        if (_bleActivoMateriaId.value == null) {
            _bleEstado.value = "BLE inactivo"
        }
    }

    fun registrarMateriaDesdeQr(payload: String): String? {
        val carnet = materiaModel.estudianteActualCarnet.value
            ?: return "No hay estudiante activo"

        val qr = parsearQr(payload) ?: return "QR invalido"
        val bitmapIndex = qr.bitmapIndexPorCarnet[carnet]
            ?: return "Tu carnet no esta habilitado en este QR"

        val docenteCarnetInt = qr.docenteCarnet.toIntOrNull()
        val docenteCarnet = if (docenteCarnetInt != null) {
            var docente = docenteModel.obtenerPorCarnet(docenteCarnetInt)
            if (docente == null) {
                docenteModel.insertar(
                    DocenteModel(
                        carnetIdentidad = docenteCarnetInt.toLong(),
                        nombre = qr.docenteNombre.ifBlank { "" },
                        apellido = qr.docenteApellido.ifBlank { "" }
                    )
                )
                docente = docenteModel.obtenerPorCarnet(docenteCarnetInt)
            }
            docente?.carnetIdentidad?.toLong()
        } else null

        val materia = materiaModel.obtenerPorFormacion(
            sigla = qr.sigla,
            grupo = qr.grupo,
            periodo = qr.periodo
        ) ?: run {
            materiaModel.insertar(
                MateriaModel(
                    sigla = qr.sigla,
                    nombre = qr.nombre,
                    grupo = qr.grupo,
                    periodo = qr.periodo,
                    docenteCarnet = docenteCarnet
                )
            )
            materiaModel.obtenerPorFormacion(
                sigla = qr.sigla,
                grupo = qr.grupo,
                periodo = qr.periodo
            )
        } ?: return "No se pudo registrar la materia"

        return try {
            inscritoModel.guardarInscripcionConBitmap(
                materiaId = materia.id,
                carnetIdentidad = carnet.toLong(),
                bitmapIndex = bitmapIndex
            )
            materiaModel.cargarMateriasEstudiante(carnet)
            null
        } catch (_: Throwable) {
            "No se pudo guardar la inscripcion desde el QR"
        }
    }

    private fun parsearQr(payload: String): QrMateriaData? {
        val bloques = payload
            .trim()
            .split(';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (bloques.isEmpty()) return null

        val cabecera = bloques.first().split('|').map { it.trim() }
        if (cabecera.size < 7) return null

        val bitmapPorCarnet = mutableMapOf<Int, Int>()
        bloques.drop(1).forEach { entrada ->
            val partes = entrada.split('|').map { it.trim() }
            if (partes.size != 2) return null
            val carnet = partes[0].toIntOrNull() ?: return null
            val bitmapIndex = partes[1].toIntOrNull() ?: return null
            bitmapPorCarnet[carnet] = bitmapIndex
        }

        return QrMateriaData(
            nombre = cabecera[0],
            sigla = cabecera[1],
            grupo = cabecera[2],
            periodo = cabecera[3],
            docenteNombre = cabecera[4],
            docenteApellido = cabecera[5],
            docenteCarnet = cabecera[6],
            bitmapIndexPorCarnet = bitmapPorCarnet
        )
    }

    private data class QrMateriaData(
        val nombre: String,
        val sigla: String,
        val grupo: String,
        val periodo: String,
        val docenteNombre: String,
        val docenteApellido: String,
        val docenteCarnet: String,
        val bitmapIndexPorCarnet: Map<Int, Int>
    )
}
