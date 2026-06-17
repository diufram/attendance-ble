package com.example.attendance.validador

interface Validador {
    val mensajeError: String
    fun esValido(): Boolean
}
