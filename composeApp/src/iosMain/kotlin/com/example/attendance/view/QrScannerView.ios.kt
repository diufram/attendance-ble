package com.example.attendance.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSProcessInfo
import platform.QuartzCore.CATransaction
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QrScannerView(
    modifier: Modifier,
    onQrScanned: (String) -> Unit,
    onError: (String) -> Unit
) {
    val scanner = remember { QrScannerState(onQrScanned, onError) }

    DisposableEffect(Unit) {
        scanner.iniciar()
        onDispose { scanner.detener() }
    }

    UIKitView(
        modifier = modifier,
        factory = { QrPreviewUIView(scanner.session) },
        onRelease = { scanner.detener() }
    )
}

// UIView que maneja su propio previewLayer via layoutSubviews().
// iOS llama a layoutSubviews() cada vez que la vista cambia de tamaño,
// eliminando la necesidad de sincronizar frames manualmente desde Compose.
@OptIn(ExperimentalForeignApi::class)
private class QrPreviewUIView(session: AVCaptureSession) : UIView(frame = CGRectZero.readValue()) {

    private val previewLayer: AVCaptureVideoPreviewLayer

    init {
        backgroundColor = UIColor.blackColor
        clipsToBounds = true

        previewLayer = AVCaptureVideoPreviewLayer(session = session).also { pl ->
            pl.videoGravity = AVLayerVideoGravityResizeAspectFill
            pl.masksToBounds = true
            layer.insertSublayer(pl, atIndex = 0u)
        }
    }

    // iOS llama a esto automáticamente cuando el frame de la vista cambia.
    // Es el lugar correcto y confiable para actualizar sublayers.
    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true) // sin animación al redimensionar
        previewLayer.frame = bounds
        CATransaction.commit()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class QrScannerState(
    private val onQrScanned: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    val session = AVCaptureSession()
    private var delegate: QrDelegate? = null
    private var ultimoPayload: String? = null

    init { configurar() }

    private fun configurar() {
        val esSimulador = NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null
        if (esSimulador) {
            onError("Simulador detectado: usa un iPhone físico para escanear QR.")
            return
        }

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            ?: return onError("No se encontró cámara disponible.")

        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, error = null)
        if (input == null || !session.canAddInput(input)) {
            return onError("No se pudo acceder a la cámara.")
        }

        val output = AVCaptureMetadataOutput()
        if (!session.canAddOutput(output)) {
            return onError("No se pudo configurar el lector QR.")
        }

        delegate = QrDelegate { payload ->
            if (payload != ultimoPayload) {
                ultimoPayload = payload
                onQrScanned(payload)
            }
        }

        session.beginConfiguration()
        session.addInput(input)
        session.addOutput(output)
        output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        output.setMetadataObjectsDelegate(delegate, dispatch_get_main_queue())
        session.commitConfiguration()
    }

    // startRunning() y stopRunning() son bloqueantes — siempre en background thread
    fun iniciar() {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            if (!session.running) session.startRunning()
        }
    }

    fun detener() {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            if (session.running) session.stopRunning()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class QrDelegate(
    private val onPayload: (String) -> Unit
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        didOutputMetadataObjects
            .filterIsInstance<AVMetadataMachineReadableCodeObject>()
            .firstOrNull { it.type == AVMetadataObjectTypeQRCode }
            ?.stringValue
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { onPayload(it) }
    }
}
