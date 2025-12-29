package com.example.freester

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

private const val TAG = "SCANNER_DEBUG"

class ScannerActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        previewView = findViewById(R.id.previewView)

        if (hasCameraPermission()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val scanner = BarcodeScanning.getClient()

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->

                //Log.d(TAG, "Frame recibido")

                if (isProcessing) {
                    //Log.d(TAG, "Ya procesando, ignoro frame")
                    imageProxy.close()
                    return@setAnalyzer
                }

                val mediaImage = imageProxy.image ?: run {
                    //Log.d(TAG, "mediaImage es null")
                    imageProxy.close()
                    return@setAnalyzer
                }

                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                val scanner = BarcodeScanning.getClient()

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        //Log.d(TAG, "Barcodes detectados: ${barcodes.size}")

                        for (barcode in barcodes) {
                            val value = barcode.rawValue
                            Log.d(TAG, "Valor QR: $value")

                            if (value != null && value.contains("spotify.com") && value.contains("/track/")) {
                                Log.d(TAG, "QR de Spotify detectado")

                                isProcessing = true

                                val trackId = extractTrackId(value)
                                Log.d(TAG, "Track ID extraído: $trackId")

                                val intent = Intent(this, PlayerActivity::class.java)
                                intent.putExtra("TRACK_ID", trackId)
                                startActivity(intent)
                                finish()
                                break
                            }
                        }
                    }
                    .addOnFailureListener {
                        //Log.e(TAG, "Error procesando imagen", it)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }


            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )

        }, ContextCompat.getMainExecutor(this))
    }

    private fun extractTrackId(url: String): String {
        return url
            .substringAfter("/track/")
            .substringBefore("?")
            .substringBefore("/")
    }
}
