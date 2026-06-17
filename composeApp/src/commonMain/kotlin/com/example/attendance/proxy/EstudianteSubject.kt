package com.example.attendance.proxy

import com.example.attendance.model.EstudianteModel

interface EstudianteSubject {
    fun crear(estudiante: EstudianteModel): Long
    fun obtenerPorCarnet(carnet: Long): EstudianteModel?
}
