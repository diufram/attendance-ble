package com.example.attendance.util

import com.example.attendance.model.DocenteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel

object QrUtils {

    data class QrMateriaData(
        val nombre: String,
        val sigla: String,
        val grupo: String,
        val periodo: String,
        val docenteNombre: String,
        val docenteApellido: String,
        val docenteCarnet: String,
        val bitmapIndexPorCarnet: Map<Int, Int>
    )

    fun construirPayloadQrMateria(
        materia: MateriaModel,
        docente: DocenteModel?,
        inscritos: List<Pair<Long, Int>>
    ): String {
        val docenteNombre = docente?.nombre ?: ""
        val docenteApellido = docente?.apellido ?: ""
        val docenteCarnet = docente?.carnetIdentidad?.toString() ?: ""
        
        val docenteInfo = "$docenteNombre|$docenteApellido|$docenteCarnet"

        val header = listOf(
            materia.nombre, 
            materia.sigla, 
            materia.grupo, 
            materia.periodo, 
            docenteInfo
        ).joinToString("|")

        val inscritosStr = inscritos
            .map { (carnet, bitmapIndex) -> "$carnet|$bitmapIndex" }
            .joinToString(";")

        return if (inscritosStr.isBlank()) header else "$header;$inscritosStr"
    }

    fun parsearQrMateria(payload: String): QrMateriaData? {
        val bloques = payload
            .trim()
            .split(';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (bloques.isEmpty()) return null

        val cabecera = bloques.first().split('|').map { it.trim() }
        if (cabecera.size < 7) return null

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
            docenteNombre = cabecera[4],
            docenteApellido = cabecera[5],
            docenteCarnet = cabecera[6],
            bitmapIndexPorCarnet = bitmapPorCarnet
        )
    }
}