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
        
        animateSplashScreen()
        
        // Esperar 2.5 segundos antes de navegar (más tiempo para disfrutar la animación)
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2500)
    }
    
    private fun animateSplashScreen() {
        val logo = findViewById<android.widget.ImageView>(R.id.logo)
        val logoGlow = findViewById<android.widget.ImageView>(R.id.logoGlow)
        val glowEffect1 = findViewById<View>(R.id.glowEffect1)
        val glowEffect2 = findViewById<View>(R.id.glowEffect2)
        
        // Animación del logo principal: entrada con escala y rotación sutil
        logo?.let {
            it.alpha = 0f
            it.scaleX = 0.5f
            it.scaleY = 0.5f
            it.rotation = -10f
            
            it.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0f)
                .setDuration(1200)
                .setInterpolator(android.view.animation.OvershootInterpolator())
                .withEndAction {
                    // Efecto de pulso continuo después de la entrada
                    startPulseAnimation(it)
                }
                .start()
        }
        
        // Animación del glow detrás del logo
        logoGlow?.let {
            it.alpha = 0f
            it.scaleX = 0.3f
            it.scaleY = 0.3f
            
            it.animate()
                .alpha(0.3f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1500)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .withEndAction {
                    // Efecto de respiración para el glow
                    startBreathingAnimation(it)
                }
                .start()
        }
        
        // Animación de partículas flotantes
        animateParticles()
        
        // Animación de efectos de brillo decorativos
        glowEffect1?.let {
            it.alpha = 0f
            it.scaleX = 0f
            it.scaleY = 0f
            
            it.animate()
                .alpha(0.2f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setStartDelay(300)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .withEndAction {
                    startFloatingAnimation(it, 2000f, 1500f)
                }
                .start()
        }
        
        glowEffect2?.let {
            it.alpha = 0f
            it.scaleX = 0f
            it.scaleY = 0f
            
            it.animate()
                .alpha(0.15f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setStartDelay(500)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .withEndAction {
                    startFloatingAnimation(it, 1800f, 2000f)
                }
                .start()
        }
    }
    
    private fun startPulseAnimation(view: android.widget.ImageView) {
        val pulseAnimator = android.animation.ObjectAnimator.ofPropertyValuesHolder(
            view,
            android.animation.PropertyValuesHolder.ofFloat("scaleX", 1f, 1.05f, 1f),
            android.animation.PropertyValuesHolder.ofFloat("scaleY", 1f, 1.05f, 1f)
        )
        pulseAnimator.duration = 2000
        pulseAnimator.repeatCount = android.animation.ValueAnimator.INFINITE
        pulseAnimator.repeatMode = android.animation.ValueAnimator.REVERSE
        pulseAnimator.interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        pulseAnimator.start()
    }
    
    private fun startBreathingAnimation(view: android.widget.ImageView) {
        val breathingAnimator = android.animation.ObjectAnimator.ofPropertyValuesHolder(
            view,
            android.animation.PropertyValuesHolder.ofFloat("alpha", 0.3f, 0.5f, 0.3f),
            android.animation.PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f),
            android.animation.PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f)
        )
        breathingAnimator.duration = 2500
        breathingAnimator.repeatCount = android.animation.ValueAnimator.INFINITE
        breathingAnimator.repeatMode = android.animation.ValueAnimator.REVERSE
        breathingAnimator.interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        breathingAnimator.start()
    }
    
    private fun animateParticles() {
        val particles = listOf(
            R.id.particle1, R.id.particle2, R.id.particle3,
            R.id.particle4, R.id.particle5, R.id.particle6
        )
        
        particles.forEachIndexed { index, particleId ->
            findViewById<android.widget.TextView>(particleId)?.let { particle ->
                particle.alpha = 0f
                particle.scaleX = 0f
                particle.scaleY = 0f
                
                val delay = index * 150L
                val duration = 800L + (index * 100L)
                
                particle.animate()
                    .alpha(0.6f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration)
                    .setStartDelay(delay)
                    .setInterpolator(android.view.animation.OvershootInterpolator())
                    .withEndAction {
                        // Animación flotante continua
                        startFloatingAnimation(particle, 1000f + (index * 200f), 1500f + (index * 300f))
                        
                        // Efecto de parpadeo sutil
                        startTwinkleAnimation(particle)
                    }
                    .start()
            }
        }
    }
    
    private fun startFloatingAnimation(view: View, duration: Float, delay: Long = 0) {
        val floatAnimator = android.animation.ObjectAnimator.ofPropertyValuesHolder(
            view,
            android.animation.PropertyValuesHolder.ofFloat("translationY", 0f, -30f, 0f, 30f, 0f),
            android.animation.PropertyValuesHolder.ofFloat("translationX", 0f, 15f, 0f, -15f, 0f)
        )
        floatAnimator.duration = duration.toLong()
        floatAnimator.repeatCount = android.animation.ValueAnimator.INFINITE
        floatAnimator.repeatMode = android.animation.ValueAnimator.RESTART
        floatAnimator.interpolator = android.view.animation.LinearInterpolator()
        if (delay > 0) {
            floatAnimator.startDelay = delay
        }
        floatAnimator.start()
    }
    
    private fun startTwinkleAnimation(view: View) {
        val twinkleAnimator = android.animation.ObjectAnimator.ofFloat(view, "alpha", 0.6f, 0.3f, 0.6f)
        twinkleAnimator.duration = 2000 + (Math.random() * 1000).toLong()
        twinkleAnimator.repeatCount = android.animation.ValueAnimator.INFINITE
        twinkleAnimator.repeatMode = android.animation.ValueAnimator.REVERSE
        twinkleAnimator.interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        twinkleAnimator.start()
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

