package com.example.attendance.di

import com.example.attendance.controller.AsistenciaDetalleController
import com.example.attendance.controller.AsistenciaController
import com.example.attendance.controller.InscritosController
import com.example.attendance.EmptyAsistenciaDetalleView
import com.example.attendance.EmptyAsistenciaView
import com.example.attendance.EmptyInscritosView
import com.example.attendance.db.AttendanceDatabase
import com.example.attendance.model.AsistenciaModel
import com.example.attendance.model.DetalleAsistenciaModel
import com.example.attendance.model.DocenteModel
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.model.MateriaModel

class AttendanceContainer(db: AttendanceDatabase) {
    val docenteModel = DocenteModel(db = db)
    val estudianteModel = EstudianteModel(db = db)
    val materiaModel = MateriaModel(db = db)
    val inscritoModel = InscritoModel(db = db)
    val asistenciaModel = AsistenciaModel(db = db)
    val detalleAsistenciaModel = DetalleAsistenciaModel(db = db)

    val asistenciaController = AsistenciaController(
        estudianteModel = estudianteModel,
        asistenciaModel = asistenciaModel,
        detalleAsistenciaModel = detalleAsistenciaModel,
        inscritoModel = inscritoModel,
        view = EmptyAsistenciaView,
    )

    val asistenciaDetalleController = AsistenciaDetalleController(
        asistenciaModel = asistenciaModel,
        estudianteModel = estudianteModel,
        detalleAsistenciaModel = detalleAsistenciaModel,
        view = EmptyAsistenciaDetalleView,
    )

    val inscritosController = InscritosController(estudianteModel, inscritoModel, EmptyInscritosView)
}
