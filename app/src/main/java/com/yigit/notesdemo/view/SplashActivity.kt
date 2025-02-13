package com.yigit.notesdemo.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.yigit.notesdemo.R

class SplashActivity : AppCompatActivity() {


    private val splashTime: Long = 3000 // 3 saniye bekleme süresi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val animationView = findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        animationView.playAnimation() // Animasyonu başlat

        // Belirtilen süre sonunda MainActivity'ye geçiş yap
        Handler(Looper.getMainLooper()).postDelayed({
            startMainActivity()
        }, splashTime)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
