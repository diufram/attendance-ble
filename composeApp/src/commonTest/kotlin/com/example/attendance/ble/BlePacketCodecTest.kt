package com.example.attendance.ble

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BlePacketCodecTest {
    @Test
    fun studentPacket_roundTrip_ok() {
        val encoded = BlePacketCodec.encodeStudent(
            BlePacketCodec.StudentAdvertisement(
                sigla = "INF-321",
                grupo = "SA",
                indice = 30,
            )
        )

        assertEquals(BlePacketCodec.STUDENT_PAYLOAD_BYTES, encoded.size)

        val decoded = BlePacketCodec.decodeStudent(encoded)
        assertEquals("INF-321", decoded?.sigla)
        assertEquals("SA", decoded?.grupo)
        assertEquals(30, decoded?.indice)
    }

    @Test
    fun teacherPacket_roundTrip_ok() {
        val fragment = byteArrayOf(1, 2, 3, 4, 5)
        val encoded = BlePacketCodec.encodeTeacherFragment(
            BlePacketCodec.TeacherFragmentAdvertisement(
                sigla = "INF-321",
                grupo = "SA",
                ark = 2,
                fragmentoBitmap = fragment,
            )
        )

        val decoded = BlePacketCodec.decodeTeacherFragment(encoded)
        assertEquals("INF-321", decoded?.sigla)
        assertEquals("SA", decoded?.grupo)
        assertEquals(2, decoded?.ark)
        assertContentEquals(fragment, decoded?.fragmentoBitmap)
    }

    @Test
    fun decodeAny_detects_teacher_and_student() {
        val studentPayload = BlePacketCodec.encodeStudent(
            BlePacketCodec.StudentAdvertisement("INF-321", "SA", 7)
        )
        val teacherPayload = BlePacketCodec.encodeTeacherFragment(
            BlePacketCodec.TeacherFragmentAdvertisement("INF-321", "SA", 1, byteArrayOf(0x01))
        )

        assertIs<BlePacketCodec.ParsedPacket.Student>(BlePacketCodec.decodeAny(studentPayload))
        assertIs<BlePacketCodec.ParsedPacket.TeacherFragment>(BlePacketCodec.decodeAny(teacherPayload))
    }

    @Test
    fun fragmentAndReassemble_bitmap_200_students() {
        val bitmap = ByteArray(25) { it.toByte() }

        val fragments = BlePacketCodec.createTeacherFragments(
            sigla = "INF-321",
            grupo = "SA",
            bitmap = bitmap,
        )

        assertEquals(3, fragments.size)
        assertEquals(1, fragments[0].ark)
        assertEquals(2, fragments[1].ark)
        assertEquals(3, fragments[2].ark)
        assertEquals(12, fragments[0].fragmentoBitmap.size)
        assertEquals(12, fragments[1].fragmentoBitmap.size)
        assertEquals(1, fragments[2].fragmentoBitmap.size)

        val rebuilt = BlePacketCodec.reassembleBitmap(fragments)
        assertContentEquals(bitmap, rebuilt)
    }

    @Test
    fun bitmap_setAndCheckBits_ok() {
        val bitmap = BleBitmap.create(200)

        BleBitmap.setBit(bitmap, 0)
        BleBitmap.setBit(bitmap, 95)
        BleBitmap.setBit(bitmap, 96)
        BleBitmap.setBit(bitmap, 191)
        BleBitmap.setBit(bitmap, 192)

        assertTrue(BleBitmap.isBitSet(bitmap, 0))
        assertTrue(BleBitmap.isBitSet(bitmap, 95))
        assertTrue(BleBitmap.isBitSet(bitmap, 96))
        assertTrue(BleBitmap.isBitSet(bitmap, 191))
        assertTrue(BleBitmap.isBitSet(bitmap, 192))
        assertFalse(BleBitmap.isBitSet(bitmap, 199))
    }
}
