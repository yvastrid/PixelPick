package com.pixelpick.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.pixelpick.app.R
import com.pixelpick.app.data.models.UpdateProfileRequest
import com.pixelpick.app.data.repository.ProfileRepository
import com.pixelpick.app.databinding.ActivitySettingsBinding
import com.pixelpick.app.ui.auth.LoginActivity
import com.pixelpick.app.util.SessionManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var profileRepository: ProfileRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        profileRepository = ProfileRepository(sessionManager)
        
        setupViews()
        setupTabs()
        loadUserData()
    }
    
    private fun setupViews() {
        binding.saveButton.setOnClickListener {
            updateProfile()
        }
        
        binding.deleteAccountButton.setOnClickListener {
            showDeleteAccountDialog()
        }
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }
    
    private fun setupTabs() {
        // Account tab button
        binding.accountTabButton.setOnClickListener {
            switchToTab("account")
        }
        
        // Payment tab button
        binding.paymentTabButton.setOnClickListener {
            switchToTab("payment")
        }
        
        // Start with account tab active
        switchToTab("account")
    }
    
    private fun switchToTab(tab: String) {
        when (tab) {
            "account" -> {
                binding.accountTabContent.visibility = View.VISIBLE
                binding.paymentTabContent.visibility = View.GONE
                binding.accountTabButton.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                binding.paymentTabButton.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
            "payment" -> {
                binding.accountTabContent.visibility = View.GONE
                binding.paymentTabContent.visibility = View.VISIBLE
                binding.accountTabButton.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                binding.paymentTabButton.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            }
        }
    }
    
    private fun loadUserData() {
        val user = sessionManager.getUser()
        user?.let {
            binding.firstNameEditText.setText(it.firstName)
            binding.lastNameEditText.setText(it.lastName)
            binding.changesRemainingText.text = getString(R.string.changes_remaining, 3 - it.nameChangeCount)
            binding.userEmailAddress.text = it.email ?: "email@ejemplo.com"
        }
    }
    
    private fun updateProfile() {
        val firstName = binding.firstNameEditText.text.toString().trim()
        val lastName = binding.lastNameEditText.text.toString().trim()
        
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, R.string.error_required_fields, Toast.LENGTH_SHORT).show()
            return
        }
        
        showLoading(true)
        
        lifecycleScope.launch {
            val result = profileRepository.updateProfile(
                UpdateProfileRequest(firstName, lastName)
            )
            
            showLoading(false)
            
            result.onSuccess { response ->
                Toast.makeText(this@SettingsActivity, response.message ?: getString(R.string.success_profile_update), Toast.LENGTH_SHORT).show()
                response.changesRemaining?.let {
                    binding.changesRemainingText.text = getString(R.string.changes_remaining, it)
                }
            }.onFailure { error ->
                Toast.makeText(this@SettingsActivity, error.message, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Cuenta")
            .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun deleteAccount() {
        showLoading(true)
        
        lifecycleScope.launch {
            val result = profileRepository.deleteAccount()
            
            showLoading(false)
            
            result.onSuccess {
                Toast.makeText(this@SettingsActivity, "Cuenta eliminada exitosamente", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure { error ->
                Toast.makeText(this@SettingsActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !show
        binding.deleteAccountButton.isEnabled = !show
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

