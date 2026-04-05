package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

data class Materia(
    val id: Long = 0,
    val sigla: String,
    val nombre: String,
    val grupo: String,
    val periodo: String,
    val docenteNombre: String,
    val docenteId: Int
) {
    companion object {
        fun insertar(db: AttendanceDatabase, materia: Materia) {
            db.materiaQueries.insertMateria(
                sigla = materia.sigla,
                nombre = materia.nombre,
                grupo = materia.grupo,
                periodo = materia.periodo,
                docente_nombre = materia.docenteNombre,
                docente_id = materia.docenteId.toLong()
            )
        }

        fun obtenerPorId(db: AttendanceDatabase, id: Long): Materia? {
            return db.materiaQueries.getMateriaById(id)
                .executeAsOneOrNull()
                ?.let {
                    Materia(
                        id = it.id,
                        sigla = it.sigla,
                        nombre = it.nombre,
                        grupo = it.grupo,
                        periodo = it.periodo,
                        docenteNombre = it.docente_nombre,
                        docenteId = it.docente_id.toInt()
                    )
                }
        }

        fun obtenerTodos(db: AttendanceDatabase): List<Materia> {
            return db.materiaQueries.getAllMaterias()
                .executeAsList()
                .map {
                    Materia(
                        id = it.id,
                        sigla = it.sigla,
                        nombre = it.nombre,
                        grupo = it.grupo,
                        periodo = it.periodo,
                        docenteNombre = it.docente_nombre,
                        docenteId = it.docente_id.toInt()
                    )
                }
        }

        fun obtenerPorDocente(db: AttendanceDatabase, docenteId: Int): List<Materia> {
            return db.materiaQueries.getMateriasByDocente(docenteId.toLong())
                .executeAsList()
                .map {
                    Materia(
                        id = it.id,
                        sigla = it.sigla,
                        nombre = it.nombre,
                        grupo = it.grupo,
                        periodo = it.periodo,
                        docenteNombre = it.docente_nombre,
                        docenteId = it.docente_id.toInt()
                    )
                }
        }

        fun obtenerPorFormacion(db: AttendanceDatabase, sigla: String, grupo: String, periodo: String): Materia? {
            return db.materiaQueries.getMateriaByFormacion(sigla, grupo, periodo)
                .executeAsOneOrNull()
                ?.let {
                    Materia(
                        id = it.id,
                        sigla = it.sigla,
                        nombre = it.nombre,
                        grupo = it.grupo,
                        periodo = it.periodo,
                        docenteNombre = it.docente_nombre,
                        docenteId = it.docente_id.toInt()
                    )
                }
        }

        fun obtenerPorEstudiante(db: AttendanceDatabase, carnet: Int): List<Materia> {
            return db.inscritoQueries.getMateriasByEstudiante(carnet.toLong())
                .executeAsList()
                .map {
                    Materia(
                        id = it.id,
                        sigla = it.sigla,
                        nombre = it.nombre,
                        grupo = it.grupo,
                        periodo = it.periodo,
                        docenteNombre = it.docente_nombre,
                        docenteId = it.docente_id.toInt()
                    )
                }
        }

        fun actualizar(db: AttendanceDatabase, materia: Materia) {
            db.materiaQueries.updateMateria(
                sigla = materia.sigla,
                nombre = materia.nombre,
                grupo = materia.grupo,
                periodo = materia.periodo,
                docente_nombre = materia.docenteNombre,
                id = materia.id
            )
        }

        fun eliminar(db: AttendanceDatabase, id: Long) {
            db.materiaQueries.deleteMateria(id)
        }
    }
}
