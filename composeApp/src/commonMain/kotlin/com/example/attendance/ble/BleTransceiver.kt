package com.example.attendance.ble

import androidx.compose.runtime.Composable

const val BLE_COMPANY_ID = 0xFFFE
const val BLE_MAX_CUSTOM_PAYLOAD = 22
const val BLE_LOCAL_NAME_PREFIX = "ATT:"

expect class BleTransceiver() {
    fun startAdvertising(payload: ByteArray, onError: (String) -> Unit = {})
    fun stopAdvertising()
    fun startScanning(onPayload: (ByteArray) -> Unit, onError: (String) -> Unit = {})
    fun stopScanning()
    fun stopAll()
}

@Composable
expect fun rememberBleTransceiver(): BleTransceiver

@Composable
expect fun rememberRequestBlePermissions(
    onGranted: () -> Unit,
    onDenied: (String) -> Unit,
): () -> Unit
