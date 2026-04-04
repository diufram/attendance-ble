package com.example.attendance

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform