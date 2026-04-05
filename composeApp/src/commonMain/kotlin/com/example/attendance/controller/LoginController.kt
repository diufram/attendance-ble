package com.example.attendance.controller

import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.model.Docente
import com.example.attendance.model.Estudiante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginController(
    private val db: AttendanceDatabase
) {
    data class LoginSuccess(
        val carnet: Int,
        val esDocente: Boolean
    )

    private val _errorMensaje = MutableStateFlow<String?>(null)
    val errorMensaje: StateFlow<String?> = _errorMensaje

    private val _navigationEvent = MutableStateFlow<LoginSuccess?>(null)
    val navigationEvent: StateFlow<LoginSuccess?> = _navigationEvent

    fun iniciarSesion(carnetInput: String, esDocente: Boolean): String? {
        val carnet = carnetInput.toIntOrNull() ?: return "Ingresa un carnet valido"

        if (esDocente) {
            var docente = Docente.obtener(db, carnet)
            if (docente == null) {
                Docente.insertar(db, Docente(carnet, "", ""))
                docente = Docente.obtener(db, carnet)
            }
            if (docente == null) return "No se pudo iniciar sesion"
        } else {
            var estudiante = Estudiante.obtenerPorCarnet(db, carnet)
            if (estudiante == null) {
                Estudiante.insertar(db, Estudiante(carnetIdentidad = carnet, nombre = "", apellido = ""))
                estudiante = Estudiante.obtenerPorCarnet(db, carnet)
            }
            if (estudiante == null) return "No se pudo iniciar sesion"
        }

        return null
    }

    fun ingresar(carnetInput: String, esDocente: Boolean) {
        val error = iniciarSesion(carnetInput, esDocente)
        if (error != null) {
            _errorMensaje.value = error
            _navigationEvent.value = null
            return
        }

        val carnet = carnetInput.toIntOrNull()
        if (carnet == null) {
            _errorMensaje.value = "Ingresa un carnet valido"
            _navigationEvent.value = null
            return
        }

        _errorMensaje.value = null
        _navigationEvent.value = LoginSuccess(carnet = carnet, esDocente = esDocente)
    }

    fun limpiarNavegacion() {
        _navigationEvent.value = null
    }

    fun limpiarError() {
        _errorMensaje.value = null
    }
}
