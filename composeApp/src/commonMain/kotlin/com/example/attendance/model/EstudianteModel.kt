package com.example.attendance.model

import com.example.attendance.db.Database

class EstudianteModel(
    val id: Long = 0,
    val nombre: String = "",
    val apellido: String = "",
    val carnetIdentidad: Long = 0,
    private val db: Database? = null
) {
    private fun requireDb(): Database = db ?: error("EstudianteModel sin db")

    fun crear(estudiante: EstudianteModel): Long {
        val database = requireDb()

        return try {
            database.estudianteQueries.transactionWithResult {
                database.estudianteQueries.insertEstudiante(
                    carnet_identidad = estudiante.carnetIdentidad,
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

    fun obtenerPorCarnet(carnet: Long): EstudianteModel? {
        val database = requireDb()
        return database.estudianteQueries.getEstudianteByCarnet(carnet)
            .executeAsOneOrNull()
            ?.let {
                EstudianteModel(
                    id = it.id,
                    carnetIdentidad = it.carnet_identidad,
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
                carnetIdentidad = it.carnet_identidad,
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
}
