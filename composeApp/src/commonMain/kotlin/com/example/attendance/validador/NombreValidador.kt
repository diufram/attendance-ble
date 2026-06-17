package com.example.attendance.validador

class NombreValidador(private val nombre: String) : Validador {
    override val mensajeError: String = "Ingresa un nombre válido"
    override fun esValido(): Boolean =
        nombre.isNotBlank() && nombre.matches(Regex("^[a-zA-Z ]+$"))
}
