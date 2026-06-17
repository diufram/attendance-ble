package com.example.attendance.proxy

import com.example.attendance.model.DocenteModel

class DocenteModelProxy(
    service: DocenteModel
) : DocenteSubject {
    private val realService: DocenteModel = service

    override fun crear(docente: DocenteModel): Long {
        return if (checkAccess(docente.carnetIdentidad)) {
            realService.crear(docente)
        } else {
            realService.obtenerPorCarnet(docente.carnetIdentidad)?.id ?: 0
        }
    }

    override fun obtenerPorCarnet(carnet: Long): DocenteModel? {
        return realService.obtenerPorCarnet(carnet)
    }

    fun checkAccess(carnet: Long): Boolean {
        return realService.obtenerPorCarnet(carnet) == null
    }
}
