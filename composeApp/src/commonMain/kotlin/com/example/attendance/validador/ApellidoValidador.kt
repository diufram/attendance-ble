package com.example.attendance.validador

class ApellidoValidador(private val apellido: String) : Validador {
    override val mensajeError: String = "Ingresa un apellido válido"
    override fun esValido(): Boolean =
        apellido.isNotBlank() && apellido.matches(Regex("^[a-zA-Z ]+$"))
}
