package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

class EstudianteModel(
    val id: Long = 0,
    val carnetIdentidad: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    private val db: AttendanceDatabase? = null
) {
    private fun requireDb(): AttendanceDatabase = db ?: error("EstudianteModel sin db")

    fun insertar(estudiante: EstudianteModel): Long {
        val database = requireDb()

        return try {
            database.estudianteQueries.transactionWithResult {
                database.estudianteQueries.insertEstudiante(
                    carnet_identidad = estudiante.carnetIdentidad.toLong(),
                    nombre = estudiante.nombre,
                    apellido = estudiante.apellido
                )

                val id = database.estudianteQueries.getLastInsertId().executeAsOne()

                if (id == 0L) {
                    throw IllegalStateException("El INSERT no generó un ID válido")
                }

                val verificacion = database.estudianteQueries.getEstudianteById(id).executeAsOneOrNull()
                if (verificacion == null) {
                    throw IllegalStateException("El estudiante no se insertó correctamente")
                }
                id
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun obtenerPorId(id: Long): EstudianteModel? {
        val database = requireDb()
        return database.estudianteQueries.getEstudianteById(id)
            .executeAsOneOrNull()
            ?.let {
                EstudianteModel(
                    id = it.id,
                    carnetIdentidad = it.carnet_identidad.toInt(),
                    nombre = it.nombre,
                    apellido = it.apellido
                )
            }
    }

    fun obtenerPorCarnet(carnet: Int): EstudianteModel? {
        val database = requireDb()
        return database.estudianteQueries.getEstudianteByCarnet(carnet.toLong())
            .executeAsOneOrNull()
            ?.let {
                EstudianteModel(
                    id = it.id,
                    carnetIdentidad = it.carnet_identidad.toInt(),
                    nombre = it.nombre,
                    apellido = it.apellido
                )
            }
    }

    fun obtenerTodos(): List<EstudianteModel> {
        val database = requireDb()
        return database.estudianteQueries.getAllEstudiantes()
            .executeAsList()
            .map {
                EstudianteModel(
                    id = it.id,
                    carnetIdentidad = it.carnet_identidad.toInt(),
                    nombre = it.nombre,
                    apellido = it.apellido
                )
            }
    }

    fun obtenerPorMateria(materiaId: Long): List<EstudianteModel> {
        val database = requireDb()

        val resultados = database.inscritoQueries.getAlumnosByMateria(materiaId)
            .executeAsList()

        val lista = resultados.map {
            EstudianteModel(
                id = it.id,
                carnetIdentidad = it.carnet_identidad.toInt(),
                nombre = it.nombre,
                apellido = it.apellido
            )
        }
        return lista
    }

    fun actualizar(estudiante: EstudianteModel) {
        val database = requireDb()
        database.estudianteQueries.updateEstudiante(
            nombre = estudiante.nombre,
            apellido = estudiante.apellido,
            id = estudiante.id
        )
    }

    fun eliminar(id: Long) {
        val database = requireDb()
        database.estudianteQueries.deleteEstudiante(id)
    }
}
