package com.example.attendance.validador

class ValidadorCompuesto(private val validadores: List<Validador>) : Validador {
    override fun esValido(): Boolean = validadores.all { it.esValido() }
}
