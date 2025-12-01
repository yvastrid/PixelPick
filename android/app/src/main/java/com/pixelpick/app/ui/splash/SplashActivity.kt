package com.pixelpick.app.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.pixelpick.app.R
import com.pixelpick.app.ui.auth.LoginActivity
import com.pixelpick.app.ui.main.MainActivity
import com.pixelpick.app.util.SessionManager

class SplashActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        sessionManager = SessionManager(this)
        
        // Animar el logo
        findViewById<android.widget.ImageView>(R.id.logo)?.let { logo ->
            logo.alpha = 0f
            logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
        
        // Esperar 2 segundos antes de navegar
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2000)
    }
    
    private fun checkLoginStatus() {
        if (sessionManager.isLoggedIn()) {
            // Usuario ya está logueado, ir a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Usuario no está logueado, ir a LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}

