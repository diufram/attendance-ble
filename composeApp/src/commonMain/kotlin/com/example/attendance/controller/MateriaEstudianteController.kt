package com.example.attendance.controller

import com.example.attendance.IMateriaEstudianteView
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel

class MateriaEstudianteController(
    private val materiaModel: MateriaModel,
    private val estudianteModel: EstudianteModel,
    private val inscritoModel: InscritoModel,
    private var view: IMateriaEstudianteView,
) {
    fun setView(view: IMateriaEstudianteView) {
        this.view = view
    }

    fun cerrarSesion() {
        materiaModel.limpiarMateriasEstudiante()
        view.irLogin()
    }

    fun registrarMateriaDesdeQr(payload: String): String? {
        val carnet = materiaModel.estudianteActualCarnet.value
            ?: return "No hay estudiante activo"

        val qr = parsearQr(payload) ?: return "QR invalido"
        val bitmapIndex = qr.bitmapIndexPorCarnet[carnet]
            ?: return "Tu carnet no esta habilitado en este QR"

        val estudiante = estudianteModel.obtenerPorCarnet(carnet)
            ?: return "No se encontro el estudiante"

        val materia = materiaModel.obtenerPorFormacion(
            sigla = qr.sigla,
            grupo = qr.grupo,
            periodo = qr.periodo
        ) ?: run {
            materiaModel.insertar(
                MateriaModel(
                    sigla = qr.sigla,
                    nombre = qr.nombre,
                    grupo = qr.grupo,
                    periodo = qr.periodo,
                    docenteNombre = "",
                    docenteId = 0
                )
            )
            materiaModel.obtenerPorFormacion(
                sigla = qr.sigla,
                grupo = qr.grupo,
                periodo = qr.periodo
            )
        } ?: return "No se pudo registrar la materia"

        return try {
            inscritoModel.guardarInscripcionConBitmap(
                materiaId = materia.id,
                estudianteId = estudiante.id,
                bitmapIndex = bitmapIndex
            )
            materiaModel.cargarMateriasEstudiante(carnet)
            null
        } catch (_: Throwable) {
            "No se pudo guardar la inscripcion desde el QR"
        }
    }

    private fun parsearQr(payload: String): QrMateriaData? {
        val bloques = payload
            .trim()
            .split(';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (bloques.isEmpty()) return null

        val cabecera = bloques.first().split('|').map { it.trim() }
        if (cabecera.size != 4) return null

        val bitmapPorCarnet = mutableMapOf<Int, Int>()
        bloques.drop(1).forEach { entrada ->
            val partes = entrada.split('|').map { it.trim() }
            if (partes.size != 2) return null
            val carnet = partes[0].toIntOrNull() ?: return null
            val bitmapIndex = partes[1].toIntOrNull() ?: return null
            bitmapPorCarnet[carnet] = bitmapIndex
        }

        return QrMateriaData(
            nombre = cabecera[0],
            sigla = cabecera[1],
            grupo = cabecera[2],
            periodo = cabecera[3],
            bitmapIndexPorCarnet = bitmapPorCarnet
        )
    }

    private data class QrMateriaData(
        val nombre: String,
        val sigla: String,
        val grupo: String,
        val periodo: String,
        val bitmapIndexPorCarnet: Map<Int, Int>
    )
}
