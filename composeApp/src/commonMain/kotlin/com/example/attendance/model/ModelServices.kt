package com.example.attendance.model

import com.example.attendance.db.AttendanceDatabase

class DocenteModel(private val db: AttendanceDatabase) {
    fun obtener(carnet: Int): Docente? = Docente.obtener(db, carnet)
    fun insertar(docente: Docente) = Docente.insertar(db, docente)
    fun actualizar(docente: Docente) = Docente.actualizar(db, docente)
}

class EstudianteModel(private val db: AttendanceDatabase) {
    fun obtener(carnet: Int): Estudiante? = Estudiante.obtener(db, carnet)
    fun insertar(estudiante: Estudiante) = Estudiante.insertar(db, estudiante)
    fun actualizar(estudiante: Estudiante) = Estudiante.actualizar(db, estudiante)
    fun obtenerPorMateria(materiaId: Long): List<Estudiante> = Estudiante.obtenerPorMateria(db, materiaId)
}

class MateriaModel(private val db: AttendanceDatabase) {
    fun insertar(materia: Materia) = Materia.insertar(db, materia)
    fun actualizar(materia: Materia) = Materia.actualizar(db, materia)
    fun eliminar(id: Long) = Materia.eliminar(db, id)
    fun obtenerPorDocente(docenteId: Int): List<Materia> = Materia.obtenerPorDocente(db, docenteId)
    fun obtenerPorEstudiante(estudianteId: Int): List<Materia> = Materia.obtenerPorEstudiante(db, estudianteId)
}

class InscritoModel(private val db: AttendanceDatabase) {
    fun insertar(inscrito: Inscrito) = Inscrito.insertar(db, inscrito)
    fun eliminarPorMateriaEstudiante(materiaId: Long, estudianteId: Int) =
        Inscrito.eliminarPorMateriaEstudiante(db, materiaId, estudianteId)
}

class AsistenciaModel(private val db: AttendanceDatabase) {
    fun insertar(asistencia: Asistencia): Long = Asistencia.insertar(db, asistencia)
    fun insertarConFechaActual(materiaId: Long): Long = Asistencia.insertarConFechaActual(db, materiaId)
    fun obtenerPorMateria(materiaId: Long): List<Asistencia> = Asistencia.obtenerPorMateria(db, materiaId)
    fun eliminar(id: Long) = Asistencia.eliminar(db, id)
}

class DetalleAsistenciaModel(private val db: AttendanceDatabase) {
    fun insertar(detalle: DetalleAsistencia) = DetalleAsistencia.insertar(db, detalle)
    fun actualizarEstado(asistenciaId: Long, estudianteId: Int, estado: String) =
        DetalleAsistencia.actualizarEstado(db, asistenciaId, estudianteId, estado)

    fun obtenerPorAsistencia(asistenciaId: Long): List<DetalleAsistencia> =
        DetalleAsistencia.obtenerPorAsistencia(db, asistenciaId)
}
