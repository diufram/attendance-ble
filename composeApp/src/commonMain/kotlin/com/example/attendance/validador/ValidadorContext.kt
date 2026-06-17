package com.example.attendance.validador

class ValidadorContext(private val validadorInicial: Validador) {
    private var validador: Validador = validadorInicial

    val mensajeError: String
        get() = validador.mensajeError

    fun esValido(): Boolean = validador.esValido()

    fun cambiarEstrategia(nuevoValidador: Validador) {
        this.validador = nuevoValidador
    }
}
