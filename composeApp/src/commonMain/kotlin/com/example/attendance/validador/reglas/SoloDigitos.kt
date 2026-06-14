package com.example.attendance.validador.reglas

import com.example.attendance.validador.Validador

class SoloDigitos(private val valor: String) : Validador {
    override fun esValido(): Boolean = valor.all { it.isDigit() }
}
