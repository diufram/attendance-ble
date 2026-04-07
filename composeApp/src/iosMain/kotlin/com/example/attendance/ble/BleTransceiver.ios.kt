package com.example.attendance.ble

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreBluetooth.CBAdvertisementDataLocalNameKey
import platform.CoreBluetooth.CBAdvertisementDataManufacturerDataKey
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBPeripheralManager
import platform.CoreBluetooth.CBPeripheralManagerDelegateProtocol
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.darwin.NSObject
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual class BleTransceiver actual constructor() {
    private var onPayload: ((ByteArray) -> Unit)? = null
    private var onScanError: ((String) -> Unit)? = null
    private var onAdvertiseError: ((String) -> Unit)? = null
    private var pendingScanRequest: PendingScanRequest? = null
    private var pendingAdvertisingRequest: PendingAdvertisingRequest? = null

    private val centralDelegate = CentralDelegate(
        onStateError = { message -> onScanError?.invoke(message) },
        onPoweredOn = { tryStartPendingScan() },
        onPayload = { payload -> onPayload?.invoke(payload) }
    )

    private val peripheralDelegate = PeripheralDelegate(
        onStateError = { message -> onAdvertiseError?.invoke(message) },
        onPoweredOn = { tryStartPendingAdvertising() }
    )

    private val centralManager = CBCentralManager(
        delegate = centralDelegate,
        queue = null
    )

    private val peripheralManager = CBPeripheralManager(
        delegate = peripheralDelegate,
        queue = null
    )

    actual fun startAdvertising(payload: ByteArray, onError: (String) -> Unit) {
        BleDebug.log("IOS-ADV", "startAdvertising len=${payload.size} payload=${payload.toHexPreview()}")
        if (payload.isEmpty() || payload.size > BLE_MAX_CUSTOM_PAYLOAD) {
            onError("Payload BLE invalido")
            return
        }

        onAdvertiseError = onError
        pendingAdvertisingRequest = PendingAdvertisingRequest(payload = payload, onError = onError)

        if (peripheralManager.state != CBManagerStatePoweredOn) {
            BleDebug.log("IOS-ADV", "Peripherial state != poweredOn (${peripheralManager.state})")
            onError("Esperando que Bluetooth se active")
            return
        }

        tryStartPendingAdvertising()
    }

    private fun tryStartPendingAdvertising() {
        if (peripheralManager.state != CBManagerStatePoweredOn) return
        val request = pendingAdvertisingRequest ?: return

        stopAdvertising()

        val localName = BLE_LOCAL_NAME_PREFIX + request.payload.toHexString()
        BleDebug.log("IOS-ADV", "localName len=${localName.length}")
        if (localName.length > 28) {
            BleDebug.log("IOS-ADV", "ATENCION: localName puede truncarse y romper payload")
        }
        runCatching {
            peripheralManager.startAdvertising(
                mapOf(CBAdvertisementDataLocalNameKey to localName)
            )
        }.onFailure {
            BleDebug.log("IOS-ADV", "startAdvertising failure: ${it.message}")
            request.onError("No se pudo iniciar advertising BLE")
        }
    }

    actual fun stopAdvertising() {
        pendingAdvertisingRequest = null
        if (peripheralManager.state == CBManagerStatePoweredOn) {
            BleDebug.log("IOS-ADV", "stopAdvertising")
            runCatching { peripheralManager.stopAdvertising() }
        }
    }

    actual fun startScanning(onPayload: (ByteArray) -> Unit, onError: (String) -> Unit) {
        BleDebug.log("IOS-SCAN", "startScanning state=${centralManager.state}")
        this.onPayload = onPayload
        this.onScanError = onError
        pendingScanRequest = PendingScanRequest(onPayload = onPayload, onError = onError)

        if (centralManager.state != CBManagerStatePoweredOn) {
            BleDebug.log("IOS-SCAN", "Central state != poweredOn (${centralManager.state})")
            onError("Esperando que Bluetooth se active")
            return
        }

        tryStartPendingScan()
    }

    private fun tryStartPendingScan() {
        if (centralManager.state != CBManagerStatePoweredOn) return
        val request = pendingScanRequest ?: return

        this.onPayload = request.onPayload
        this.onScanError = request.onError

        stopScanning()
        runCatching {
            centralManager.scanForPeripheralsWithServices(
                serviceUUIDs = null,
                options = null
            )
        }.onFailure {
            BleDebug.log("IOS-SCAN", "scanForPeripherals failure: ${it.message}")
            request.onError("No se pudo iniciar scan BLE")
        }
    }

    actual fun stopScanning() {
        pendingScanRequest = null
        if (centralManager.state == CBManagerStatePoweredOn) {
            BleDebug.log("IOS-SCAN", "stopScan")
            runCatching { centralManager.stopScan() }
        }
    }

    actual fun stopAll() {
        stopScanning()
        stopAdvertising()
    }
}

private data class PendingScanRequest(
    val onPayload: (ByteArray) -> Unit,
    val onError: (String) -> Unit,
)

private data class PendingAdvertisingRequest(
    val payload: ByteArray,
    val onError: (String) -> Unit,
)

