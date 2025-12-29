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
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import android.widget.Toast




private const val TAG = "SCANNER_DEBUG"

class ScannerActivity : AppCompatActivity() {

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var previewView: PreviewView
    private var isProcessing = false

    companion object
    {
        private const val CLIENT_ID = "7842d79066714c71ba12b5eaba7b6899"
        private const val REDIRECT_URI = "freester://callback"
    }

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

    override fun onStart() {
        super.onStart()
        connectToSpotify()
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }

    private fun connectToSpotify() {

        val params = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(false)
            .build()

        SpotifyAppRemote.connect(
            this,
            params,
            object : Connector.ConnectionListener {

                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    Log.d("HITSTER_MODE", "Spotify conectado")
                }

                override fun onFailure(error: Throwable) {
                    Log.e("HITSTER_MODE", "No conectado", error)

                    Toast.makeText(
                        this@ScannerActivity,
                        "Abre Spotify y vuelve para continuar",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }




    override fun onResume() {
        super.onResume()
        isProcessing = false
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
                            //Log.d(TAG, "Valor QR: $value")

                            if (value != null && value.contains("spotify.com") && value.contains("/track/")) {
                                //Log.d(TAG, "QR de Spotify detectado")

                                isProcessing = true

                                val trackId = extractTrackId(value)
                                //Log.d(TAG, "Track ID extraído: $trackId")
                                spotifyAppRemote?.playerApi
                                    ?.play("spotify:track:$trackId")
                                    ?: run {
                                        Toast.makeText(
                                            this,
                                            "Spotify no conectado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                //openSpotifyTrack(trackId)
                                val intent = Intent(this, PlayerActivity::class.java)
                                intent.putExtra("TRACK_ID", trackId)
                                startActivity(intent)
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

    private fun openSpotifyTrack(trackId: String) {

        val spotifyIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("spotify:track:$trackId")
            setPackage("com.spotify.music")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            // 1. Abrimos Spotify
            startActivity(spotifyIntent)

            // 2. Volvemos a nuestra app tras un pequeño delay
            Handler(Looper.getMainLooper()).postDelayed({
                moveTaskToBack(false)
            }, 100)

        } catch (e: Exception) {
            // Fallback navegador
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://open.spotify.com/track/$trackId")
                )
            )
        }
    }


}
