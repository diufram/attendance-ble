package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

class DocenteModel(
    val carnetIdentidad: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    private val db: AttendanceDatabase? = null
) {
    private fun requireDb(): AttendanceDatabase = db ?: error("DocenteModel sin db")

    fun insertar(docente: DocenteModel) {
        val database = requireDb()
        database.docenteQueries.insertDocente(
            carnet_identidad = docente.carnetIdentidad.toLong(),
            nombre = docente.nombre,
            apellido = docente.apellido
        )
    }

    fun obtener(carnet: Int): DocenteModel? {
        val database = requireDb()
        return database.docenteQueries.getDocente(carnet.toLong())
            .executeAsOneOrNull()
            ?.let {
                DocenteModel(
                    carnetIdentidad = it.carnet_identidad.toInt(),
                    nombre = it.nombre,
                    apellido = it.apellido
                )
            }
    }

    fun obtenerTodos(): List<DocenteModel> {
        val database = requireDb()
        return database.docenteQueries.getAllDocentes()
            .executeAsList()
            .map {
                DocenteModel(
                    carnetIdentidad = it.carnet_identidad.toInt(),
                    nombre = it.nombre,
                    apellido = it.apellido
                )
            }
    }

    fun actualizar(docente: DocenteModel) {
        val database = requireDb()
        database.docenteQueries.updateDocente(
            nombre = docente.nombre,
            apellido = docente.apellido,
            carnet_identidad = docente.carnetIdentidad.toLong()
        )
    }

    fun eliminar(carnet: Int) {
        val database = requireDb()
        database.docenteQueries.deleteDocente(carnet.toLong())
    }
}
