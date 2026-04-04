package com.example.attendance.db

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): AttendanceDatabase {
    return AttendanceDatabase(driverFactory.createDriver())
}