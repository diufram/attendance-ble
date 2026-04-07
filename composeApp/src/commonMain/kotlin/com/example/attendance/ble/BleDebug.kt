package com.example.attendance.ble

object BleDebug {
    const val enabled: Boolean = true

    fun log(tag: String, message: String) {
        if (!enabled) return
        println("[BLE][$tag] $message")
    }
}

fun ByteArray.toHexPreview(maxBytes: Int = 24): String {
    if (isEmpty()) return "<empty>"
    val take = if (size <= maxBytes) this else copyOf(maxBytes)
    val base = take.joinToString(separator = "") { byte ->
        (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
    }
    return if (size > maxBytes) "$base...(${size}B)" else "$base (${size}B)"
}
