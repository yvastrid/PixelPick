package com.pixelpick.app.ui.subscription

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pixelpick.app.R
import com.pixelpick.app.databinding.ActivityBenefitsBinding
import com.pixelpick.app.data.api.RetrofitClient
import com.pixelpick.app.data.repository.SubscriptionRepository
import com.pixelpick.app.ui.auth.LoginActivity
import com.pixelpick.app.ui.main.MainActivity
import com.pixelpick.app.util.SessionManager
import com.pixelpick.app.util.onFailure
import com.pixelpick.app.util.onSuccess
import kotlinx.coroutines.launch

class BenefitsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBenefitsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var subscriptionRepository: SubscriptionRepository
    private var mode: String = "view" // "view" o "upgrade"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBenefitsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        subscriptionRepository = SubscriptionRepository(RetrofitClient.apiService)
        
        // Obtener el modo desde el Intent
        mode = intent.getStringExtra("mode") ?: "view"
        
        setupViews()
        
        // Luego cargar el estado real de la suscripción
        binding.root.post {
            loadSubscriptionStatus()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refrescar el estado de suscripción cuando el usuario vuelve a esta pantalla
        loadSubscriptionStatus()
    }
    
    private fun setupViews() {
        // Verificar si el usuario está logueado
        if (!sessionManager.isLoggedIn()) {
            // Si no está logueado, redirigir a login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // Botón para seleccionar plan básico
        binding.selectBasicButton.setOnClickListener {
            activateBasicPlan()
        }
        
        // Botón para comprar plan premium (sin pago, solo para testing)
        binding.purchaseButton.setOnClickListener {
            activatePremiumPlan()
        }
        
        binding.cancelButton.setOnClickListener {
            finish()
        }
    }
    
    private fun activateBasicPlan() {
        lifecycleScope.launch {
            android.util.Log.d("BenefitsActivity", "=== ACTIVANDO PLAN BÁSICO ===")
            val result = subscriptionRepository.activateBasicPlan()
            result.onSuccess { message ->
                android.util.Log.d("BenefitsActivity", "✅ Plan básico activado: $message")
                Toast.makeText(
                    this@BenefitsActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                
                // Esperar un momento para que el mensaje se muestre
                kotlinx.coroutines.delay(1500)
                
                // Verificar el plan después de activar básico
                val verifyResult = subscriptionRepository.getSubscriptionStatus()
                verifyResult.onSuccess { statusResponse ->
                    val planType = statusResponse.subscription?.planType ?: ""
                    val hasPremiumAccess = statusResponse.subscription?.hasPremiumAccess == true
                    val isPremium = planType.equals("pixelie_plan", ignoreCase = true) || hasPremiumAccess
                    
                    android.util.Log.d("BenefitsActivity", "Plan verificado después de activar básico: planType=$planType, hasPremiumAccess=$hasPremiumAccess, isPremium=$isPremium")
                    
                    // Redirigir a la Activity correspondiente según el plan
                    val intent = if (isPremium && statusResponse.hasSubscription) {
                        // Si aún es premium (periodo pagado activo), volver a premium
                        android.util.Log.d("BenefitsActivity", "✅ Redirigiendo a MainActivityPremium (periodo pagado activo)")
                        Intent(this@BenefitsActivity, com.pixelpick.app.ui.main.MainActivityPremium::class.java)
                    } else {
                        // Si ya cambió a básico, ir a básico
                        android.util.Log.d("BenefitsActivity", "✅ Redirigiendo a MainActivity (plan básico)")
                        Intent(this@BenefitsActivity, MainActivity::class.java)
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }.onFailure { error ->
                    android.util.Log.e("BenefitsActivity", "❌ Error al verificar estado: ${error.message}")
                    // En caso de error, ir a MainActivity básico
                    val intent = Intent(this@BenefitsActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }.onFailure { error ->
                android.util.Log.e("BenefitsActivity", "❌ Error al activar plan básico: ${error.message}")
                error.printStackTrace()
                Toast.makeText(
                    this@BenefitsActivity,
                    "Error al activar plan básico: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun activatePremiumPlan() {
        lifecycleScope.launch {
            android.util.Log.d("BenefitsActivity", "=== ACTIVANDO PLAN PREMIUM ===")
            
            // Verificar primero si ya tiene periodo pagado activo
            val checkResult = subscriptionRepository.getSubscriptionStatus()
            checkResult.onSuccess { statusResponse ->
                val subscription = statusResponse.subscription
                val currentPeriodEnd = subscription?.currentPeriodEnd
                
                if (currentPeriodEnd != null) {
                    // Verificar si el periodo aún está activo
                    try {
                        val periodEndDate = java.time.Instant.parse(currentPeriodEnd).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                        val now = java.time.LocalDateTime.now()
                        
                        if (periodEndDate.isAfter(now)) {
                            // Aún tiene periodo pagado activo, bloquear suscripción
                            android.util.Log.d("BenefitsActivity", "❌ Usuario tiene periodo pagado activo hasta $currentPeriodEnd")
                            Toast.makeText(
                                this@BenefitsActivity,
                                "Ya tienes un periodo pagado activo. No puedes volver a suscribirte hasta que termine tu periodo actual.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@onSuccess
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BenefitsActivity", "Error al parsear fecha: ${e.message}")
                    }
                }
                
                // Proceder con la activación
                val result = subscriptionRepository.activatePremiumPlan()
                result.onSuccess { message ->
                    android.util.Log.d("BenefitsActivity", "✅ Plan premium activado exitosamente en backend: $message")
                    Toast.makeText(
                        this@BenefitsActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                
                    // Esperar un momento para que el mensaje se muestre y el backend procese
                    kotlinx.coroutines.delay(1500)  // Aumentar delay para asegurar que el backend procese
                    
                    // Verificar que el cambio se haya guardado antes de volver
                    android.util.Log.d("BenefitsActivity", "Verificando estado actualizado después de activar premium...")
                    val verifyResult = subscriptionRepository.getSubscriptionStatus()
                    verifyResult.onSuccess { statusResponse ->
                        android.util.Log.d("BenefitsActivity", "Estado verificado: hasSubscription=${statusResponse.hasSubscription}")
                        
                        val planType = statusResponse.subscription?.planType ?: ""
                        val status = statusResponse.subscription?.status ?: ""
                        android.util.Log.d("BenefitsActivity", "Plan type verificado: '$planType'")
                        android.util.Log.d("BenefitsActivity", "Status verificado: '$status'")
                        
                        // Verificar si es plan premium (considerar acceso premium por periodo pagado)
                        val hasPremiumAccess = statusResponse.subscription?.hasPremiumAccess == true
                        val isPremium = planType.equals("pixelie_plan", ignoreCase = true) || hasPremiumAccess
                        
                        android.util.Log.d("BenefitsActivity", "hasPremiumAccess: $hasPremiumAccess")
                        android.util.Log.d("BenefitsActivity", "isPremium determinado: $isPremium")
                        
                        // Redirigir a la Activity correspondiente según el plan
                        val intent = if (isPremium && statusResponse.hasSubscription) {
                            android.util.Log.d("BenefitsActivity", "✅ Redirigiendo a MainActivityPremium")
                            Intent(this@BenefitsActivity, com.pixelpick.app.ui.main.MainActivityPremium::class.java)
                        } else {
                            android.util.Log.d("BenefitsActivity", "❌ Redirigiendo a MainActivity (básico)")
                            Intent(this@BenefitsActivity, MainActivity::class.java)
                        }
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }.onFailure { error ->
                        android.util.Log.e("BenefitsActivity", "❌ Error al verificar estado: ${error.message}")
                        error.printStackTrace()
                        // Aún así, intentar redirigir a premium (asumir que se activó correctamente)
                        android.util.Log.d("BenefitsActivity", "Asumiendo que el plan se activó correctamente, redirigiendo a premium")
                        val intent = Intent(this@BenefitsActivity, com.pixelpick.app.ui.main.MainActivityPremium::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }.onFailure { error ->
                    android.util.Log.e("BenefitsActivity", "❌ Error al activar plan premium: ${error.message}")
                    error.printStackTrace()
                    Toast.makeText(
                        this@BenefitsActivity,
                        "Error al activar plan premium: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }.onFailure { error ->
                android.util.Log.e("BenefitsActivity", "❌ Error al verificar periodo pagado: ${error.message}")
                error.printStackTrace()
                Toast.makeText(
                    this@BenefitsActivity,
                    "Error al verificar periodo pagado: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun loadSubscriptionStatus() {
        lifecycleScope.launch {
            android.util.Log.d("BenefitsActivity", "=== CARGANDO ESTADO DE SUSCRIPCIÓN ===")
            val result = subscriptionRepository.getSubscriptionStatus()
            result.onSuccess { statusResponse ->
                android.util.Log.d("BenefitsActivity", "Estado recibido: hasSubscription=${statusResponse.hasSubscription}")
                if (statusResponse.hasSubscription && statusResponse.subscription != null) {
                    val subscription = statusResponse.subscription
                    val planType = subscription.planType ?: ""
                    val status = subscription.status ?: ""
                    android.util.Log.d("BenefitsActivity", "🔍 Plan type recibido: '$planType'")
                    android.util.Log.d("BenefitsActivity", "🔍 Status recibido: '$status'")
                    android.util.Log.d("BenefitsActivity", "🔍 Plan type length: ${planType.length}")
                    android.util.Log.d("BenefitsActivity", "🔍 Plan type bytes: ${planType.toByteArray().contentToString()}")
                    
                    // Verificar tipo de plan - comparación más estricta
                    val isBasicPlan = planType.equals("pixelie_basic", ignoreCase = true) ||
                                     planType.equals("pixelie_basic_plan", ignoreCase = true) ||
                                     planType.contains("basic", ignoreCase = true)
                    
                    // El plan premium tiene plan_type='pixelie_plan' exactamente
                    // O tiene acceso premium por periodo pagado activo
                    val hasPremiumAccess = subscription.hasPremiumAccess == true
                    val isPremiumPlan = planType.equals("pixelie_plan", ignoreCase = true) || hasPremiumAccess
                    
                    // Verificar si tiene periodo pagado activo (aunque plan_type sea básico)
                    // Esto ocurre cuando cambió a básico pero aún tiene periodo pagado activo
                    val hasPaidPeriodActive = hasPremiumAccess && isBasicPlan && 
                                             subscription.currentPeriodEnd != null &&
                                             subscription.cancelAtPeriodEnd == true
                    
                    android.util.Log.d("BenefitsActivity", "hasPremiumAccess: $hasPremiumAccess")
                    android.util.Log.d("BenefitsActivity", "currentPeriodEnd: ${subscription.currentPeriodEnd}")
                    android.util.Log.d("BenefitsActivity", "cancelAtPeriodEnd: ${subscription.cancelAtPeriodEnd}")
                    android.util.Log.d("BenefitsActivity", "hasPaidPeriodActive: $hasPaidPeriodActive")
                    
                    android.util.Log.d("BenefitsActivity", "✅ isBasicPlan: $isBasicPlan")
                    android.util.Log.d("BenefitsActivity", "✅ isPremiumPlan: $isPremiumPlan")
                    android.util.Log.d("BenefitsActivity", "✅ Comparación 'pixelie_plan': ${planType.equals("pixelie_plan", ignoreCase = true)}")
                    
                    // Determinar el nombre del plan
                    val planName = when {
                        isPremiumPlan -> {
                            android.util.Log.d("BenefitsActivity", "✅ Mostrando 'Pixelie Plan'")
                            "Pixelie Plan"
                        }
                        isBasicPlan -> {
                            android.util.Log.d("BenefitsActivity", "✅ Mostrando 'Pixelie Basic Plan'")
                            "Pixelie Basic Plan"
                        }
                        else -> {
                            android.util.Log.d("BenefitsActivity", "⚠️ Plan desconocido, usando básico por defecto")
                            "Pixelie Basic Plan"
                        }
                    }
                    
                    android.util.Log.d("BenefitsActivity", "✅ Plan name final: '$planName'")
                    binding.activePlanName.text = planName
                    val statusText = when (subscription.status?.lowercase()) {
                        "active" -> "Activo"
                        "cancelled" -> "Cancelado"
                        "expired" -> "Expirado"
                        else -> subscription.status ?: "Activo"
                    }
                    binding.activePlanStatus.text = statusText
                    
                    // Aplicar lógica según el modo
                    if (mode == "upgrade") {
                        // Modo "Suscríbete ahora": mostrar el plan opuesto para cambiar
                        if (hasPaidPeriodActive) {
                            // Tiene periodo pagado activo (cambió a básico pero aún tiene premium activo)
                            // Bloquear suscripción premium y mostrar nota
                            applyUpgradeModeBasicBlocked(hasPaidPeriodActive, subscription.currentPeriodEnd)
                        } else if (isBasicPlan && subscription.status == "active") {
                            // Tiene básico sin periodo pagado → mostrar premium para upgrade
                            applyUpgradeModeBasic()
                        } else if (isPremiumPlan && subscription.status == "active") {
                            // Tiene premium → mostrar básico como opción para cambiar
                            // Verificar si hay periodo pagado activo para mostrar nota
                            val hasPaidPeriod = subscription.currentPeriodEnd != null && 
                                               subscription.cancelAtPeriodEnd == true
                            applyUpgradeModePremiumForChange(hasPaidPeriod, subscription.currentPeriodEnd)
                        } else {
                            // Por defecto, mostrar premium
                            applyUpgradeModeBasic()
                        }
                    } else {
                        // Modo "view" (desde menú de perfil): mostrar solo plan activo (sin opción de cambiar)
                        if (hasPaidPeriodActive) {
                            // Tiene periodo pagado activo pero plan_type es básico
                            // Mostrar como premium con nota informativa
                            applyPremiumPlanViewOnly(true, subscription.currentPeriodEnd)
                        } else if (isBasicPlan && subscription.status == "active") {
                            applyBasicPlanSelectedState()
                        } else if (isPremiumPlan && subscription.status == "active") {
                            // Solo mostrar el plan premium, sin opción de cambiar
                            // Verificar si hay periodo pagado activo para mostrar nota
                            val hasPaidPeriod = subscription.currentPeriodEnd != null && 
                                               subscription.cancelAtPeriodEnd == true
                            applyPremiumPlanViewOnly(hasPaidPeriod, subscription.currentPeriodEnd)
                        } else {
                            // Por defecto, básico
                            applyBasicPlanSelectedState()
                        }
                    }
                } else {
                    // Si no tiene suscripción
                    binding.activePlanName.text = "Pixelie Basic Plan"
                    binding.activePlanStatus.text = "Activo"
                    
                    if (mode == "upgrade") {
                        // Modo upgrade: mostrar premium para que pueda suscribirse
                        applyUpgradeModeBasic()
                    } else {
                        // Modo view: mostrar básico como activo
                        applyBasicPlanSelectedState()
                    }
                }
            }.onFailure { error ->
                // En caso de error
                binding.activePlanName.text = "Pixelie Basic Plan"
                binding.activePlanStatus.text = "Activo"
                
                if (mode == "upgrade") {
                    applyUpgradeModeBasic()
                } else {
                    applyBasicPlanSelectedState()
                }
            }
        }
    }
    
    private fun applyUpgradeModeBasic() {
        // Modo upgrade cuando tiene plan básico: mostrar solo premium para upgrade
        binding.basicPlanCard.visibility = View.GONE
        binding.premiumPlanCard.visibility = View.VISIBLE
        binding.premiumPlanCard.alpha = 1f
        binding.purchaseButton.isEnabled = true
        binding.purchaseButton.isClickable = true
        
        // Ocultar nota
        binding.planChangeNote.visibility = View.GONE
    }
    
    private fun applyUpgradeModePremium() {
        // Modo upgrade cuando tiene plan premium: mostrar básico difuminado (no puede downgrade)
        binding.basicPlanCard.visibility = View.VISIBLE
        binding.basicPlanCard.alpha = 0.6f
        binding.selectBasicButton.text = "No disponible"
        binding.selectBasicButton.isEnabled = false
        binding.selectBasicButton.isClickable = false
        binding.premiumPlanCard.visibility = View.GONE
    }
    
    private fun applyUpgradeModePremiumForChange(hasPaidPeriod: Boolean = false, periodEnd: String? = null) {
        // Modo upgrade cuando tiene plan premium: mostrar básico como opción para cambiar
        binding.basicPlanCard.visibility = View.VISIBLE
        binding.basicPlanCard.alpha = 1f
        binding.selectBasicButton.text = "Cambiar a Plan Básico"
        binding.selectBasicButton.isEnabled = true
        binding.selectBasicButton.isClickable = true
        binding.premiumPlanCard.visibility = View.GONE
        
        // Mostrar nota si hay periodo pagado activo
        if (hasPaidPeriod && periodEnd != null) {
            try {
                val periodEndDate = java.time.Instant.parse(periodEnd).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                val formattedDate = java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", java.util.Locale("es"))
                val dateString = periodEndDate.format(formattedDate)
                
                binding.planChangeNote.visibility = View.VISIBLE
                binding.planChangeNote.text = "Nota: Tienes acceso premium hasta el $dateString debido a tu periodo pagado. El cambio al plan básico se aplicará automáticamente después de esa fecha."
            } catch (e: Exception) {
                android.util.Log.e("BenefitsActivity", "Error al formatear fecha: ${e.message}")
                binding.planChangeNote.visibility = View.VISIBLE
                binding.planChangeNote.text = "Nota: Tienes acceso premium hasta que termine tu periodo pagado. El cambio al plan básico se aplicará automáticamente después de esa fecha."
            }
        } else {
            binding.planChangeNote.visibility = View.GONE
        }
    }
    
    private fun applyPremiumPlanSelectedState(hasPaidPeriod: Boolean = false) {
        // Modo view cuando tiene plan premium: mostrar ambos planes, premium marcado
        binding.basicPlanCard.visibility = View.VISIBLE
        binding.basicPlanCard.alpha = 1f
        binding.selectBasicButton.text = "Cambiar a Plan Básico"
        binding.selectBasicButton.isEnabled = true
        binding.selectBasicButton.isClickable = true
        binding.premiumPlanCard.visibility = View.VISIBLE
        binding.premiumPlanCard.alpha = 0.6f
        binding.purchaseButton.text = "Plan Actual"
        binding.purchaseButton.isEnabled = false
        binding.purchaseButton.isClickable = false
        
        // Mostrar nota sobre cambio de plan solo cuando hay periodo pagado activo
        if (hasPaidPeriod) {
            binding.planChangeNote.visibility = View.VISIBLE
            binding.planChangeNote.text = "Nota: Si tienes un periodo pagado activo, el cambio al plan básico se aplicará al finalizar tu periodo actual. Se respetará el tiempo que ya pagaste."
        } else {
            binding.planChangeNote.visibility = View.GONE
        }
    }
    
    private fun applyPremiumPlanViewOnly(hasPaidPeriod: Boolean = false, periodEnd: String? = null) {
        // Modo view cuando tiene plan premium: mostrar solo el plan premium (sin opción de cambiar)
        binding.basicPlanCard.visibility = View.GONE
        binding.premiumPlanCard.visibility = View.VISIBLE
        binding.premiumPlanCard.alpha = 0.6f
        binding.purchaseButton.text = "Plan Actual"
        binding.purchaseButton.isEnabled = false
        binding.purchaseButton.isClickable = false
        
        // Mostrar nota si hay periodo pagado activo y cancel_at_period_end es true
        if (hasPaidPeriod && periodEnd != null) {
            try {
                val periodEndDate = java.time.Instant.parse(periodEnd).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                val formattedDate = java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", java.util.Locale("es"))
                val dateString = periodEndDate.format(formattedDate)
                
                binding.planChangeNote.visibility = View.VISIBLE
                binding.planChangeNote.text = "Nota: Tienes acceso premium hasta el $dateString debido a tu periodo pagado. Después de esa fecha, tu plan cambiará automáticamente al plan básico."
            } catch (e: Exception) {
                android.util.Log.e("BenefitsActivity", "Error al formatear fecha: ${e.message}")
                binding.planChangeNote.visibility = View.VISIBLE
                binding.planChangeNote.text = "Nota: Tienes acceso premium hasta que termine tu periodo pagado. Después de esa fecha, tu plan cambiará automáticamente al plan básico."
            }
        } else {
            binding.planChangeNote.visibility = View.GONE
        }
    }
    
    private fun applyBasicPlanSelectedState() {
        // Difuminar el card del plan básico (reducir opacidad) - aplicar inmediatamente
        binding.basicPlanCard.alpha = 0.6f
        
        // Cambiar el texto del botón a "Seleccionado"
        binding.selectBasicButton.text = "Seleccionado"
        binding.selectBasicButton.isEnabled = false
        binding.selectBasicButton.isClickable = false
        
        // Ocultar el card del plan premium
        binding.premiumPlanCard.visibility = View.GONE
        
        // Cancelar cualquier animación pendiente
        binding.selectBasicButton.clearAnimation()
        binding.selectBasicButton.alpha = 1f
        binding.selectBasicButton.translationY = 0f
        
        // Asegurar que los cambios se apliquen inmediatamente
        binding.basicPlanCard.invalidate()
        binding.selectBasicButton.invalidate()
        binding.premiumPlanCard.invalidate()
    }
    
    private fun animateViews() {
        binding.planPrice.alpha = 0f
        binding.planPrice.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setStartDelay(200)
            .start()
        
        binding.selectBasicButton.alpha = 0f
        binding.selectBasicButton.translationY = 30f
        binding.selectBasicButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(300)
            .start()
        
        binding.purchaseButton.alpha = 0f
        binding.purchaseButton.translationY = 30f
        binding.purchaseButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(400)
            .start()
    }
}

