package com.example.attendance.validador.reglas

import com.example.attendance.validador.Validador

class SoloCaracteres(private val valor: String) : Validador {
    override fun esValido(): Boolean = valor.matches(Regex("^[a-zA-Z ]+$"))
}