@Composable
actual fun rememberBleTransceiver(): BleTransceiver {
    return remember { BleTransceiver() }
}

@Composable
actual fun rememberRequestBlePermissions(
    onGranted: () -> Unit,
    onDenied: (String) -> Unit,
): () -> Unit {
    val onGrantedState = rememberUpdatedState(onGranted)
    val onDeniedState = rememberUpdatedState(onDenied)
    val requester = remember { IosBlePermissionRequester() }

    return remember(requester) {
        {
            requester.request(
                onGranted = onGrantedState.value,
                onDenied = onDeniedState.value
            )
        }
    }
}

private class IosBlePermissionRequester {
    private var onGranted: (() -> Unit)? = null
    private var onDenied: ((String) -> Unit)? = null

    private val locationDelegate = LocationPermissionDelegate(
        onStatusChanged = { status ->
            when (status) {
                kCLAuthorizationStatusAuthorizedWhenInUse,
                kCLAuthorizationStatusAuthorizedAlways -> onGranted?.invoke()

                kCLAuthorizationStatusDenied,
                kCLAuthorizationStatusRestricted -> {
                    onDenied?.invoke("Activa permiso de ubicacion para usar BLE")
                }
            }
        }
    )

    private val locationManager = CLLocationManager().also {
        it.delegate = locationDelegate
    }

    fun request(onGranted: () -> Unit, onDenied: (String) -> Unit) {
        this.onGranted = onGranted
        this.onDenied = onDenied

        when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> onGranted()

            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> onDenied("Activa permiso de ubicacion para usar BLE")

            kCLAuthorizationStatusNotDetermined -> locationManager.requestWhenInUseAuthorization()

            else -> onDenied("No se pudo obtener permiso de ubicacion")
        }
    }
}

private class LocationPermissionDelegate(
    private val onStatusChanged: (Int) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onStatusChanged(CLLocationManager.authorizationStatus().toInt())
    }
}

@OptIn(ExperimentalForeignApi::class)
private class CentralDelegate(
    private val onStateError: (String) -> Unit,
    private val onPoweredOn: () -> Unit,
    private val onPayload: (ByteArray) -> Unit,
) : NSObject(), CBCentralManagerDelegateProtocol {
    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        BleDebug.log("IOS-SCAN", "central state=${central.state}")
        if (central.state == CBManagerStatePoweredOn) {
            onPoweredOn()
        } else {
            onStateError("Bluetooth no disponible o apagado")
        }
    }

    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: NSNumber,
    ) {
        val manufacturerData = advertisementData[CBAdvertisementDataManufacturerDataKey] as? NSData
        if (manufacturerData != null) {
            manufacturerData.toByteArray()?.let { rawPayload ->
                BleDebug.log("IOS-SCAN", "manufacturer raw ${rawPayload.toHexPreview()}")
                val payload = rawPayload.stripCompanyPrefixIfPresent()
                BleDebug.log("IOS-SCAN", "manufacturer normalized ${payload.toHexPreview()}")
                if (payload.isNotEmpty()) {
                    onPayload(payload)
                    return
                }
            }
        }

        val localName = (advertisementData[CBAdvertisementDataLocalNameKey] as? String)
            ?: return

        val payload = localName
            .takeIf { it.startsWith(BLE_LOCAL_NAME_PREFIX) }
            ?.removePrefix(BLE_LOCAL_NAME_PREFIX)
            ?.hexToByteArray()
            ?: return

        BleDebug.log("IOS-SCAN", "localName payload ${payload.toHexPreview()} localName=$localName")

        onPayload(payload)
    }
}

private class PeripheralDelegate(
    private val onStateError: (String) -> Unit,
    private val onPoweredOn: () -> Unit,
) : NSObject(), CBPeripheralManagerDelegateProtocol {
    override fun peripheralManagerDidUpdateState(peripheral: CBPeripheralManager) {
        BleDebug.log("IOS-ADV", "peripheral state=${peripheral.state}")
        if (peripheral.state == CBManagerStatePoweredOn) {
            onPoweredOn()
        } else {
            onStateError("Bluetooth no disponible o apagado")
        }
    }

    override fun peripheralManagerDidStartAdvertising(
        peripheral: CBPeripheralManager,
        error: NSError?,
    ) {
        if (error != null) {
            BleDebug.log("IOS-ADV", "didStartAdvertising error=${error.localizedDescription}")
            onStateError("No se pudo iniciar advertising BLE")
        } else {
            BleDebug.log("IOS-ADV", "didStartAdvertising success")
        }
    }
}

private fun ByteArray.toHexString(): String {
    return joinToString(separator = "") { byte ->
        (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
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

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray? {
    val lengthInt = length.toInt()
    if (lengthInt <= 0) return byteArrayOf()
    val source = bytes ?: return null
    val result = ByteArray(lengthInt)
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), source, length)
    }
    return result
}

private fun ByteArray.stripCompanyPrefixIfPresent(): ByteArray {
    if (size < 3) return this
    val littleEndianCompanyId = (this[0].toInt() and 0xFF) or ((this[1].toInt() and 0xFF) shl 8)
    return if (littleEndianCompanyId == BLE_COMPANY_ID) copyOfRange(2, size) else this
}
