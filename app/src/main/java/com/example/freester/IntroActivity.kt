package com.example.freester

import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val logo = findViewById<ImageView>(R.id.imgLogo)

        val title = findViewById<TextView>(R.id.tvTitle)
        val subtitle = findViewById<TextView>(R.id.tvSubtitle)
        val button = findViewById<Button>(R.id.btnContinue)

        title.alpha = 0f
        title.translationY = 40f

        subtitle.alpha = 0f
        subtitle.translationY = 40f

        button.alpha = 0f
        button.translationY = 40f

        title.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(200)
            .start()

        subtitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(350)
            .start()

        button.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(500)
            .start()


        logo.scaleX = 0f
        logo.scaleY = 0f
        logo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator())
            .start()

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }
    }
}
