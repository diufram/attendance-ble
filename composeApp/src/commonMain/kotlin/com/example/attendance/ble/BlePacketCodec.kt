package com.example.attendance.ble

import kotlin.math.ceil

object BlePacketCodec {
    const val SIGLA_BYTES = 7
    const val GRUPO_BYTES = 2
    const val INDEX_BYTES = 1
    const val STUDENT_PAYLOAD_BYTES = SIGLA_BYTES + GRUPO_BYTES + INDEX_BYTES
    const val TEACHER_HEADER_BYTES = SIGLA_BYTES + GRUPO_BYTES + 1
    const val BLE_CUSTOM_DATA_BYTES = 22
    const val TEACHER_FRAGMENT_MAX_BYTES = BLE_CUSTOM_DATA_BYTES - TEACHER_HEADER_BYTES

    data class StudentAdvertisement(
        val sigla: String,
        val grupo: String,
        val indice: Int,
    )

    data class TeacherFragmentAdvertisement(
        val sigla: String,
        val grupo: String,
        val ark: Int,
        val fragmentoBitmap: ByteArray,
    )

    sealed interface ParsedPacket {
        data class Student(val packet: StudentAdvertisement) : ParsedPacket
        data class TeacherFragment(val packet: TeacherFragmentAdvertisement) : ParsedPacket
    }

    fun encodeStudent(packet: StudentAdvertisement): ByteArray {
        require(packet.indice in 0..255) { "indice debe estar entre 0 y 255" }

        val siglaBytes = packet.sigla.toFixedFieldBytes(SIGLA_BYTES, "sigla")
        val grupoBytes = packet.grupo.toFixedFieldBytes(GRUPO_BYTES, "grupo")

        return ByteArray(STUDENT_PAYLOAD_BYTES).also { payload ->
            siglaBytes.copyInto(payload, destinationOffset = 0)
            grupoBytes.copyInto(payload, destinationOffset = SIGLA_BYTES)
            payload[SIGLA_BYTES + GRUPO_BYTES] = packet.indice.toByte()
        }
    }

    fun decodeStudent(payload: ByteArray): StudentAdvertisement? {
        if (payload.size != STUDENT_PAYLOAD_BYTES) return null

        val sigla = payload.copyOfRange(0, SIGLA_BYTES).decodeField()
        val grupo = payload.copyOfRange(SIGLA_BYTES, SIGLA_BYTES + GRUPO_BYTES).decodeField()
        val indice = payload[SIGLA_BYTES + GRUPO_BYTES].toInt() and 0xFF

        if (!sigla.isValidSigla() || !grupo.isValidGrupo()) return null

        return StudentAdvertisement(sigla = sigla, grupo = grupo, indice = indice)
    }

    fun encodeTeacherFragment(packet: TeacherFragmentAdvertisement): ByteArray {
        require(packet.ark in 1..255) { "ark debe estar entre 1 y 255" }
        require(packet.fragmentoBitmap.isNotEmpty()) { "fragmentoBitmap no puede estar vacio" }
        require(packet.fragmentoBitmap.size <= TEACHER_FRAGMENT_MAX_BYTES) {
            "fragmentoBitmap excede $TEACHER_FRAGMENT_MAX_BYTES bytes"
        }

        val siglaBytes = packet.sigla.toFixedFieldBytes(SIGLA_BYTES, "sigla")
        val grupoBytes = packet.grupo.toFixedFieldBytes(GRUPO_BYTES, "grupo")

        return ByteArray(TEACHER_HEADER_BYTES + packet.fragmentoBitmap.size).also { payload ->
            siglaBytes.copyInto(payload, destinationOffset = 0)
            grupoBytes.copyInto(payload, destinationOffset = SIGLA_BYTES)
            payload[SIGLA_BYTES + GRUPO_BYTES] = packet.ark.toByte()
            packet.fragmentoBitmap.copyInto(payload, destinationOffset = TEACHER_HEADER_BYTES)
        }
    }

    fun decodeTeacherFragment(payload: ByteArray): TeacherFragmentAdvertisement? {
        if (payload.size <= TEACHER_HEADER_BYTES) return null
        if (payload.size > BLE_CUSTOM_DATA_BYTES) return null

        val sigla = payload.copyOfRange(0, SIGLA_BYTES).decodeField()
        val grupo = payload.copyOfRange(SIGLA_BYTES, SIGLA_BYTES + GRUPO_BYTES).decodeField()
        val ark = payload[SIGLA_BYTES + GRUPO_BYTES].toInt() and 0xFF
        val fragmento = payload.copyOfRange(TEACHER_HEADER_BYTES, payload.size)

        if (!sigla.isValidSigla() || !grupo.isValidGrupo() || ark == 0 || fragmento.isEmpty()) return null

        return TeacherFragmentAdvertisement(
            sigla = sigla,
            grupo = grupo,
            ark = ark,
            fragmentoBitmap = fragmento,
        )
    }

