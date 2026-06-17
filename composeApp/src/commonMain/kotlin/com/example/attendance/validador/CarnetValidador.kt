package com.example.attendance.validador

class CarnetValidador(private val carnet: String) : Validador {
    override val mensajeError: String = "Ingresa un carnet válido"
    override fun esValido(): Boolean =
        carnet.isNotBlank() && carnet.all { it.isDigit() }
}
