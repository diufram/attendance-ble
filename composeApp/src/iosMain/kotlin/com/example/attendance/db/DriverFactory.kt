package com.example.attendance.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            AttendanceDatabase.Schema,
            "attendance_v2.db"
        )
    }
}
