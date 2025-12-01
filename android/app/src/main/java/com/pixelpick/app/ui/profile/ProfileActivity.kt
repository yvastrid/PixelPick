package com.pixelpick.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pixelpick.app.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.pixelpick.app.data.repository.AuthRepository
import com.pixelpick.app.data.repository.ProfileRepository
import com.pixelpick.app.databinding.ActivityProfileBinding
import com.pixelpick.app.ui.auth.LoginActivity
import com.pixelpick.app.util.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.TimeZone

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProfileBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var profileRepository: ProfileRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        authRepository = AuthRepository(sessionManager)
        profileRepository = ProfileRepository(sessionManager)
        
        setupViews()
        loadProfile()
    }
    
    private fun setupViews() {
        binding.logoutButton.setOnClickListener {
            performLogout()
        }
        
        binding.deleteAccountButton.setOnClickListener {
            showDeleteAccountDialog()
        }
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.profile)
    }
    
    private fun loadProfile() {
        // Mostrar datos de la sesión primero mientras se cargan los datos actualizados
        val sessionUser = sessionManager.getUser()
        sessionUser?.let { user ->
            displayUserData(user, null, null)
        }
        
        showLoading(true)
        
        lifecycleScope.launch {
            val result = profileRepository.getProfile()
            
            showLoading(false)
            
            result.onSuccess { response ->
                response.user?.let { user ->
                    // Guardar usuario actualizado en sesión
                    sessionManager.saveUser(user)
                    displayUserData(user, response.stats, response.games)
                } ?: run {
                    // Si no hay usuario en la respuesta, usar datos de sesión
                    sessionUser?.let { user ->
                        displayUserData(user, response.stats, response.games)
                    }
                }
            }.onFailure { error ->
                val errorMessage = error.message ?: "Error desconocido"
                
                // Si la sesión expiró, redirigir al login
                if (errorMessage.contains("Sesión expirada") || errorMessage.contains("inicia sesión")) {
                    sessionManager.clearSession()
                    val intent = android.content.Intent(this@ProfileActivity, LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    return@launch
                }
                
                // Si falla la carga, mantener los datos de sesión si existen
                sessionUser?.let { user ->
                    displayUserData(user, null, null)
                    // Mostrar mensaje más corto si hay datos guardados
                    val shortMessage = when {
                        errorMessage.contains("Tiempo de espera") -> "El servidor está tardando en responder. Los datos mostrados pueden estar desactualizados."
                        errorMessage.contains("no puede conectar") -> "No se puede conectar al servidor. Los datos mostrados pueden estar desactualizados."
                        else -> "No se pudieron actualizar los datos. Mostrando información guardada."
                    }
                    Toast.makeText(this@ProfileActivity, shortMessage, Toast.LENGTH_LONG).show()
                } ?: run {
                    // Si no hay datos guardados, mostrar el error completo
                    android.app.AlertDialog.Builder(this@ProfileActivity)
                        .setTitle("Error al cargar perfil")
                        .setMessage(errorMessage)
                        .setPositiveButton("Reintentar") { _, _ -> loadProfile() }
                        .setNegativeButton("Cerrar", null)
                        .show()
                }
            }
        }
    }
    
    private fun displayUserData(
        user: com.pixelpick.app.data.models.User,
        stats: com.pixelpick.app.data.models.Stats?,
        recentGames: List<com.pixelpick.app.data.models.UserGame>?
    ) {
        // Formatear nombre completo - manejar strings vacíos o null
        val firstName = (user.firstName ?: "").trim()
        val lastName = (user.lastName ?: "").trim()
        val fullName = when {
            firstName.isNotEmpty() && lastName.isNotEmpty() -> "$firstName $lastName"
            firstName.isNotEmpty() -> firstName
            lastName.isNotEmpty() -> lastName
            else -> "Usuario"
        }
        
        binding.nameText.text = fullName
        binding.emailText.text = (user.email ?: "").ifEmpty { "email@ejemplo.com" }
        
        // Formatear y mostrar fecha de registro
        user.createdAt?.let { createdAtStr ->
            try {
                val date = parseDate(createdAtStr)
                if (date != null) {
                    val monthNames = arrayOf(
                        "enero", "febrero", "marzo", "abril", "mayo", "junio",
                        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
                    )
                    
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    val month = monthNames[calendar.get(Calendar.MONTH)]
                    val year = calendar.get(Calendar.YEAR)
                    
                    binding.joinedDateText.text = "Se unió en $month de $year"
                    binding.joinedDateLayout.visibility = View.VISIBLE
                } else {
                    binding.joinedDateText.text = "Se unió recientemente"
                    binding.joinedDateLayout.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // Si hay error al parsear, mostrar mensaje por defecto
                binding.joinedDateText.text = "Se unió recientemente"
                binding.joinedDateLayout.visibility = View.VISIBLE
            }
        } ?: run {
            binding.joinedDateText.text = "Se unió recientemente"
            binding.joinedDateLayout.visibility = View.VISIBLE
        }
        
        // Mostrar estadísticas
        stats?.let {
            binding.completedGamesText.text = it.completed.toString()
            binding.playingGamesText.text = it.playing.toString()
        } ?: run {
            // Valores por defecto si no hay estadísticas
            binding.completedGamesText.text = "0"
            binding.playingGamesText.text = "0"
        }
        
        // Actualizar mensaje del empty state con el nombre del usuario
        binding.emptyStateDescription.text = "$fullName no ha jugado juegos recientemente."
        
        // Mostrar juegos recientes o estado vacío
        recentGames?.let { games ->
            val playingGames = games.filter { it.status == "playing" || it.status == "completed" }
            if (playingGames.isNotEmpty()) {
                // Mostrar RecyclerView con juegos
                binding.recentGamesRecyclerView.visibility = View.VISIBLE
                binding.recentGamesEmptyState.visibility = View.GONE
                
                val adapter = RecentGamesAdapter(playingGames.take(5)) // Mostrar máximo 5 juegos
                binding.recentGamesRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.recentGamesRecyclerView.adapter = adapter
                
                // Actualizar badge del empty state con el número de juegos
                binding.emptyStateBadge.text = playingGames.size.toString()
            } else {
                // Mostrar estado vacío
                binding.recentGamesRecyclerView.visibility = View.GONE
                binding.recentGamesEmptyState.visibility = View.VISIBLE
                binding.emptyStateBadge.text = "0"
            }
        } ?: run {
            // Si no hay juegos, mostrar estado vacío
            binding.recentGamesRecyclerView.visibility = View.GONE
            binding.recentGamesEmptyState.visibility = View.VISIBLE
            binding.emptyStateBadge.text = "0"
        }
    }
    
    private fun performLogout() {
        lifecycleScope.launch {
            authRepository.logout()
            val intent = android.content.Intent(this@ProfileActivity, LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
                Toast.makeText(this@ProfileActivity, "Cuenta eliminada exitosamente", Toast.LENGTH_SHORT).show()
                val intent = android.content.Intent(this@ProfileActivity, LoginActivity::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure { error ->
                Toast.makeText(this@ProfileActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun parseDate(dateStr: String): Date? {
        val formats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd"
        )
        
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(dateStr)
            } catch (e: Exception) {
                // Intentar siguiente formato
            }
        }
        return null
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

