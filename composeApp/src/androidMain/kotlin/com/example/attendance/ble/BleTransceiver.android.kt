package com.example.attendance.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

actual class BleTransceiver actual constructor() {
    private var advertiseCallback: AdvertiseCallback? = null
    private var scanCallback: ScanCallback? = null

    private val adapter: BluetoothAdapter?
        get() = BluetoothAdapter.getDefaultAdapter()

    actual fun startAdvertising(payload: ByteArray, onError: (String) -> Unit) {
        BleDebug.log("ANDROID-ADV", "startAdvertising len=${payload.size} payload=${payload.toHexPreview()}")
        if (payload.isEmpty() || payload.size > BLE_MAX_CUSTOM_PAYLOAD) {
            onError("Payload BLE invalido")
            return
        }

        val btAdapter = adapter
        if (btAdapter == null) {
            onError("Bluetooth no disponible")
            return
        }
        if (!btAdapter.isEnabled) {
            onError("Bluetooth apagado")
            return
        }

        val advertiser = btAdapter.bluetoothLeAdvertiser
        if (advertiser == null) {
            onError("Este dispositivo no soporta BLE advertising")
            return
        }

        stopAdvertising()

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(false)
            .setTimeout(0)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addManufacturerData(BLE_COMPANY_ID, payload)
            .build()

        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                BleDebug.log("ANDROID-ADV", "Advertising iniciado")
            }

            override fun onStartFailure(errorCode: Int) {
                BleDebug.log("ANDROID-ADV", "onStartFailure code=$errorCode")
                onError("No se pudo iniciar advertising BLE ($errorCode)")
            }
        }

        advertiseCallback = callback

        try {
            advertiser.startAdvertising(settings, data, callback)
        } catch (_: SecurityException) {
            BleDebug.log("ANDROID-ADV", "SecurityException al iniciar advertising")
            onError("Permisos Bluetooth faltantes")
        } catch (_: Throwable) {
            BleDebug.log("ANDROID-ADV", "Throwable al iniciar advertising")
            onError("Error iniciando advertising BLE")
        }
    }

    actual fun stopAdvertising() {
        val btAdapter = adapter ?: return
        val advertiser = btAdapter.bluetoothLeAdvertiser ?: return
        val callback = advertiseCallback ?: return
        runCatching { advertiser.stopAdvertising(callback) }
        advertiseCallback = null
    }

    actual fun startScanning(onPayload: (ByteArray) -> Unit, onError: (String) -> Unit) {
        BleDebug.log("ANDROID-SCAN", "startScanning")
        val btAdapter = adapter
        if (btAdapter == null) {
            onError("Bluetooth no disponible")
            return
        }
        if (!btAdapter.isEnabled) {
            onError("Bluetooth apagado")
            return
        }

        val scanner = btAdapter.bluetoothLeScanner
        if (scanner == null) {
            onError("Scanner BLE no disponible")
            return
        }

        stopScanning()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val scanRecord = result.scanRecord
                BleDebug.log(
                    "ANDROID-SCAN",
                    "resultado device=${result.device?.name ?: "?"} addr=${result.device?.address ?: "?"} rssi=${result.rssi}"
                )
                val manufacturerData = scanRecord
                    ?.manufacturerSpecificData
                    ?.get(BLE_COMPANY_ID)
                    ?.clone()

                if (manufacturerData != null) {
                    BleDebug.log("ANDROID-SCAN", "manufacturerData[BLE_COMPANY_ID] ${manufacturerData.toHexPreview()}")
                    onPayload(manufacturerData)
                    return
                }

                val anyManufacturer = scanRecord?.manufacturerSpecificData
                if (anyManufacturer != null && anyManufacturer.size() > 0) {
                    for (index in 0 until anyManufacturer.size()) {
                        val key = anyManufacturer.keyAt(index)
                        val value = anyManufacturer.valueAt(index)
                        if (value != null) {
                            BleDebug.log(
                                "ANDROID-SCAN",
                                "manufacturerData[$key] ${value.toHexPreview()}"
                            )
                            if (BlePacketCodec.decodeAny(value) != null) {
                                onPayload(value)
                                return
                            }
                        }
                    }
                }

                val localName = scanRecord?.deviceName ?: result.device?.name
                val transportPayload = localName
                    ?.takeIf { it.startsWith(BLE_LOCAL_NAME_PREFIX) }
                    ?.removePrefix(BLE_LOCAL_NAME_PREFIX)
                    ?.hexToByteArray()
                    ?: return

                BleDebug.log("ANDROID-SCAN", "localName payload ${transportPayload.toHexPreview()}")

                onPayload(transportPayload)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
            }

            override fun onScanFailed(errorCode: Int) {
                BleDebug.log("ANDROID-SCAN", "onScanFailed code=$errorCode")
                onError("No se pudo iniciar scan BLE ($errorCode)")
            }
        }

        scanCallback = callback

        try {
            scanner.startScan(null, settings, callback)
        } catch (_: SecurityException) {
            BleDebug.log("ANDROID-SCAN", "SecurityException al iniciar scan")
            onError("Permisos Bluetooth faltantes")
        } catch (_: Throwable) {
            BleDebug.log("ANDROID-SCAN", "Throwable al iniciar scan")
            onError("Error iniciando scan BLE")
        }
    }

    actual fun stopScanning() {
        val btAdapter = adapter ?: return
        val scanner = btAdapter.bluetoothLeScanner ?: return
        val callback = scanCallback ?: return
        runCatching { scanner.stopScan(callback) }
        scanCallback = null
    }

    actual fun stopAll() {
        stopScanning()
        stopAdvertising()
    }
}

@Composable
actual fun rememberBleTransceiver(): BleTransceiver {
    LocalContext.current
    return remember { BleTransceiver() }
}

@Composable
actual fun rememberRequestBlePermissions(
    onGranted: () -> Unit,
    onDenied: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val onGrantedState = rememberUpdatedState(onGranted)
    val onDeniedState = rememberUpdatedState(onDenied)

    val permisos = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = permisos.all { permission ->
            result[permission] == true ||
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            onGrantedState.value()
        } else {
            onDeniedState.value("Permisos Bluetooth requeridos")
        }
    }

    return remember(context, launcher) {
        {
            val faltantes = permisos.filter {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }

            if (faltantes.isEmpty()) {
                onGrantedState.value()
            } else {
                launcher.launch(faltantes.toTypedArray())
            }
        }
    }
}

private fun String.hexToByteArray(): ByteArray? {
    if (length % 2 != 0) return null
    return runCatching {
        ByteArray(length / 2) { index ->
            substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }.getOrNull()
}
