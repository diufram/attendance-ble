package com.example.attendance.di

import com.example.attendance.controller.AsistenciaDetalleController
import com.example.attendance.controller.AsistenciaController
import com.example.attendance.controller.InscritosController
import com.example.attendance.controller.LoginController
import com.example.attendance.controller.MateriaEstudianteController
import com.example.attendance.controller.MateriaDocenteController
import com.example.attendance.EmptyAsistenciaDetalleView
import com.example.attendance.EmptyAsistenciaView
import com.example.attendance.EmptyInscritosView
import com.example.attendance.EmptyLoginView
import com.example.attendance.EmptyMateriaDocenteView
import com.example.attendance.EmptyMateriaEstudianteView
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

    val materiaDocenteController = MateriaDocenteController(
        materiaModel = materiaModel,
        view = EmptyMateriaDocenteView,
    )

    val materiaEstudianteController = MateriaEstudianteController(
        materiaModel = materiaModel,
        estudianteModel = estudianteModel,
        inscritoModel = inscritoModel,
        view = EmptyMateriaEstudianteView,
    )

    val loginController = LoginController(docenteModel, estudianteModel, materiaModel, EmptyLoginView)

    val asistenciaController = AsistenciaController(
        estudianteModel = estudianteModel,
        asistenciaModel = asistenciaModel,
        detalleAsistenciaModel = detalleAsistenciaModel,
        inscritoModel = inscritoModel,
        view = EmptyAsistenciaView,
    )

    val asistenciaDetalleController = AsistenciaDetalleController(
        detalleAsistenciaModel,
        EmptyAsistenciaDetalleView,
    )

    val inscritosController = InscritosController(estudianteModel, inscritoModel, EmptyInscritosView)
}
