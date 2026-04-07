package com.example.attendance.ble

class TeacherBleAttendanceSession(
    private val sigla: String,
    private val grupo: String,
    totalEstudiantes: Int,
) {
    private val bitmap = BleBitmap.create(totalEstudiantes)
    private var fragmentCursor = 0
    private val maxIndice = totalEstudiantes - 1

    enum class ScanResult {
        IGNORADO,
        INVALIDO,
        MARCADO_PRESENTE,
        YA_MARCADO,
    }

    fun onScannedPayload(payload: ByteArray): ScanResult {
        return when (val parsed = BlePacketCodec.decodeAny(payload)) {
            is BlePacketCodec.ParsedPacket.Student -> {
                val packet = parsed.packet
                if (packet.sigla != sigla || packet.grupo != grupo) return ScanResult.IGNORADO
                if (packet.indice !in 0..maxIndice) return ScanResult.INVALIDO
                val before = BleBitmap.isBitSet(bitmap, packet.indice)
                BleBitmap.setBit(bitmap, packet.indice)
                BleDebug.log(
                    "SESSION-DOCENTE",
                    "indice=${packet.indice} before=$before bitmap=${bitmap.toHexPreview()}"
                )
                if (before) ScanResult.YA_MARCADO else ScanResult.MARCADO_PRESENTE
            }

            is BlePacketCodec.ParsedPacket.TeacherFragment -> ScanResult.IGNORADO
            null -> ScanResult.INVALIDO
        }
    }

    fun currentBitmap(): ByteArray = bitmap.copyOf()

    fun setPresence(indice: Int, presente: Boolean): Boolean {
        if (indice !in 0..maxIndice) return false
        if (presente) {
            BleBitmap.setBit(bitmap, indice)
        } else {
            BleBitmap.clearBit(bitmap, indice)
        }
        BleDebug.log(
            "SESSION-DOCENTE",
            "setPresence indice=$indice presente=$presente bitmap=${bitmap.toHexPreview()}"
        )
        return true
    }

    fun presentesCount(): Int {
        var total = 0
        for (indice in 0..maxIndice) {
            if (BleBitmap.isBitSet(bitmap, indice)) total++
        }
        return total
    }

    fun buildFragmentPayloads(): List<ByteArray> {
        val fragments = BlePacketCodec.createTeacherFragments(sigla, grupo, bitmap)
        return fragments.map(BlePacketCodec::encodeTeacherFragment)
    }

    fun nextFragmentPayload(): ByteArray {
        val payloads = buildFragmentPayloads()
        val payload = payloads[fragmentCursor % payloads.size]
        fragmentCursor = (fragmentCursor + 1) % payloads.size
        return payload
    }
}

class StudentBleAttendanceSession(
    private val sigla: String,
    private val grupo: String,
    private val indice: Int,
) {
    private val fragmentosPorArk = mutableMapOf<Int, ByteArray>()

    var confirmado: Boolean = false
        private set

    enum class ScanResult {
        IGNORADO,
        INVALIDO,
        FRAGMENTO_RECIBIDO,
        CONFIRMADO,
    }

    init {
        require(indice in 0..255) { "indice debe estar entre 0 y 255" }
    }

    fun studentPayload(): ByteArray {
        return BlePacketCodec.encodeStudent(
            BlePacketCodec.StudentAdvertisement(
                sigla = sigla,
                grupo = grupo,
                indice = indice,
            )
        )
    }

    fun onScannedPayload(payload: ByteArray): ScanResult {
        val parsed = BlePacketCodec.decodeAny(payload) ?: return ScanResult.INVALIDO
        val teacherPacket = (parsed as? BlePacketCodec.ParsedPacket.TeacherFragment)?.packet
            ?: return ScanResult.IGNORADO

        if (teacherPacket.sigla != sigla || teacherPacket.grupo != grupo) return ScanResult.IGNORADO

        fragmentosPorArk[teacherPacket.ark] = teacherPacket.fragmentoBitmap
        BleDebug.log(
            "SESSION-ESTUDIANTE",
            "ark=${teacherPacket.ark} fragment=${teacherPacket.fragmentoBitmap.toHexPreview()} arks=${fragmentosPorArk.keys.sorted()}"
        )

        if (isOwnBitPresent()) {
            confirmado = true
            BleDebug.log("SESSION-ESTUDIANTE", "bit propio confirmado indice=$indice")
            return ScanResult.CONFIRMADO
        }

        return ScanResult.FRAGMENTO_RECIBIDO
    }

    fun receivedArks(): Set<Int> = fragmentosPorArk.keys

    fun reconstructedBitmap(): ByteArray {
        val fragmentos = fragmentosPorArk
            .entries
            .sortedBy { it.key }
            .map { entry ->
                BlePacketCodec.TeacherFragmentAdvertisement(
                    sigla = sigla,
                    grupo = grupo,
                    ark = entry.key,
                    fragmentoBitmap = entry.value,
                )
            }
        return BlePacketCodec.reassembleBitmap(fragmentos)
    }

    fun reset() {
        fragmentosPorArk.clear()
        confirmado = false
    }

    private fun isOwnBitPresent(): Boolean {
        val byteIndex = indice / 8
        val fragmentArk = (byteIndex / BlePacketCodec.TEACHER_FRAGMENT_MAX_BYTES) + 1
        val fragment = fragmentosPorArk[fragmentArk] ?: return false
        val offsetInFragment = byteIndex % BlePacketCodec.TEACHER_FRAGMENT_MAX_BYTES
        if (offsetInFragment !in fragment.indices) return false
        val bitIndex = indice % 8
        val byteValue = fragment[offsetInFragment].toInt() and 0xFF
        return (byteValue and (1 shl bitIndex)) != 0
    }
}
