package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

data class Estudiante(
    val id: Long = 0,
    val carnetIdentidad: Int,
    val nombre: String,
    val apellido: String
) {
    companion object {
        fun insertar(db: AttendanceDatabase, estudiante: Estudiante): Long {
            db.estudianteQueries.insertEstudiante(
                carnet_identidad = estudiante.carnetIdentidad.toLong(),
                nombre = estudiante.nombre,
                apellido = estudiante.apellido
            )
            return db.estudianteQueries.getLastInsertId().executeAsOne()
        }

        fun obtenerPorId(db: AttendanceDatabase, id: Long): Estudiante? {
            return db.estudianteQueries.getEstudianteById(id)
                .executeAsOneOrNull()
                ?.let {
                    Estudiante(
                        id = it.id,
                        carnetIdentidad = it.carnet_identidad.toInt(),
                        nombre = it.nombre,
                        apellido = it.apellido
                    )
                }
        }

        fun obtenerPorCarnet(db: AttendanceDatabase, carnet: Int): Estudiante? {
            return db.estudianteQueries.getEstudianteByCarnet(carnet.toLong())
                .executeAsOneOrNull()
                ?.let {
                    Estudiante(
                        id = it.id,
                        carnetIdentidad = it.carnet_identidad.toInt(),
                        nombre = it.nombre,
                        apellido = it.apellido
                    )
                }
        }

        fun obtenerTodos(db: AttendanceDatabase): List<Estudiante> {
            return db.estudianteQueries.getAllEstudiantes()
                .executeAsList()
                .map {
                    Estudiante(
                        id = it.id,
                        carnetIdentidad = it.carnet_identidad.toInt(),
                        nombre = it.nombre,
                        apellido = it.apellido
                    )
                }
        }

        fun obtenerPorMateria(db: AttendanceDatabase, materiaId: Long): List<Estudiante> {
            return db.inscritoQueries.getAlumnosByMateria(materiaId)
                .executeAsList()
                .map {
                    Estudiante(
                        id = it.id,
                        carnetIdentidad = it.carnet_identidad.toInt(),
                        nombre = it.nombre,
                        apellido = it.apellido
                    )
                }
        }

        fun actualizar(db: AttendanceDatabase, estudiante: Estudiante) {
            db.estudianteQueries.updateEstudiante(
                nombre = estudiante.nombre,
                apellido = estudiante.apellido,
                id = estudiante.id
            )
        }

        fun eliminar(db: AttendanceDatabase, id: Long) {
            db.estudianteQueries.deleteEstudiante(id)
        }
    }
}
