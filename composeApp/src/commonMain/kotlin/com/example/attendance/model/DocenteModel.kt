package com.example.attendance.model

import com.example.attendance.db.Database

class DocenteModel(
    val id: Long = 0,
    val nombre: String = "",
    val apellido: String = "",
    val carnetIdentidad: Long = 0,
    private val db: Database? = null
) {
    private fun requireDb(): Database = db ?: error("DocenteModel sin db")

    fun crear(docente: DocenteModel): Long {
        val database = requireDb()
        database.docenteQueries.insertDocente(
            carnet_identidad = docente.carnetIdentidad,
            nombre = docente.nombre,
            apellido = docente.apellido
        )
        return database.docenteQueries.getLastInsertId().executeAsOne()
    }

    fun obtenerPorCarnet(carnet: Long): DocenteModel? {
        val database = requireDb()
        return database.docenteQueries.getDocenteByCarnet(carnet)
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
