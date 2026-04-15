package com.example.matarpontun.ui.scan

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.RectF
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.matarpontun.AppContainer
import com.example.matarpontun.R
import com.example.matarpontun.ui.patients.PatientListActivity
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.math.max

/**
 * US11 — Camera-based QR code scanner for room lookup.
 *
 * Uses CameraX for live preview and ML Kit for barcode detection.
 * When a QR code with the prefix "ROOM-" is detected, it calls the backend to look up
 * the room and navigates to [PatientListActivity] filtered to that room.
 *
 * [alreadyScanned] prevents duplicate API calls when a code stays in frame.
 */
@ExperimentalGetImage
class QrScanActivity : AppCompatActivity() {

    /** Guards against processing the same QR code multiple times while it stays in frame. */
    private var alreadyScanned = false
    private lateinit var previewView: PreviewView
    private lateinit var overlay: QrOverlayView

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scan)

        previewView = findViewById(R.id.previewView)
        overlay = findViewById(R.id.qrOverlay)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        requestPermission.launch(android.Manifest.permission.CAMERA)
    }

    /** Initialises CameraX with a live preview and an image analysis use case for barcode detection. */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val analysisExecutor = Executors.newSingleThreadExecutor()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val barcodeScanner = BarcodeScanning.getClient()

            // STRATEGY_KEEP_ONLY_LATEST drops frames when the analyser is busy to avoid backlog
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null && !alreadyScanned) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                    // Map bounding box from image coordinates to PreviewView coordinates
                    val rotation = imageProxy.imageInfo.rotationDegrees
                    val imgW = if (rotation == 90 || rotation == 270) imageProxy.height.toFloat()
                               else imageProxy.width.toFloat()
                    val imgH = if (rotation == 90 || rotation == 270) imageProxy.width.toFloat()
                               else imageProxy.height.toFloat()

                    barcodeScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            val barcode = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                            val qrValue = barcode?.rawValue

                            // Draw green box around detected QR code
                            val boundingBox = barcode?.boundingBox
                            if (boundingBox != null) {
                                val viewW = previewView.width.toFloat()
                                val viewH = previewView.height.toFloat()
                                val scale = max(viewW / imgW, viewH / imgH)
                                val offsetX = (viewW - imgW * scale) / 2f
                                val offsetY = (viewH - imgH * scale) / 2f
                                val mapped = RectF(
                                    boundingBox.left * scale + offsetX,
                                    boundingBox.top * scale + offsetY,
                                    boundingBox.right * scale + offsetX,
                                    boundingBox.bottom * scale + offsetY
                                )
                                runOnUiThread { overlay.setRect(mapped) }
                            } else {
                                runOnUiThread { overlay.setRect(null) }
                            }

                            // Only handle codes that look like room QR codes
                            if (qrValue != null) {
                                val trimmed = qrValue.trim()
                                setDebugText(trimmed)
                                if (trimmed.startsWith("ROOM-") && !alreadyScanned) {
                                    alreadyScanned = true
                                    handleQrCode(trimmed)
                                }
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("SetTextI18n")
    private fun setDebugText(text: String) {
        runOnUiThread { findViewById<TextView>(R.id.tvScannedCode).text = text }
    }

    /**
     * Looks up the room on the backend by its QR code, then navigates to [PatientListActivity]
     * with a room filter applied. Resets [alreadyScanned] on failure so the user can try again.
     */
    private fun handleQrCode(qrCode: String) {
        val url = "wards/rooms/qr/$qrCode"
        setDebugText("$qrCode\nCalling: $url")
        lifecycleScope.launch {
            try {
                val room = withContext(Dispatchers.IO) {
                    AppContainer.api.getRoomByQrCode(qrCode)
                }
                val intent = Intent(this@QrScanActivity, PatientListActivity::class.java).apply {
                    putExtra("WARD_ID", room.wardId)
                    putExtra("ROOM_FILTER", room.roomNumber)  // filters list to this room only
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                alreadyScanned = false  // allow retry after error
                setDebugText("$qrCode\nError: ${e.message}")
            }
        }
    }
}
