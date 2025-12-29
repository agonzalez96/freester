package com.example.freester

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote

class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val CLIENT_ID = "7842d79066714c71ba12b5eaba7b6899"
        private const val REDIRECT_URI = "freester://callback"
    }

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var trackId: String
    private var hasStartedPlayback = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("PLAYER_DEBUG", "PlayerActivity onCreate")
        setContentView(R.layout.activity_player)

        trackId = intent.getStringExtra("TRACK_ID") ?: run {
            finish()
            return
        }

        findViewById<Button>(R.id.btnPlayPause).setOnClickListener {
            togglePlayPause()
        }

        findViewById<Button>(R.id.btnRestart).setOnClickListener {
            spotifyAppRemote?.playerApi?.seekTo(0)
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
            finish()
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

    private fun togglePlayPause() {
        spotifyAppRemote?.playerApi?.playerState
            ?.setResultCallback { state ->
                if (state.isPaused) {
                    spotifyAppRemote?.playerApi?.resume()
                } else {
                    spotifyAppRemote?.playerApi?.pause()
                }
            }
    }
}
