package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

data class Estudiante(
    val carnetIdentidad: Int,
    val nombre: String,
    val apellido: String
) {
    companion object {
        fun insertar(db: AttendanceDatabase, estudiante: Estudiante) {
            db.estudianteQueries.insertEstudiante(
                carnet_identidad = estudiante.carnetIdentidad.toLong(),
                nombre = estudiante.nombre,
                apellido = estudiante.apellido
            )
        }

        fun obtener(db: AttendanceDatabase, carnet: Int): Estudiante? {
            return db.estudianteQueries.getEstudiante(carnet.toLong())
                .executeAsOneOrNull()
                ?.let {
                    Estudiante(
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
                carnet_identidad = estudiante.carnetIdentidad.toLong()
            )
        }

        fun eliminar(db: AttendanceDatabase, carnet: Int) {
            db.estudianteQueries.deleteEstudiante(carnet.toLong())
        }
    }
}