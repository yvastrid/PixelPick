package com.pixelpick.app.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pixelpick.app.R
import com.pixelpick.app.ui.auth.LoginActivity
import com.pixelpick.app.ui.main.MainActivity
import com.pixelpick.app.ui.main.MainActivityPremium
import com.pixelpick.app.util.SessionManager
import com.pixelpick.app.data.api.RetrofitClient
import com.pixelpick.app.data.repository.SubscriptionRepository
import com.pixelpick.app.util.onSuccess
import com.pixelpick.app.util.onFailure
import kotlinx.coroutines.launch

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
            // Usuario ya está logueado, verificar plan y redirigir
            checkPlanAndRedirect()
        } else {
            // Usuario no está logueado, ir a LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    
    private fun checkPlanAndRedirect() {
        val subscriptionRepository = SubscriptionRepository(RetrofitClient.apiService)
        lifecycleScope.launch {
            android.util.Log.d("SplashActivity", "=== VERIFICANDO PLAN DEL USUARIO ===")
            val result = subscriptionRepository.getSubscriptionStatus()
            result.onSuccess { statusResponse ->
                val planType = statusResponse.subscription?.planType ?: ""
                val hasPremiumAccess = statusResponse.subscription?.hasPremiumAccess == true
                android.util.Log.d("SplashActivity", "🔍 Plan type recibido: '$planType'")
                android.util.Log.d("SplashActivity", "🔍 hasPremiumAccess: $hasPremiumAccess")
                
                // Verificar tipo de plan - considerar acceso premium por periodo pagado
                val isPremiumPlan = planType.equals("pixelie_plan", ignoreCase = true) || hasPremiumAccess
                
                android.util.Log.d("SplashActivity", "✅ isPremiumPlan: $isPremiumPlan")
                
                val intent = if (isPremiumPlan && statusResponse.hasSubscription) {
                    android.util.Log.d("SplashActivity", "✅ Redirigiendo a MainActivityPremium")
                    Intent(this@SplashActivity, MainActivityPremium::class.java)
                } else {
                    android.util.Log.d("SplashActivity", "✅ Redirigiendo a MainActivity (básico)")
                    Intent(this@SplashActivity, MainActivity::class.java)
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure { error ->
                android.util.Log.e("SplashActivity", "❌ Error al verificar plan: ${error.message}")
                // En caso de error, ir a MainActivity básico por defecto
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}

