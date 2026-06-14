package com.example.attendance.validador

import com.example.attendance.validador.reglas.EsRequerido
import com.example.attendance.validador.reglas.SoloCaracteres
import com.example.attendance.validador.reglas.SoloDigitos

object ValidadorFactory {
    fun login(carnet: String): Validador =
        ValidadorCompuesto(listOf(EsRequerido(carnet), SoloDigitos(carnet)))

    fun registro(carnet: String, nombre: String, apellido: String): Validador =
        ValidadorCompuesto(listOf(
            EsRequerido(carnet),
            SoloDigitos(carnet),
            EsRequerido(nombre),
            SoloCaracteres(nombre),
            EsRequerido(apellido),
            SoloCaracteres(apellido),
        ))
}
