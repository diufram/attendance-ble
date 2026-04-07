package com.example.attendance.ble

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BleAttendanceSessionTest {
    @Test
    fun teacherSession_marksPresenceAndIgnoresDuplicates() {
        val teacher = TeacherBleAttendanceSession(
            sigla = "INF-321",
            grupo = "SA",
            totalEstudiantes = 200,
        )

        val p30 = BlePacketCodec.encodeStudent(BlePacketCodec.StudentAdvertisement("INF-321", "SA", 30))
        val p96 = BlePacketCodec.encodeStudent(BlePacketCodec.StudentAdvertisement("INF-321", "SA", 96))

        assertEquals(TeacherBleAttendanceSession.ScanResult.MARCADO_PRESENTE, teacher.onScannedPayload(p30))
        assertEquals(TeacherBleAttendanceSession.ScanResult.MARCADO_PRESENTE, teacher.onScannedPayload(p96))
        assertEquals(TeacherBleAttendanceSession.ScanResult.YA_MARCADO, teacher.onScannedPayload(p30))
        assertEquals(2, teacher.presentesCount())

        val bitmap = teacher.currentBitmap()
        assertTrue(BleBitmap.isBitSet(bitmap, 30))
        assertTrue(BleBitmap.isBitSet(bitmap, 96))
        assertFalse(BleBitmap.isBitSet(bitmap, 31))
    }

    @Test
    fun studentSession_confirmsAfterReceivingTeacherFragments() {
        val teacher = TeacherBleAttendanceSession(
            sigla = "INF-321",
            grupo = "SA",
            totalEstudiantes = 200,
        )
        val student = StudentBleAttendanceSession(
            sigla = "INF-321",
            grupo = "SA",
            indice = 96,
        )

        val studentAdv = student.studentPayload()
        teacher.onScannedPayload(studentAdv)

        val teacherFragments = teacher.buildFragmentPayloads()
        val shuffled = listOf(teacherFragments[2], teacherFragments[0], teacherFragments[1])

        assertEquals(StudentBleAttendanceSession.ScanResult.FRAGMENTO_RECIBIDO, student.onScannedPayload(shuffled[0]))
        assertEquals(StudentBleAttendanceSession.ScanResult.FRAGMENTO_RECIBIDO, student.onScannedPayload(shuffled[1]))
        assertEquals(StudentBleAttendanceSession.ScanResult.CONFIRMADO, student.onScannedPayload(shuffled[2]))
        assertTrue(student.confirmado)
    }

    @Test
    fun studentSession_ignoresOtherPackets() {
        val student = StudentBleAttendanceSession(
            sigla = "INF-321",
            grupo = "SA",
            indice = 10,
        )

        val otherStudent = BlePacketCodec.encodeStudent(
            BlePacketCodec.StudentAdvertisement("INF-321", "SA", 33)
        )
        assertEquals(StudentBleAttendanceSession.ScanResult.IGNORADO, student.onScannedPayload(otherStudent))

        val otherMateriaTeacher = BlePacketCodec.encodeTeacherFragment(
            BlePacketCodec.TeacherFragmentAdvertisement(
                sigla = "MAT-100",
                grupo = "SB",
                ark = 1,
                fragmentoBitmap = byteArrayOf(0x01),
            )
        )
        assertEquals(StudentBleAttendanceSession.ScanResult.IGNORADO, student.onScannedPayload(otherMateriaTeacher))
        assertFalse(student.confirmado)
    }
}
