package com.example.freester

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import android.widget.ImageButton
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.View
import android.view.HapticFeedbackConstants


class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val CLIENT_ID = "7842d79066714c71ba12b5eaba7b6899"
        private const val REDIRECT_URI = "freester://callback"
    }

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var trackId: String
    private var hasStartedPlayback = false
    private var isPlaying = true

    private lateinit var pulseAnimator: ValueAnimator
    private var backgroundAnimator: ValueAnimator? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val btnPlayPause = findViewById<ImageButton>(R.id.btnPlayPause)
        val btnBack5 = findViewById<ImageButton>(R.id.btnBack5)
        val btnForward5 = findViewById<ImageButton>(R.id.btnForward5)
        val btnRestart = findViewById<ImageButton>(R.id.btnRestart)
        val btnScanAgain = findViewById<Button>(R.id.btnScanAgain)
        val playCircle = findViewById<View>(R.id.playCircle)

        val animatedBackground =
            findViewById<AnimatedGradientView>(R.id.animatedBackground)


        // ▶️ ⏸ Play / Pause
        btnPlayPause.setOnClickListener {
            spotifyAppRemote?.playerApi?.let { player ->
                if (isPlaying) {
                    player.pause()
                    btnPlayPause.setImageResource(R.drawable.ic_play_filled)
                    pulseAnimator.pause()
                } else {
                    player.resume()
                    btnPlayPause.setImageResource(R.drawable.ic_pause_filled)
                    pulseAnimator.resume()
                }
                isPlaying = !isPlaying
            }
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }

        // ⏪ -5 segundos
        btnBack5.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            spotifyAppRemote?.playerApi?.getPlayerState()?.setResultCallback { state ->
                val newPosition = (state.playbackPosition - 5000).coerceAtLeast(0)
                spotifyAppRemote?.playerApi?.seekTo(newPosition)
            }
        }

        // ⏩ +5 segundos
        btnForward5.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            spotifyAppRemote?.playerApi?.getPlayerState()?.setResultCallback { state ->
                val newPosition = state.playbackPosition + 5000
                spotifyAppRemote?.playerApi?.seekTo(newPosition)
            }
        }

        // 🔄 Volver a empezar
        btnRestart.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            spotifyAppRemote?.playerApi?.seekTo(0)
        }

        // 📷 Escanear otro QR
        btnScanAgain.setOnClickListener {
            finish() // vuelve al ScannerActivity
        }

        //Animacion Boton central
        pulseAnimator = ValueAnimator.ofFloat(1f, 1.06f).apply {
            duration = 1400
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                playCircle.scaleX = scale
                playCircle.scaleY = scale
            }
        }
        pulseAnimator.start()

        //Animacion fondo
        val backgroundAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 20000L // 20 segundos por vuelta (calmo y elegante)
            repeatCount = ValueAnimator.INFINITE
            interpolator = android.view.animation.LinearInterpolator()

            addUpdateListener { animator ->
                val angle = animator.animatedValue as Float
                animatedBackground.setAngle(angle)
            }
        }
        backgroundAnimator.start()
    }

    override fun onStart() {
        super.onStart()

        if (spotifyAppRemote == null) {
            connectToSpotify()
        }
    }


    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }

    override fun onResume() {
        super.onResume()
        backgroundAnimator?.resume()
    }

    override fun onPause() {
        super.onPause()
        backgroundAnimator?.pause()
    }

    private fun connectToSpotify() {
        val params = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, params,
            object : Connector.ConnectionListener {

                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    Log.d("SPOTIFY", "Conectado a Spotify")
                    playTrackOnceReady()
                }

                override fun onFailure(error: Throwable) {
                    Log.e("SPOTIFY", "Error conexión", error)
                }
            })
    }

    private fun playTrackOnceReady() {
        val uri = "spotify:track:$trackId"

        spotifyAppRemote
            ?.playerApi
            ?.subscribeToPlayerState()
            ?.setEventCallback {

                if (!hasStartedPlayback) {
                    hasStartedPlayback = true
                    spotifyAppRemote?.playerApi?.play(uri)
                }
            }
    }
}
