package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

data class DetalleAsistencia(
    val id: Long = 0,
    val asistenciaId: Long,
    val estudianteId: Long,
    val estado: String = "FALTA",
    val carnetEstudiante: Int = 0,
    val nombreEstudiante: String = "",
    val apellidoEstudiante: String = ""
) {
    companion object {
        fun insertar(db: AttendanceDatabase, detalle: DetalleAsistencia) {
            db.detalleAsistenciaQueries.insertDetalle(
                asistencia_id = detalle.asistenciaId,
                estudiante_id = detalle.estudianteId,
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
                        estudianteId = it.estudiante_id,
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
                        estudianteId = it.estudiante_id,
                        estado = it.estado,
                        carnetEstudiante = it.carnet_identidad.toInt(),
                        nombreEstudiante = it.nombre,
                        apellidoEstudiante = it.apellido
                    )
                }
        }

        fun actualizarEstado(
            db: AttendanceDatabase,
            asistenciaId: Long,
            estudianteId: Long,
            estado: String
        ) {
            db.detalleAsistenciaQueries.updateEstado(
                estado = estado,
                asistencia_id = asistenciaId,
                estudiante_id = estudianteId
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