    fun decodeAny(payload: ByteArray): ParsedPacket? {
        decodeTeacherFragment(payload)?.let { return ParsedPacket.TeacherFragment(it) }
        decodeStudent(payload)?.let { return ParsedPacket.Student(it) }
        return null
    }

    fun createTeacherFragments(sigla: String, grupo: String, bitmap: ByteArray): List<TeacherFragmentAdvertisement> {
        require(bitmap.isNotEmpty()) { "bitmap no puede estar vacio" }

        val totalFragments = ceil(bitmap.size.toDouble() / TEACHER_FRAGMENT_MAX_BYTES).toInt()
        require(totalFragments in 1..255) { "cantidad de fragmentos fuera de rango" }

        return bitmap
            .asList()
            .chunked(TEACHER_FRAGMENT_MAX_BYTES)
            .mapIndexed { index, fragmentBytes ->
                TeacherFragmentAdvertisement(
                    sigla = sigla,
                    grupo = grupo,
                    ark = index + 1,
                    fragmentoBitmap = fragmentBytes.toByteArray(),
                )
            }
    }

    fun reassembleBitmap(fragments: Collection<TeacherFragmentAdvertisement>): ByteArray {
        if (fragments.isEmpty()) return byteArrayOf()

        val sigla = fragments.first().sigla
        val grupo = fragments.first().grupo
        require(fragments.all { it.sigla == sigla && it.grupo == grupo }) {
            "todos los fragmentos deben pertenecer a la misma materia y grupo"
        }

        return fragments
            .sortedBy { it.ark }
            .flatMap { it.fragmentoBitmap.asList() }
            .toByteArray()
    }

    private fun String.toFixedFieldBytes(maxBytes: Int, fieldName: String): ByteArray {
        val trimmed = trim()
        require(trimmed.isNotEmpty()) { "$fieldName no puede estar vacio" }

        val bytes = trimmed.encodeToByteArray()
        require(bytes.size <= maxBytes) { "$fieldName excede $maxBytes bytes" }

        return ByteArray(maxBytes).also { fixed ->
            bytes.copyInto(fixed)
        }
    }

    private fun ByteArray.decodeField(): String {
        val firstZero = indexOf(0)
        val slice = if (firstZero == -1) this else copyOfRange(0, firstZero)
        return slice.decodeToString().trim()
    }

    private fun String.isValidSigla(): Boolean {
        if (length !in 3..7) return false
        return all { ch -> ch.isUpperCase() || ch.isDigit() || ch == '-' }
    }

    private fun String.isValidGrupo(): Boolean {
        if (length != 2) return false
        return all { ch -> ch.isUpperCase() || ch.isDigit() }
    }
}

object BleBitmap {
    fun bytesForStudents(cantidadEstudiantes: Int): Int {
        require(cantidadEstudiantes in 1..256) { "cantidadEstudiantes debe estar entre 1 y 256" }
        return (cantidadEstudiantes + 7) / 8
    }

    fun create(cantidadEstudiantes: Int): ByteArray {
        return ByteArray(bytesForStudents(cantidadEstudiantes))
    }

    fun setBit(bitmap: ByteArray, indice: Int) {
        require(indice in 0..255) { "indice debe estar entre 0 y 255" }
        val byteIndex = indice / 8
        require(byteIndex in bitmap.indices) { "indice fuera de rango para el bitmap actual" }
        val bitIndex = indice % 8
        val current = bitmap[byteIndex].toInt() and 0xFF
        bitmap[byteIndex] = (current or (1 shl bitIndex)).toByte()
    }

    fun clearBit(bitmap: ByteArray, indice: Int) {
        require(indice in 0..255) { "indice debe estar entre 0 y 255" }
        val byteIndex = indice / 8
        require(byteIndex in bitmap.indices) { "indice fuera de rango para el bitmap actual" }
        val bitIndex = indice % 8
        val current = bitmap[byteIndex].toInt() and 0xFF
        bitmap[byteIndex] = (current and (1 shl bitIndex).inv()).toByte()
    }

    fun isBitSet(bitmap: ByteArray, indice: Int): Boolean {
        require(indice in 0..255) { "indice debe estar entre 0 y 255" }
        val byteIndex = indice / 8
        if (byteIndex !in bitmap.indices) return false
        val bitIndex = indice % 8
        val current = bitmap[byteIndex].toInt() and 0xFF
        return (current and (1 shl bitIndex)) != 0
    }
}
