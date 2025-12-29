package com.example.freester

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class SpotifyAuthActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SPOTIFY_AUTH"
        private const val CLIENT_ID = "7842d79066714c71ba12b5eaba7b6899"
        private const val REDIRECT_URI = "freester://callback"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI mínima para evitar pantalla blanca
        val tv = TextView(this)
        tv.text = "Conectando con Spotify…"
        tv.textSize = 18f
        setContentView(tv)

        Log.d(TAG, "onCreate intent=$intent")

        // ¿Venimos del redirect?
        if (handleRedirect(intent)) return

        startLogin()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent intent=$intent")
        handleRedirect(intent)
    }

    private fun handleRedirect(intent: Intent): Boolean {

        Log.d(TAG, "Redirect recibido correctamente")
        val uri: Uri = intent.data ?: return false

        if (uri.scheme == "freester") {
            Log.d(TAG, "Redirect recibido correctamente")

            getSharedPreferences("spotify", MODE_PRIVATE)
                .edit()
                .putBoolean("authorized", true)
                .apply()

            startActivity(
                Intent(this, IntroActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
            finish()
            return true
        }
        return false
    }

    private fun startLogin() {
        Log.d(TAG, "Lanzando login Spotify")

        val request = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )
            .setScopes(
                arrayOf(
                    "streaming",
                    "app-remote-control",
                    "user-modify-playback-state",
                    "user-read-playback-state"
                )
            )
            .build()
        Log.d(TAG, "login request $request")

        AuthorizationClient.openLoginActivity(this, 0, request)
        Log.d(TAG, "Request lanzado")

    }
}
