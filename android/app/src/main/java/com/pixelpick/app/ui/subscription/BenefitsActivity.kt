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
            val result = subscriptionRepository.activateBasicPlan()
            result.onSuccess {
                Toast.makeText(
                    this@BenefitsActivity,
                    "¡Plan básico activado exitosamente!",
                    Toast.LENGTH_LONG
                ).show()
                // Volver a MainActivity
                val intent = Intent(this@BenefitsActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure { error ->
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
            val result = subscriptionRepository.activatePremiumPlan()
            result.onSuccess {
                android.util.Log.d("BenefitsActivity", "✅ Plan premium activado exitosamente en backend")
                Toast.makeText(
                    this@BenefitsActivity,
                    "¡Plan premium activado exitosamente!",
                    Toast.LENGTH_LONG
                ).show()
                
                // Esperar un momento para que el mensaje se muestre y el backend procese
                kotlinx.coroutines.delay(1000)
                
                // Verificar que el cambio se haya guardado antes de volver
                android.util.Log.d("BenefitsActivity", "Verificando estado actualizado...")
                val verifyResult = subscriptionRepository.getSubscriptionStatus()
                verifyResult.onSuccess { statusResponse ->
                    android.util.Log.d("BenefitsActivity", "Estado verificado: hasSubscription=${statusResponse.hasSubscription}")
                    if (statusResponse.hasSubscription && statusResponse.subscription != null) {
                        val planType = statusResponse.subscription.planType ?: ""
                        android.util.Log.d("BenefitsActivity", "Plan type verificado: '$planType'")
                    }
                    
                    // Volver a MainActivity para ver los cambios
                    // Usar FLAG_ACTIVITY_CLEAR_TASK para asegurar que MainActivity se recree completamente
                    val intent = Intent(this@BenefitsActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }.onFailure { error ->
                    android.util.Log.e("BenefitsActivity", "Error al verificar estado: ${error.message}")
                    // Aún así, volver a MainActivity
                    val intent = Intent(this@BenefitsActivity, MainActivity::class.java)
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
                    android.util.Log.d("BenefitsActivity", "Plan type: '$planType'")
                    android.util.Log.d("BenefitsActivity", "Status: '$status'")
                    
                    // Verificar tipo de plan
                    val isBasicPlan = planType.contains("basic", ignoreCase = true) || 
                                     planType.contains("pixelie_basic", ignoreCase = true)
                    val isPremiumPlan = planType.equals("pixelie_plan", ignoreCase = true) ||
                                       (planType.contains("pixelie", ignoreCase = true) && 
                                        !planType.contains("basic", ignoreCase = true) &&
                                        planType.contains("plan", ignoreCase = true))
                    
                    android.util.Log.d("BenefitsActivity", "isBasicPlan: $isBasicPlan, isPremiumPlan: $isPremiumPlan")
                    
                    // Determinar el nombre del plan
                    val planName = when {
                        isBasicPlan -> "Pixelie Basic Plan"
                        isPremiumPlan -> "Pixelie Plan"
                        else -> "Pixelie Basic Plan"
                    }
                    
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
                        // Modo "Suscríbete ahora": mostrar el plan opuesto
                        if (isBasicPlan && subscription.status == "active") {
                            // Tiene básico → mostrar premium para upgrade
                            applyUpgradeModeBasic()
                        } else if (isPremiumPlan && subscription.status == "active") {
                            // Tiene premium → mostrar básico difuminado (no puede downgrade)
                            applyUpgradeModePremium()
                        } else {
                            // Por defecto, mostrar premium
                            applyUpgradeModeBasic()
                        }
                    } else {
                        // Modo "view" (desde menú de perfil): mostrar plan activo
                        if (isBasicPlan && subscription.status == "active") {
                            applyBasicPlanSelectedState()
                        } else if (isPremiumPlan && subscription.status == "active") {
                            applyPremiumPlanSelectedState()
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
    
    private fun applyPremiumPlanSelectedState() {
        // Modo view cuando tiene plan premium: mostrar ambos planes, premium marcado
        binding.basicPlanCard.visibility = View.VISIBLE
        binding.basicPlanCard.alpha = 1f
        binding.selectBasicButton.text = "Seleccionar Plan"
        binding.selectBasicButton.isEnabled = true
        binding.selectBasicButton.isClickable = true
        binding.premiumPlanCard.visibility = View.VISIBLE
        binding.premiumPlanCard.alpha = 0.6f
        binding.purchaseButton.text = "Seleccionado"
        binding.purchaseButton.isEnabled = false
        binding.purchaseButton.isClickable = false
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

