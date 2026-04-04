package com.example.attendance.controller

import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DocenteController(private val db: AttendanceDatabase) {

    private val _docente = MutableStateFlow<Docente?>(null)
    val docente: StateFlow<Docente?> = _docente

    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materias: StateFlow<List<Materia>> = _materias

    private val _alumnos = MutableStateFlow<List<Estudiante>>(emptyList())
    val alumnos: StateFlow<List<Estudiante>> = _alumnos

    private val _asistencias = MutableStateFlow<List<Asistencia>>(emptyList())
    val asistencias: StateFlow<List<Asistencia>> = _asistencias

    private val _detalleAsistencia = MutableStateFlow<List<DetalleAsistencia>>(emptyList())
    val detalleAsistencia: StateFlow<List<DetalleAsistencia>> = _detalleAsistencia

    // ========== INGRESO ==========

    fun ingresar(carnet: Int) {
        var docente = Docente.obtener(db, carnet)
        if (docente == null) {
            Docente.insertar(db, Docente(carnet, "", ""))
            docente = Docente.obtener(db, carnet)
        }
        _docente.value = docente
        cargarMaterias()
    }

    fun actualizarPerfil(nombre: String, apellido: String) {
        val docenteActual = _docente.value ?: return
        val actualizado = docenteActual.copy(nombre = nombre, apellido = apellido)
        Docente.actualizar(db, actualizado)
        _docente.value = actualizado
    }

    // ========== MATERIAS ==========

    fun crearMateria(sigla: String, nombre: String, grupo: String) {
        val docenteActual = _docente.value ?: return
        Materia.insertar(db, Materia(
            sigla = sigla,
            nombre = nombre,
            grupo = grupo,
            docenteId = docenteActual.carnetIdentidad
        ))
        cargarMaterias()
    }

    fun actualizarMateria(materia: Materia) {
        Materia.actualizar(db, materia)
        cargarMaterias()
    }

    fun eliminarMateria(id: Long) {
        Materia.eliminar(db, id)
        cargarMaterias()
    }

    fun cargarMaterias() {
        val docenteActual = _docente.value ?: return
        _materias.value = Materia.obtenerPorDocente(db, docenteActual.carnetIdentidad)
    }

    // ========== ALUMNOS ==========

    fun cargarAlumnos(materiaId: Long) {
        _alumnos.value = Estudiante.obtenerPorMateria(db, materiaId)
    }

    fun inscribirAlumno(materiaId: Long, carnet: Int, nombre: String, apellido: String) {
        if (Estudiante.obtener(db, carnet) == null) {
            Estudiante.insertar(db, Estudiante(carnet, nombre, apellido))
        }
        Inscrito.insertar(db, Inscrito(materiaId = materiaId, estudianteId = carnet))
        cargarAlumnos(materiaId)
    }

    fun desinscribirAlumno(materiaId: Long, estudianteId: Int) {
        Inscrito.eliminarPorMateriaEstudiante(db, materiaId, estudianteId)
        cargarAlumnos(materiaId)
    }

    fun cargarAlumnosDesdeCSV(materiaId: Long, contenidoCsv: String) {
        val lineas = contenidoCsv.lines().drop(1)
        for (linea in lineas) {
            if (linea.isBlank()) continue
            val campos = linea.split(",").map { it.trim() }
            if (campos.size >= 3) {
                val carnet = campos[0].toIntOrNull() ?: continue
                val nombre = campos[1]
                val apellido = campos[2]
                inscribirAlumno(materiaId, carnet, nombre, apellido)
            }
        }
    }

    // ========== ASISTENCIA ==========

    fun crearAsistencia(materiaId: Long, fecha: String): Long {
        val id = Asistencia.insertar(db, Asistencia(materiaId = materiaId, fecha = fecha))
        val alumnos = Estudiante.obtenerPorMateria(db, materiaId)
        for (alumno in alumnos) {
            DetalleAsistencia.insertar(db, DetalleAsistencia(
                asistenciaId = id,
                estudianteId = alumno.carnetIdentidad,
                estado = "FALTA"
            ))
        }
        cargarAsistencias(materiaId)
        return id
    }

    fun marcarPresente(asistenciaId: Long, estudianteId: Int) {
        DetalleAsistencia.actualizarEstado(db, asistenciaId, estudianteId, "PRESENTE")
        cargarDetalleAsistencia(asistenciaId)
    }

    fun marcarFalta(asistenciaId: Long, estudianteId: Int) {
        DetalleAsistencia.actualizarEstado(db, asistenciaId, estudianteId, "FALTA")
        cargarDetalleAsistencia(asistenciaId)
    }

    fun cargarAsistencias(materiaId: Long) {
        _asistencias.value = Asistencia.obtenerPorMateria(db, materiaId)
    }

    fun cargarDetalleAsistencia(asistenciaId: Long) {
        _detalleAsistencia.value = DetalleAsistencia.obtenerPorAsistencia(db, asistenciaId)
    }

    fun eliminarAsistencia(id: Long, materiaId: Long) {
        Asistencia.eliminar(db, id)
        cargarAsistencias(materiaId)
    }
}