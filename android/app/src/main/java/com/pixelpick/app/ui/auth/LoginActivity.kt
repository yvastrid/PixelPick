package com.pixelpick.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pixelpick.app.R
import com.pixelpick.app.data.models.LoginRequest
import com.pixelpick.app.data.repository.AuthRepository
import com.pixelpick.app.databinding.ActivityLoginBinding
import com.pixelpick.app.ui.main.MainActivity
import com.pixelpick.app.ui.main.MainActivityPremium
import com.pixelpick.app.data.api.RetrofitClient
import com.pixelpick.app.data.repository.SubscriptionRepository
import com.pixelpick.app.util.onSuccess
import com.pixelpick.app.util.onFailure
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.pixelpick.app.util.SessionManager

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        authRepository = AuthRepository(sessionManager)
        
        setupViews()
        animateViews()
    }
    
    private fun animateViews() {
        // Animar logo
        binding.logoImageView.alpha = 0f
        binding.logoImageView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
        
        // Animar campos de entrada con delay
        binding.emailEditText.alpha = 0f
        binding.emailEditText.translationY = 30f
        binding.emailEditText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(200)
            .start()
        
        binding.passwordEditText.alpha = 0f
        binding.passwordEditText.translationY = 30f
        binding.passwordEditText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(300)
            .start()
        
        // Animar botón
        binding.loginButton.alpha = 0f
        binding.loginButton.translationY = 30f
        binding.loginButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(400)
            .start()
    }
    
    private fun setupViews() {
        binding.loginButton.setOnClickListener {
            performLogin()
        }
        
        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        
        // Password visibility is handled by TextInputLayout's endIconMode
    }
    
    private fun performLogin() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        
        if (validateInput(email, password)) {
            showLoading(true)
            
            lifecycleScope.launch {
                val result = authRepository.login(LoginRequest(email, password))
                
                showLoading(false)
                
                result.onSuccess { response ->
                    android.util.Log.d("LoginActivity", "✅ Login exitoso, usuario: ${response.user?.email}")
                    Toast.makeText(this@LoginActivity, R.string.success_login, Toast.LENGTH_SHORT).show()
                    // Esperar un momento para asegurar que la sesión esté completamente establecida
                    // antes de verificar el plan
                    kotlinx.coroutines.delay(1000)
                    // Verificar el plan del usuario y redirigir a la Activity correspondiente
                    checkPlanAndRedirect()
                }.onFailure { error ->
                    Toast.makeText(this@LoginActivity, error.message ?: getString(R.string.error_login), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_required_fields, Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !show
    }
    
    private fun checkPlanAndRedirect() {
        val subscriptionRepository = SubscriptionRepository(RetrofitClient.apiService)
        lifecycleScope.launch {
            android.util.Log.d("LoginActivity", "=== VERIFICANDO PLAN DESPUÉS DE LOGIN ===")
            
            // Esperar un momento para asegurar que la sesión esté establecida
            kotlinx.coroutines.delay(500)
            
            val result = subscriptionRepository.getSubscriptionStatus()
            result.onSuccess { statusResponse ->
                android.util.Log.d("LoginActivity", "✅ Respuesta recibida exitosamente")
                android.util.Log.d("LoginActivity", "hasSubscription: ${statusResponse.hasSubscription}")
                android.util.Log.d("LoginActivity", "subscription: ${statusResponse.subscription}")
                
                val planType = statusResponse.subscription?.planType ?: ""
                android.util.Log.d("LoginActivity", "🔍 Plan type recibido: '$planType'")
                android.util.Log.d("LoginActivity", "🔍 Plan type length: ${planType.length}")
                android.util.Log.d("LoginActivity", "🔍 Plan type equals 'pixelie_plan': ${planType.equals("pixelie_plan", ignoreCase = true)}")
                android.util.Log.d("LoginActivity", "🔍 Plan type contains 'pixelie': ${planType.contains("pixelie", ignoreCase = true)}")
                android.util.Log.d("LoginActivity", "🔍 Plan type contains 'basic': ${planType.contains("basic", ignoreCase = true)}")
                
                // Verificar tipo de plan - comparación estricta (igual que en BenefitsActivity)
                val isPremiumPlan = planType.equals("pixelie_plan", ignoreCase = true)
                
                android.util.Log.d("LoginActivity", "✅ isPremiumPlan: $isPremiumPlan")
                android.util.Log.d("LoginActivity", "✅ hasSubscription: ${statusResponse.hasSubscription}")
                android.util.Log.d("LoginActivity", "✅ Condición completa: ${isPremiumPlan && statusResponse.hasSubscription}")
                
                val intent = if (isPremiumPlan && statusResponse.hasSubscription) {
                    android.util.Log.d("LoginActivity", "✅✅✅ REDIRIGIENDO A MainActivityPremium ✅✅✅")
                    Intent(this@LoginActivity, MainActivityPremium::class.java)
                } else {
                    android.util.Log.d("LoginActivity", "✅✅✅ REDIRIGIENDO A MainActivity (básico) ✅✅✅")
                    Intent(this@LoginActivity, MainActivity::class.java)
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure { error ->
                android.util.Log.e("LoginActivity", "❌ Error al verificar plan: ${error.message}")
                android.util.Log.e("LoginActivity", "❌ Error stack trace:")
                error.printStackTrace()
                // En caso de error, ir a MainActivity básico por defecto
                android.util.Log.d("LoginActivity", "Redirigiendo a MainActivity por error")
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}

