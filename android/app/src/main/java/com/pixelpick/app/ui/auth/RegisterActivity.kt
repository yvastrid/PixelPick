package com.pixelpick.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pixelpick.app.R
import com.pixelpick.app.data.models.RegisterRequest
import com.pixelpick.app.data.repository.AuthRepository
import com.pixelpick.app.databinding.ActivityRegisterBinding
import com.pixelpick.app.ui.main.MainActivity
import com.pixelpick.app.util.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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
        
        // Animar campos con delay escalonado
        val fields = listOf(
            binding.firstNameEditText,
            binding.lastNameEditText,
            binding.emailEditText,
            binding.passwordEditText
        )
        
        fields.forEachIndexed { index, field ->
            field.alpha = 0f
            field.translationY = 30f
            field.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(200 + (index * 100).toLong())
                .start()
        }
        
        // Animar botón
        binding.registerButton.alpha = 0f
        binding.registerButton.translationY = 30f
        binding.registerButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(600)
            .start()
    }
    
    private fun setupViews() {
        binding.registerButton.setOnClickListener {
            performRegister()
        }
        
        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        
        // Password visibility is handled by TextInputLayout's endIconMode
    }
    
    private fun performRegister() {
        val firstName = binding.firstNameEditText.text.toString().trim()
        val lastName = binding.lastNameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        val termsAccepted = binding.termsCheckBox.isChecked
        
        if (validateInput(firstName, lastName, email, password, termsAccepted)) {
            showLoading(true)
            
            lifecycleScope.launch {
                val result = authRepository.register(
                    RegisterRequest(firstName, lastName, email, password, termsAccepted)
                )
                
                showLoading(false)
                
                result.onSuccess { response ->
                    Toast.makeText(this@RegisterActivity, R.string.success_register, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                }.onFailure { error ->
                    Toast.makeText(this@RegisterActivity, error.message ?: getString(R.string.error_register), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun validateInput(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        termsAccepted: Boolean
    ): Boolean {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_required_fields, Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (password.length < 8) {
            Toast.makeText(this, R.string.error_password_length, Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (!termsAccepted) {
            Toast.makeText(this, R.string.error_terms, Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.registerButton.isEnabled = !show
    }
}

