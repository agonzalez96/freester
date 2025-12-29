package com.example.freester

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote


import android.os.Handler
import android.os.Looper

class ConnectActivity : AppCompatActivity() {

    companion object
    {

        private const val TAG = "CONNECT_DEBUG"
        private const val CLIENT_ID = "7842d79066714c71ba12b5eaba7b6899"
        private const val REDIRECT_URI = "freester://callback"
    }

    private var spotifyAppRemote: SpotifyAppRemote? = null

    private var hasTriedToOpenSpotify = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        findViewById<Button>(R.id.btnTestTrack).setOnClickListener {
            playTestTrack()
        }

    }

    private fun playTestTrack() {
        // Canción de prueba (puedes cambiarla)
        val trackId = "3muhrPqpGXzeEOp3mJiDN4" // la del QR que usaste antes

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("spotify:track:$trackId")
            setPackage("com.spotify.music")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback al navegador
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://open.spotify.com/track/$trackId")
                )
            )
        }
    }


    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }

    override fun onResume() {
        super.onResume()

        if (hasTriedToOpenSpotify) {
            Log.d(TAG, "Volviendo desde Spotify, espero y reintento conexión")

            Handler(Looper.getMainLooper()).postDelayed({
                connectToSpotify()
            }, 1000) // 1 segundo
        }
    }



private fun openSpotifyApp() {
    hasTriedToOpenSpotify = true

    val intent = packageManager.getLaunchIntentForPackage("com.spotify.music")
    if (intent != null) {
        startActivity(intent)
    } else {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")
            )
        )
    }
}


private fun connectToSpotify() {
    Log.d(TAG, "Intentando conectar a Spotify App Remote")

    val params = ConnectionParams.Builder(CLIENT_ID)
        .setRedirectUri(REDIRECT_URI)
        .showAuthView(false)
        .build()

    SpotifyAppRemote.connect(this, params,
        object : Connector.ConnectionListener {

            override fun onConnected(appRemote: SpotifyAppRemote) {
                Log.d(TAG, "Conectado a Spotify")
                spotifyAppRemote = appRemote

                startActivity(Intent(this@ConnectActivity, ScannerActivity::class.java))
                finish()
            }

            override fun onFailure(error: Throwable) {
                Log.e(TAG, "Aún no conectado", error)
            }
        })
}

}
