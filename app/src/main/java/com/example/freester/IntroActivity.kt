package com.example.freester

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val authorized = getSharedPreferences("spotify", MODE_PRIVATE)
            .getBoolean("authorized", false)

        /*if (!authorized) {
            startActivity(Intent(this, SpotifyAuthActivity::class.java))
            finish()
            return
        }*/

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
            finish()
        }
    }

}
