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
import com.pixelpick.app.util.SessionManager
import kotlinx.coroutines.launch

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
                    Toast.makeText(this@LoginActivity, R.string.success_login, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
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
}

