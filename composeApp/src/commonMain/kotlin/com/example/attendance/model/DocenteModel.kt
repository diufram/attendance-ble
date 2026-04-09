package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

class DocenteModel(
    val id: Long = 0,
    val carnetIdentidad: Long = 0,
    val nombre: String = "",
    val apellido: String = "",
    private val db: AttendanceDatabase? = null
) {
    private fun requireDb(): AttendanceDatabase = db ?: error("DocenteModel sin db")

    fun insertar(docente: DocenteModel): Long {
        val database = requireDb()
        database.docenteQueries.insertDocente(
            carnet_identidad = docente.carnetIdentidad,
            nombre = docente.nombre,
            apellido = docente.apellido
        )
        return database.docenteQueries.getLastInsertId().executeAsOne()
    }

    fun obtenerPorId(id: Long): DocenteModel? {
        val database = requireDb()
        return database.docenteQueries.getDocenteById(id)
            .executeAsOneOrNull()
            ?.let {
                DocenteModel(
                    id = it.id,
                    carnetIdentidad = it.carnet_identidad,
                    nombre = it.nombre,
                    apellido = it.apellido
                )
            }
    }

    fun obtenerPorCarnet(carnet: Int): DocenteModel? {
        val database = requireDb()
        return database.docenteQueries.getDocenteByCarnet(carnet.toLong())
            .executeAsOneOrNull()
            ?.let {
                DocenteModel(
                    id = it.id,
                    carnetIdentidad = it.carnet_identidad,
                    nombre = it.nombre,
                    apellido = it.apellido
                )
            }
    }
}
