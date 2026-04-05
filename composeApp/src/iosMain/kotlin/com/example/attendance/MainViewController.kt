package com.example.attendance

import androidx.compose.ui.window.ComposeUIViewController
import com.example.attendance.db.DriverFactory
import com.example.attendance.db.createDatabase

fun MainViewController() = ComposeUIViewController {
    val db = createDatabase(DriverFactory())
    App(db)
}
