package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

data class Asistencia(
    val id: Long = 0,
    val materiaId: Long,
    val fecha: String
) {
    companion object {
        fun insertar(db: AttendanceDatabase, asistencia: Asistencia): Long {
            db.asistenciaQueries.insertAsistencia(
                materia_id = asistencia.materiaId,
                fecha = asistencia.fecha
            )
            return db.asistenciaQueries.getLastInsertId()
                .executeAsOne()
        }

        fun insertarConFechaActual(db: AttendanceDatabase, materiaId: Long): Long {
            db.asistenciaQueries.insertAsistenciaNow(
                materia_id = materiaId
            )
            return db.asistenciaQueries.getLastInsertId()
                .executeAsOne()
        }

        fun obtenerPorId(db: AttendanceDatabase, id: Long): Asistencia? {
            return db.asistenciaQueries.getAsistenciaById(id)
                .executeAsOneOrNull()
                ?.let {
                    Asistencia(
                        id = it.id,
                        materiaId = it.materia_id,
                        fecha = it.fecha
                    )
                }
        }

        fun obtenerTodos(db: AttendanceDatabase): List<Asistencia> {
            return db.asistenciaQueries.getAllAsistencias()
                .executeAsList()
                .map {
                    Asistencia(
                        id = it.id,
                        materiaId = it.materia_id,
                        fecha = it.fecha
                    )
                }
        }

        fun obtenerPorMateria(db: AttendanceDatabase, materiaId: Long): List<Asistencia> {
            return db.asistenciaQueries.getAsistenciasByMateria(materiaId)
                .executeAsList()
                .map {
                    Asistencia(
                        id = it.id,
                        materiaId = it.materia_id,
                        fecha = it.fecha
                    )
                }
        }

        fun actualizar(db: AttendanceDatabase, asistencia: Asistencia) {
            db.asistenciaQueries.updateAsistencia(
                fecha = asistencia.fecha,
                id = asistencia.id
            )
        }

        fun eliminar(db: AttendanceDatabase, id: Long) {
            db.detalleAsistenciaQueries.deleteDetalleByAsistencia(id)
            db.asistenciaQueries.deleteAsistencia(id)
        }
    }
}
