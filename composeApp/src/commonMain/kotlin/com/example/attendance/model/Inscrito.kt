package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

data class Inscrito(
    val id: Long = 0,
    val materiaId: Long,
    val estudianteId: Int
) {
    companion object {
        fun insertar(db: AttendanceDatabase, inscrito: Inscrito) {
            db.inscritoQueries.insertInscrito(
                materia_id = inscrito.materiaId,
                estudiante_id = inscrito.estudianteId.toLong()
            )
        }

        fun obtenerPorId(db: AttendanceDatabase, id: Long): Inscrito? {
            return db.inscritoQueries.getInscritoById(id)
                .executeAsOneOrNull()
                ?.let {
                    Inscrito(
                        id = it.id,
                        materiaId = it.materia_id,
                        estudianteId = it.estudiante_id.toInt()
                    )
                }
        }

        fun obtenerTodos(db: AttendanceDatabase): List<Inscrito> {
            return db.inscritoQueries.getAllInscritos()
                .executeAsList()
                .map {
                    Inscrito(
                        id = it.id,
                        materiaId = it.materia_id,
                        estudianteId = it.estudiante_id.toInt()
                    )
                }
        }

        fun eliminar(db: AttendanceDatabase, id: Long) {
            db.inscritoQueries.deleteInscrito(id)
        }

        fun eliminarPorMateriaEstudiante(db: AttendanceDatabase, materiaId: Long, estudianteId: Int) {
            db.inscritoQueries.deleteInscritoByMateriaEstudiante(
                materia_id = materiaId,
                estudiante_id = estudianteId.toLong()
            )
        }
    }
}