package com.example.attendance.di

import com.example.attendance.controller.AsistenciaDetalleController
import com.example.attendance.controller.AsistenciaViewController
import com.example.attendance.controller.EstudianteHomeController
import com.example.attendance.controller.InscritosViewController
import com.example.attendance.controller.LoginController
import com.example.attendance.controller.MateriaDocenteController
import com.example.attendance.db.AttendanceDatabase

class AttendanceContainer(db: AttendanceDatabase) {
    val materiaDocenteController = MateriaDocenteController(
        db = db,
    )

    val estudianteHomeController = EstudianteHomeController(
        db = db,
    )

    val loginController = LoginController(db)

    val asistenciaViewController = AsistenciaViewController(
        db = db
    )

    val asistenciaDetalleController = AsistenciaDetalleController(db)

    val inscritosViewController = InscritosViewController(db)
}
