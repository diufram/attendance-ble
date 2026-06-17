package com.example.attendance.controller

import com.example.attendance.model.DocenteModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.proxy.DocenteSubject
import com.example.attendance.proxy.EstudianteSubject
import com.example.attendance.validador.ApellidoValidador
import com.example.attendance.validador.CarnetValidador
import com.example.attendance.validador.NombreValidador
import com.example.attendance.validador.Validador
import com.example.attendance.validador.ValidadorContext
import com.example.attendance.view.LoginView
import com.example.attendance.view.RegistroView

class AuthController(
    private val docenteSubject: DocenteSubject,
    private val estudianteSubject: EstudianteSubject,
    private val loginView: LoginView,
    private val registroView: RegistroView,
) {
    fun onLogin(): String? {
        loginView.setSubmitting(true)

        val validadorCarnet: Validador = CarnetValidador(loginView.carnet.value)
        if (!validadorCarnet.esValido()) {
            loginView.setError(validadorCarnet.mensajeError)
            loginView.setSubmitting(false)
            return null
        }

        val carnet = loginView.carnet.value
        val carnetLong = carnet.toLong()

        val docente = docenteSubject.obtenerPorCarnet(carnetLong)
        if (docente != null) {
            loginView.setError("")
            loginView.setSubmitting(false)
            return "DOCENTE:$carnetLong"
        }

        val estudiante = estudianteSubject.obtenerPorCarnet(carnetLong)
        if (estudiante != null) {
            loginView.setError("")
            loginView.setSubmitting(false)
            return "ESTUDIANTE:$carnetLong"
        }

        loginView.setError("Usuario no encontrado. Regístrate primero")
        loginView.setSubmitting(false)
        return null
    }

    fun onRegistrar(): String? {
        registroView.setSubmitting(true)
        val esDocente = registroView.esDocente.value

        val context = ValidadorContext(CarnetValidador(registroView.carnet.value))

        val errorCarnet = if (context.esValido()) "" else context.mensajeError

        context.cambiarEstrategia(NombreValidador(registroView.nombre.value))
        val errorNombre = if (context.esValido()) "" else context.mensajeError

        context.cambiarEstrategia(ApellidoValidador(registroView.apellido.value))
        val errorApellido = if (context.esValido()) "" else context.mensajeError

        registroView.setErrorCarnet(errorCarnet)
        registroView.setErrorNombre(errorNombre)
        registroView.setErrorApellido(errorApellido)

        if (errorCarnet.isNotEmpty() || errorNombre.isNotEmpty() || errorApellido.isNotEmpty()) {
            registroView.setSubmitting(false)
            return null
        }

        val carnet = registroView.carnet.value
        val nombre = registroView.nombre.value
        val apellido = registroView.apellido.value

        val carnetLong = carnet.toLong()
        val nombreTrim = nombre.trim()
        val apellidoTrim = apellido.trim()

        if (esDocente) {
            val docente = DocenteModel(
                carnetIdentidad = carnetLong,
                nombre = nombreTrim,
                apellido = apellidoTrim
            )
            docenteSubject.crear(docente)
            docenteSubject.obtenerPorCarnet(carnetLong)
                ?: run {
                    registroView.setErrorCarnet("No se pudo registrar el docente")
                    registroView.setSubmitting(false)
                    return null
                }

            registroView.setErrorCarnet("")
            return "DOCENTE:$carnetLong"
        } else {
            val estudiante = EstudianteModel(
                carnetIdentidad = carnetLong,
                nombre = nombreTrim,
                apellido = apellidoTrim
            )
            estudianteSubject.crear(estudiante)
            estudianteSubject.obtenerPorCarnet(carnetLong)
                ?: run {
                    registroView.setErrorCarnet("No se pudo registrar el estudiante")
                    registroView.setSubmitting(false)
                    return null
                }

            registroView.setErrorCarnet("")
            return "ESTUDIANTE:$carnetLong"
        }
    }
}
