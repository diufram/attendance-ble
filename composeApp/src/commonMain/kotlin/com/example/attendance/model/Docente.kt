package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

data class Docente(
    val carnetIdentidad: Int,
    val nombre: String,
    val apellido: String
) {
    companion object {
        fun insertar(db: AttendanceDatabase, docente: Docente) {
            db.docenteQueries.insertDocente(
                carnet_identidad = docente.carnetIdentidad.toLong(),
                nombre = docente.nombre,
                apellido = docente.apellido
            )
        }

        fun obtener(db: AttendanceDatabase, carnet: Int): Docente? {
            return db.docenteQueries.getDocente(carnet.toLong())
                .executeAsOneOrNull()
                ?.let {
                    Docente(
                        carnetIdentidad = it.carnet_identidad.toInt(),
                        nombre = it.nombre,
                        apellido = it.apellido
                    )
                }
        }

        fun obtenerTodos(db: AttendanceDatabase): List<Docente> {
            return db.docenteQueries.getAllDocentes()
                .executeAsList()
                .map {
                    Docente(
                        carnetIdentidad = it.carnet_identidad.toInt(),
                        nombre = it.nombre,
                        apellido = it.apellido
                    )
                }
        }

        fun actualizar(db: AttendanceDatabase, docente: Docente) {
            db.docenteQueries.updateDocente(
                nombre = docente.nombre,
                apellido = docente.apellido,
                carnet_identidad = docente.carnetIdentidad.toLong()
            )
        }

        fun eliminar(db: AttendanceDatabase, carnet: Int) {
            db.docenteQueries.deleteDocente(carnet.toLong())
        }
    }
}
