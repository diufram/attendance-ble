package com.example.attendance.controller

import com.example.attendance.model.DocenteModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.view.ILoginView
import com.example.attendance.view.IRegistroView

class AuthController(
    private val docenteModel: DocenteModel,
    private val estudianteModel: EstudianteModel,
    private val loginView: ILoginView,
    private val registroView: IRegistroView,
) {
    fun onLogin(): String? {
        loginView.setSubmitting(true)
        val carnetTexto = loginView.carnet.value
        if (carnetTexto.isBlank()) {
            loginView.setError("Ingresa un carnet")
            loginView.setSubmitting(false)
            return null
        }

        val carnet = carnetTexto.toLongOrNull()
        if (carnet == null) {
            loginView.setError("Carnet debe ser numérico")
            loginView.setSubmitting(false)
            return null
        }

        val docente = docenteModel.obtenerPorCarnet(carnet)
        if (docente != null) {
            loginView.setError("")
            loginView.setSubmitting(false)
            return "DOCENTE:$carnet"
        }

        val estudiante = estudianteModel.obtenerPorCarnet(carnet)
        if (estudiante != null) {
            loginView.setError("")
            loginView.setSubmitting(false)
            return "ESTUDIANTE:$carnet"
        }

        loginView.setError("Usuario no encontrado. Regístrate primero")
        loginView.setSubmitting(false)
        return null
    }

    fun onRegistrar(): String? {
        registroView.setSubmitting(true)
        val carnetTexto = registroView.carnet.value
        val nombre = registroView.nombre.value
        val apellido = registroView.apellido.value
        val esDocente = registroView.esDocente.value

        if (carnetTexto.isBlank()) {
            registroView.setError("Ingresa un carnet")
            registroView.setSubmitting(false)
            return null
        }
        if (nombre.isBlank()) {
            registroView.setError("Ingresa el nombre")
            registroView.setSubmitting(false)
            return null
        }
        if (apellido.isBlank()) {
            registroView.setError("Ingresa el apellido")
            registroView.setSubmitting(false)
            return null
        }

        val carnet = carnetTexto.toLongOrNull()
        if (carnet == null) {
            registroView.setError("Carnet debe ser numérico")
            registroView.setSubmitting(false)
            return null
        }

        if (esDocente) {
            docenteModel.obtenerPorCarnet(carnet) ?: run {
                docenteModel.crear(
                    DocenteModel(
                        carnetIdentidad = carnet,
                        nombre = nombre.trim(),
                        apellido = apellido.trim()
                    )
                )
                docenteModel.obtenerPorCarnet(carnet)
                    ?: run {
                        registroView.setError("No se pudo registrar el docente")
                        registroView.setSubmitting(false)
                        return null
                    }
            }

            registroView.setError("")
            return "DOCENTE:$carnet"
        } else {
            estudianteModel.obtenerPorCarnet(carnet) ?: run {
                estudianteModel.crear(
                    EstudianteModel(
                        carnetIdentidad = carnet,
                        nombre = nombre.trim(),
                        apellido = apellido.trim()
                    )
                )
                estudianteModel.obtenerPorCarnet(carnet)
                    ?: run {
                        registroView.setError("No se pudo registrar el estudiante")
                        registroView.setSubmitting(false)
                        return null
                    }
            }

            registroView.setError("")
            return "ESTUDIANTE:$carnet"
        }
    }
}
