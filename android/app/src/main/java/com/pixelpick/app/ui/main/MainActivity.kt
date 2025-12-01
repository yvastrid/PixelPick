package com.pixelpick.app.ui.main

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.pixelpick.app.R
import com.pixelpick.app.data.api.RetrofitClient
import com.pixelpick.app.data.models.Game
import com.pixelpick.app.data.models.User
import com.pixelpick.app.data.repository.AuthRepository
import com.pixelpick.app.data.repository.GameRepository
import com.pixelpick.app.data.repository.SubscriptionRepository
import com.pixelpick.app.databinding.ActivityMainBinding
import com.pixelpick.app.databinding.ProfileDropdownBinding
import com.pixelpick.app.ui.game.GameActivity
import com.pixelpick.app.ui.profile.ProfileActivity
import com.pixelpick.app.ui.settings.SettingsActivity
import com.pixelpick.app.ui.subscription.BenefitsActivity
import com.pixelpick.app.util.onFailure
import com.pixelpick.app.util.onSuccess
import com.pixelpick.app.util.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var gameRepository: GameRepository
    private lateinit var subscriptionRepository: SubscriptionRepository
    private var profilePopupWindow: PopupWindow? = null
    private lateinit var recommendationsAdapter: RecommendationsAdapter
    private var isPremiumPlan: Boolean = false  // true si tiene plan premium, false si tiene básico
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        authRepository = AuthRepository(sessionManager)
        gameRepository = GameRepository()
        subscriptionRepository = SubscriptionRepository(RetrofitClient.apiService)
        
        setupViews()
        setupRecommendationsRecyclerView()
        animateViews()
        loadUserData()
        // Verificar estado de suscripción primero, luego cargar contenido según el plan
        checkSubscriptionStatus()
        // loadRecommendations() y loadCatalog() se llamarán desde applyPlanRestrictions()
    }
    
    override fun onResume() {
        super.onResume()
        // Recargar estado de suscripción y aplicar restricciones
        // Esto asegura que si el usuario cambió de plan, se actualicen las restricciones
        // Agregar un pequeño delay para asegurar que el backend haya procesado el cambio
        lifecycleScope.launch {
            kotlinx.coroutines.delay(500)  // Esperar 500ms para que el backend procese
            checkSubscriptionStatus()
        }
    }
    
    private fun animateViews() {
        // Animar elementos con delay escalonado
        binding.welcomeText.alpha = 0f
        binding.welcomeText.translationY = 30f
        binding.welcomeText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(300)
            .start()
        
        binding.exploreButton.alpha = 0f
        binding.exploreButton.translationY = 30f
        binding.exploreButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(500)
            .start()
        
        binding.catalogButton.alpha = 0f
        binding.catalogButton.translationY = 30f
        binding.catalogButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(600)
            .start()
    }
    
    private fun setupViews() {
        binding.subscribeButton.setOnClickListener {
            // Navegar a BenefitsActivity en modo "upgrade" (desde Suscríbete ahora)
            val intent = Intent(this, BenefitsActivity::class.java)
            intent.putExtra("mode", "upgrade")
            startActivity(intent)
        }
        
        binding.profileButton.setOnClickListener { view ->
            // Mostrar PopupMenu de perfil
            showProfileMenu(view)
        }
        
        binding.exploreButton.setOnClickListener {
            // Scroll a la sección de recomendaciones IA
            binding.root.post {
                val section = binding.aiRecommendationsSection
                val scrollView = binding.mainScrollView
                val location = IntArray(2)
                section.getLocationOnScreen(location)
                val y = location[1] - scrollView.top
                scrollView.smoothScrollTo(0, y)
            }
        }
        
        binding.catalogButton.setOnClickListener {
            // Scroll a la sección de catálogo completo
            binding.root.post {
                val section = binding.catalogSection
                val scrollView = binding.mainScrollView
                val location = IntArray(2)
                section.getLocationOnScreen(location)
                val y = location[1] - scrollView.top
                scrollView.smoothScrollTo(0, y)
            }
        }
    }
    
    private fun checkSubscriptionStatus() {
        lifecycleScope.launch {
            android.util.Log.d("MainActivity", "=== VERIFICANDO ESTADO DE SUSCRIPCIÓN ===")
            val result = subscriptionRepository.getSubscriptionStatus()
            result.onSuccess { statusResponse ->
                android.util.Log.d("MainActivity", "Estado de suscripción recibido: hasSubscription=${statusResponse.hasSubscription}")
                if (statusResponse.hasSubscription && statusResponse.subscription != null) {
                    val subscription = statusResponse.subscription
                    val planType = subscription.planType ?: ""
                    val status = subscription.status ?: ""
                    android.util.Log.d("MainActivity", "Plan type: '$planType'")
                    android.util.Log.d("MainActivity", "Status: '$status'")
                    
                    // Verificar si es plan premium
                    // El plan premium tiene plan_type='pixelie_plan' (sin guión bajo después de pixelie)
                    isPremiumPlan = planType.equals("pixelie_plan", ignoreCase = true) ||
                                   (planType.contains("pixelie", ignoreCase = true) && 
                                    !planType.contains("basic", ignoreCase = true) &&
                                    planType.contains("plan", ignoreCase = true))
                    
                    android.util.Log.d("MainActivity", "isPremiumPlan determinado: $isPremiumPlan")
                    android.util.Log.d("MainActivity", "Comparación detallada:")
                    android.util.Log.d("MainActivity", "  - planType.equals('pixelie_plan'): ${planType.equals("pixelie_plan", ignoreCase = true)}")
                    android.util.Log.d("MainActivity", "  - contiene 'pixelie' y no 'basic': ${planType.contains("pixelie", ignoreCase = true) && !planType.contains("basic", ignoreCase = true)}")
                } else {
                    // Si no tiene suscripción, es plan básico por defecto
                    android.util.Log.d("MainActivity", "No tiene suscripción o subscription es null, usando plan básico")
                    isPremiumPlan = false
                }
                
                    android.util.Log.d("MainActivity", "=== FIN VERIFICACIÓN. isPremiumPlan final: $isPremiumPlan ===")
                
                // Si es premium, redirigir a MainActivityPremium
                if (isPremiumPlan) {
                    android.util.Log.d("MainActivity", "Plan premium detectado, redirigiendo a MainActivityPremium")
                    val intent = Intent(this@MainActivity, MainActivityPremium::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    return@onSuccess
                }
                
                // Aplicar restricciones según el plan (solo para básico)
                applyPlanRestrictions()
            }.onFailure { error ->
                // En caso de error, asumir plan básico
                android.util.Log.e("MainActivity", "Error al verificar suscripción: ${error.message}")
                error.printStackTrace()
                isPremiumPlan = false
                applyPlanRestrictions()
            }
        }
    }
    
    private fun applyPlanRestrictions() {
        android.util.Log.d("MainActivity", "=== APLICANDO RESTRICCIONES DE PLAN ===")
        android.util.Log.d("MainActivity", "isPremiumPlan: $isPremiumPlan")
        
        // Ocultar/mostrar sección de Recomendaciones IA según el plan
        if (isPremiumPlan) {
            android.util.Log.d("MainActivity", "✅ Plan premium: Mostrando recomendaciones IA")
            binding.aiRecommendationsSection.visibility = View.VISIBLE
            binding.exploreButton.visibility = View.VISIBLE  // Mostrar botón de explorar recomendaciones
            loadRecommendations()
        } else {
            android.util.Log.d("MainActivity", "❌ Plan básico: Ocultando recomendaciones IA")
            binding.aiRecommendationsSection.visibility = View.GONE
            binding.exploreButton.visibility = View.GONE  // Ocultar botón de explorar recomendaciones
        }
        
        // Recargar catálogo con restricciones aplicadas
        // Esto actualizará el adapter con el estado correcto de isPremiumPlan
        android.util.Log.d("MainActivity", "Recargando catálogo con isPremiumPlan: $isPremiumPlan")
        loadCatalog()
        
        android.util.Log.d("MainActivity", "=== FIN APLICACIÓN DE RESTRICCIONES ===")
    }
    
    private fun showProfileMenu(anchor: View) {
        // Cerrar popup anterior si existe
        profilePopupWindow?.dismiss()
        
        // Inflar el layout del dropdown
        val dropdownBinding = ProfileDropdownBinding.inflate(LayoutInflater.from(this))
        
        // Cargar datos del usuario
        val user = sessionManager.getUser()
        if (user != null) {
            val fullName = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
            dropdownBinding.profileName.text = if (fullName.isNotEmpty()) fullName else "Usuario"
            dropdownBinding.profileEmail.text = user.email ?: "email@ejemplo.com"
        } else {
            dropdownBinding.profileName.text = "Usuario"
            dropdownBinding.profileEmail.text = "email@ejemplo.com"
        }
        
        // Configurar listeners
        dropdownBinding.menuProfile.setOnClickListener {
            profilePopupWindow?.dismiss()
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        
        dropdownBinding.menuSettings.setOnClickListener {
            profilePopupWindow?.dismiss()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        dropdownBinding.menuSubscription.setOnClickListener {
            profilePopupWindow?.dismiss()
            // Navegar a BenefitsActivity en modo "view" (desde menú de perfil)
            val intent = Intent(this, BenefitsActivity::class.java)
            intent.putExtra("mode", "view")
            startActivity(intent)
        }
        
        dropdownBinding.menuLogout.setOnClickListener {
            profilePopupWindow?.dismiss()
            performLogout()
        }
        
        // Medir el layout antes de crear el PopupWindow
        dropdownBinding.root.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        
        // Crear PopupWindow
        profilePopupWindow = PopupWindow(
            dropdownBinding.root,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            
            setOnDismissListener {
                profilePopupWindow = null
            }
            
            // Calcular posición - alinear a la derecha del botón
            val location = IntArray(2)
            anchor.getLocationOnScreen(location)
            val rootLocation = IntArray(2)
            binding.root.getLocationOnScreen(rootLocation)
            
            val dropdownWidth = dropdownBinding.root.measuredWidth
            val anchorWidth = anchor.width
            val x = location[0] - rootLocation[0] + anchorWidth - dropdownWidth
            val y = location[1] - rootLocation[1] + anchor.height + 8
            
            showAtLocation(binding.root, Gravity.NO_GRAVITY, x, y)
        }
    }
    
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        // Ocultar el menú de opciones del ActionBar ya que usamos íconos en el header
        menu?.clear()
        return false
    }
    
    private fun loadUserData() {
        val user = sessionManager.getUser()
        if (user != null) {
            // El texto de bienvenida se mantiene como "juego favorito"
            // Podemos agregar un saludo personalizado más abajo si es necesario
        }
    }
    
    private fun setupRecommendationsRecyclerView() {
        recommendationsAdapter = RecommendationsAdapter(emptyList()) { game ->
            // Callback cuando se hace clic en un juego recomendado
            android.util.Log.d("MainActivity", "Clic en juego recomendado: ${game.name}")
        }
        binding.gamesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 1)
            adapter = recommendationsAdapter
        }
    }
    
    private fun loadRecommendations() {
        lifecycleScope.launch {
            val result = gameRepository.getRecommendations()
            result.onSuccess { games ->
                android.util.Log.d("MainActivity", "Recomendaciones cargadas: ${games.size} juegos")
                if (games.isNotEmpty()) {
                    recommendationsAdapter = RecommendationsAdapter(games) { game ->
                        android.util.Log.d("MainActivity", "Clic en juego recomendado: ${game.name}")
                    }
                    binding.gamesRecyclerView.adapter = recommendationsAdapter
                    binding.gamesRecyclerView.visibility = View.VISIBLE
                    binding.emptyStateLayout.visibility = View.GONE
                } else {
                    binding.gamesRecyclerView.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.VISIBLE
                }
            }.onFailure { error ->
                android.util.Log.e("MainActivity", "Error al cargar recomendaciones: ${error.message}")
                binding.gamesRecyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
            }
        }
    }
    
    private fun loadCatalog() {
        lifecycleScope.launch {
            val result = gameRepository.getGames()
            result.onSuccess { games ->
                android.util.Log.d("MainActivity", "=== INICIO CARGA CATÁLOGO ===")
                android.util.Log.d("MainActivity", "Total juegos recibidos del servidor: ${games.size}")
                
                // Log de todos los juegos recibidos
                games.forEachIndexed { index, game ->
                    android.util.Log.d("MainActivity", "Juego[$index]: nombre='${game.name}', precio=${game.price}, gameUrl='${game.gameUrl}'")
                }
                
                // Filtrar solo los 5 juegos chistosos (gratuitos)
                val funnyGameNames = listOf("Frootilupis Match", "Chocopops Volador", "SnackAttack Laberinto", "CerealKiller Connect", "Munchies Memory")
                // También incluir nombres antiguos para compatibilidad
                val oldFunnyGameNames = listOf("Flootilupis", "Chocopops", "SnackAttack", "CerealKiller", "Munchies")
                val allFunnyGameNames = funnyGameNames + oldFunnyGameNames
                
                // Primero filtrar por nombre
                val gamesByName = games.filter { it.name in allFunnyGameNames }
                android.util.Log.d("MainActivity", "Juegos filtrados por nombre: ${gamesByName.size}")
                gamesByName.forEach { game ->
                    android.util.Log.d("MainActivity", "  - ${game.name} (precio: ${game.price})")
                }
                
                // Luego filtrar por precio (gratuitos)
                var catalogGames = gamesByName.filter { it.price == 0.0 }
                android.util.Log.d("MainActivity", "Juegos finales después de filtrar por precio: ${catalogGames.size}")
                
                // Si no se encontraron juegos del servidor, usar juegos hardcodeados como fallback
                if (catalogGames.isEmpty()) {
                    android.util.Log.w("MainActivity", "⚠️ No se encontraron juegos chistosos en el servidor, usando fallback local")
                    catalogGames = getHardcodedGames()
                }
                
                catalogGames.forEach { game ->
                    android.util.Log.d("MainActivity", "  ✓ ${game.name}")
                }
                
                if (catalogGames.isEmpty()) {
                    // Mostrar empty state
                    android.util.Log.w("MainActivity", "⚠️ No se encontraron juegos chistosos en el catálogo")
                    binding.catalogRecyclerView.visibility = View.GONE
                    binding.catalogEmptyStateLayout.visibility = View.VISIBLE
                } else {
                    android.util.Log.d("MainActivity", "✅ Mostrando ${catalogGames.size} juegos en el catálogo")
                    binding.catalogRecyclerView.visibility = View.VISIBLE
                    binding.catalogEmptyStateLayout.visibility = View.GONE
                    android.util.Log.d("MainActivity", "Creando CatalogAdapter con isPremiumPlan: $isPremiumPlan")
                    val adapter = CatalogAdapter(catalogGames, isPremiumPlan) { game ->
                        val gameIndex = catalogGames.indexOfFirst { it.id == game.id }
                        android.util.Log.d("MainActivity", "Click en juego: ${game.name}, índice: $gameIndex, isPremiumPlan: $isPremiumPlan")
                        
                        if (!isPremiumPlan && gameIndex > 0) {
                            // Plan básico: solo el primer juego (índice 0) está desbloqueado
                            Toast.makeText(this@MainActivity, "Actualiza a Premium para desbloquear este juego", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@MainActivity, BenefitsActivity::class.java)
                            intent.putExtra("mode", "upgrade")
                            startActivity(intent)
                        } else {
                            // Plan premium o primer juego: abrir el juego
                            val intent = Intent(this@MainActivity, GameActivity::class.java)
                            intent.putExtra("game_url", game.gameUrl)
                            intent.putExtra("game_id", game.id)
                            intent.putExtra("game_name", game.name)
                            startActivity(intent)
                        }
                    }
                    // Carrusel horizontal
                    val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                        this@MainActivity,
                        androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.catalogRecyclerView.layoutManager = layoutManager
                    binding.catalogRecyclerView.adapter = adapter
                }
                android.util.Log.d("MainActivity", "=== FIN CARGA CATÁLOGO ===")
            }.onFailure { error ->
                // En caso de error, usar juegos hardcodeados como fallback
                android.util.Log.e("MainActivity", "❌ Error al cargar catálogo: ${error.message}", error)
                error.printStackTrace()
                
                // Usar juegos hardcodeados como fallback
                val fallbackGames = getHardcodedGames()
                if (fallbackGames.isNotEmpty()) {
                    android.util.Log.d("MainActivity", "✅ Usando ${fallbackGames.size} juegos hardcodeados como fallback")
                    binding.catalogRecyclerView.visibility = View.VISIBLE
                    binding.catalogEmptyStateLayout.visibility = View.GONE
                    val adapter = CatalogAdapter(fallbackGames) { game ->
                        Toast.makeText(this@MainActivity, "Este juego no está disponible aún", Toast.LENGTH_SHORT).show()
                    }
                    // Carrusel horizontal
                    val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                        this@MainActivity,
                        androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.catalogRecyclerView.layoutManager = layoutManager
                    binding.catalogRecyclerView.adapter = adapter
                } else {
                    binding.catalogRecyclerView.visibility = View.GONE
                    binding.catalogEmptyStateLayout.visibility = View.VISIBLE
                }
            }
        }
    }
    
    // Juegos hardcodeados como fallback si el servidor no responde
    private fun getHardcodedGames(): List<com.pixelpick.app.data.models.Game> {
        return listOf(
            com.pixelpick.app.data.models.Game(
                id = 1,
                name = "Frootilupis Match",
                description = "🍩 ¡Combina 3 o más cereales del mismo color! Un juego adictivo donde los cereales vuelan y explotan con efectos increíbles. ¿Tendrás lo necesario para alcanzar el puntaje más alto?",
                price = 0.0,
                platforms = listOf("Android"),
                imageUrl = null,
                gameUrl = "flootilupis.html",
                category = "Match-3"
            ),
            com.pixelpick.app.data.models.Game(
                id = 2,
                name = "Chocopops Volador",
                description = "🍫 ¡Vuela como un chocolate loco! Toca la pantalla para hacer volar tu chocolate y esquiva los obstáculos verdes. ¿Podrás llegar más lejos que tus amigos?",
                price = 0.0,
                platforms = listOf("Android"),
                imageUrl = null,
                gameUrl = "chocopops.html",
                category = "Arcade"
            ),
            com.pixelpick.app.data.models.Game(
                id = 3,
                name = "SnackAttack Laberinto",
                description = "🍿 ¡Come todos los snacks antes de que los fantasmas te atrapen! Recolecta puntos dorados y usa los power pellets para convertirte en el rey del laberinto.",
                price = 0.0,
                platforms = listOf("Android"),
                imageUrl = null,
                gameUrl = "snackattack.html",
                category = "Arcade"
            ),
            com.pixelpick.app.data.models.Game(
                id = 4,
                name = "CerealKiller Connect",
                description = "🥣 ¡Conecta los cereales del mismo color sin que se crucen! Dibuja líneas táctiles para unir los puntos. Cada nivel es más difícil que el anterior. ¿Podrás con el desafío?",
                price = 0.0,
                platforms = listOf("Android"),
                imageUrl = null,
                gameUrl = "cerealkiller.html",
                category = "Puzzle"
            ),
            com.pixelpick.app.data.models.Game(
                id = 5,
                name = "Munchies Memory",
                description = "🧠 ¡Encuentra todos los pares de snacks antes de que se acabe el tiempo! Entrena tu memoria con este juego relajante lleno de deliciosos snacks. ¿Tienes buena memoria?",
                price = 0.0,
                platforms = listOf("Android"),
                imageUrl = null,
                gameUrl = "munchies.html",
                category = "Memory"
            )
        )
    }
    
    
    private fun performLogout() {
        lifecycleScope.launch {
            authRepository.logout()
            // Navegar a LoginActivity
            val intent = Intent(this@MainActivity, com.pixelpick.app.ui.auth.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}

