package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

data class DetalleAsistencia(
    val id: Long = 0,
    val asistenciaId: Long,
    val estudianteId: Int,
    val estado: String = "FALTA",
    val nombreEstudiante: String = "",
    val apellidoEstudiante: String = ""
) {
    companion object {
        fun insertar(db: AttendanceDatabase, detalle: DetalleAsistencia) {
            db.detalleAsistenciaQueries.insertDetalle(
                asistencia_id = detalle.asistenciaId,
                estudiante_id = detalle.estudianteId.toLong(),
                estado = detalle.estado
            )
        }

        fun obtenerPorId(db: AttendanceDatabase, id: Long): DetalleAsistencia? {
            return db.detalleAsistenciaQueries.getDetalleById(id)
                .executeAsOneOrNull()
                ?.let {
                    DetalleAsistencia(
                        id = it.id,
                        asistenciaId = it.asistencia_id,
                        estudianteId = it.estudiante_id.toInt(),
                        estado = it.estado
                    )
                }
        }

        fun obtenerPorAsistencia(db: AttendanceDatabase, asistenciaId: Long): List<DetalleAsistencia> {
            return db.detalleAsistenciaQueries.getDetalleByAsistencia(asistenciaId)
                .executeAsList()
                .map {
                    DetalleAsistencia(
                        id = it.id,
                        asistenciaId = it.asistencia_id,
                        estudianteId = it.estudiante_id.toInt(),
                        estado = it.estado,
                        nombreEstudiante = it.nombre,
                        apellidoEstudiante = it.apellido
                    )
                }
        }

        fun actualizarEstado(
            db: AttendanceDatabase,
            asistenciaId: Long,
            estudianteId: Int,
            estado: String
        ) {
            db.detalleAsistenciaQueries.updateEstado(
                estado = estado,
                asistencia_id = asistenciaId,
                estudiante_id = estudianteId.toLong()
            )
        }

        fun eliminar(db: AttendanceDatabase, id: Long) {
            db.detalleAsistenciaQueries.deleteDetalle(id)
        }

        fun eliminarPorAsistencia(db: AttendanceDatabase, asistenciaId: Long) {
            db.detalleAsistenciaQueries.deleteDetalleByAsistencia(asistenciaId)
        }
    }
}