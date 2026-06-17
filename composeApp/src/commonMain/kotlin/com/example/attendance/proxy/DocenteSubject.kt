package com.example.attendance.proxy

import com.example.attendance.model.DocenteModel

interface DocenteSubject {
    fun crear(docente: DocenteModel): Long
    fun obtenerPorCarnet(carnet: Long): DocenteModel?
}
