package com.pixelpick.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pixelpick.app.R
import com.pixelpick.app.data.models.RegisterRequest
import com.pixelpick.app.data.repository.AuthRepository
import com.pixelpick.app.databinding.ActivityRegisterBinding
import com.pixelpick.app.ui.main.MainActivity
import com.pixelpick.app.ui.subscription.BenefitsActivity
import com.pixelpick.app.util.SessionManager
import kotlinx.coroutines.launch
import java.util.regex.Pattern

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
        
        // Agregar filtros de entrada para validar mientras el usuario escribe
        setupInputFilters()
        
        // Password visibility is handled by TextInputLayout's endIconMode
    }
    
    private fun setupInputFilters() {
        // Filtro para nombre y apellido: solo letras (sin números ni espacios)
        val nameFilter = InputFilter { source, start, end, dest, dstart, dend ->
            val regex = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ]*$")
            for (i in start until end) {
                val char = source[i].toString()
                if (!regex.matcher(char).matches()) {
                    return@InputFilter ""
                }
            }
            null
        }
        
        binding.firstNameEditText.filters = arrayOf(nameFilter)
        binding.lastNameEditText.filters = arrayOf(nameFilter)
        
        // Filtro para email: no espacios en blanco
        val emailFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }
        
        binding.emailEditText.filters = arrayOf(emailFilter)
        
        // Filtro para contraseña: no espacios en blanco
        val passwordFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }
        
        binding.passwordEditText.filters = arrayOf(passwordFilter)
    }
    
    private fun performRegister() {
        val firstName = binding.firstNameEditText.text.toString().trim()
        val lastName = binding.lastNameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
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
                    // Redirigir a la pantalla de planes para que el usuario elija su plan
                    val intent = Intent(this@RegisterActivity, BenefitsActivity::class.java)
                    intent.putExtra("mode", "new_user") // Modo especial para nuevo usuario
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
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
        // Validar campos vacíos
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_required_fields, Toast.LENGTH_SHORT).show()
            return false
        }
        
        // Validar nombre: solo letras (sin números ni espacios)
        val namePattern = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ]+$")
        if (!namePattern.matcher(firstName).matches()) {
            Toast.makeText(this, "El nombre solo puede contener letras", Toast.LENGTH_SHORT).show()
            return false
        }
        
        // Validar apellido: solo letras (sin números ni espacios)
        if (!namePattern.matcher(lastName).matches()) {
            Toast.makeText(this, "El apellido solo puede contener letras", Toast.LENGTH_SHORT).show()
            return false
        }
        
        // Validar email: formato válido sin espacios
        if (email.contains(" ")) {
            Toast.makeText(this, "El correo electrónico no puede contener espacios", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido. Formato: texto@texto.texto", Toast.LENGTH_SHORT).show()
            return false
        }
        
        // Validar contraseña: sin espacios, mínimo 8 caracteres
        if (password.contains(" ")) {
            Toast.makeText(this, "La contraseña no puede contener espacios", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (password.length < 8) {
            Toast.makeText(this, R.string.error_password_length, Toast.LENGTH_SHORT).show()
            return false
        }
        
        // Validar términos y condiciones
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

