package com.example.attendance.proxy

import com.example.attendance.model.EstudianteModel

class EstudianteModelProxy(
    service: EstudianteModel
) : EstudianteSubject {
    private val realService: EstudianteModel = service

    override fun crear(estudiante: EstudianteModel): Long {
        return if (checkAccess(estudiante.carnetIdentidad)) {
            realService.crear(estudiante)
        } else {
            realService.obtenerPorCarnet(estudiante.carnetIdentidad)?.id ?: 0
        }
    }

    override fun obtenerPorCarnet(carnet: Long): EstudianteModel? {
        return realService.obtenerPorCarnet(carnet)
    }

    fun checkAccess(carnet: Long): Boolean {
        return realService.obtenerPorCarnet(carnet) == null
    }
}
