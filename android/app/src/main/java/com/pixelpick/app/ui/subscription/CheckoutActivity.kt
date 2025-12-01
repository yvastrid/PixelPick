package com.pixelpick.app.ui.subscription

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pixelpick.app.R
import com.pixelpick.app.databinding.ActivityCheckoutBinding
import com.pixelpick.app.data.api.RetrofitClient
import com.pixelpick.app.data.repository.SubscriptionRepository
import com.pixelpick.app.ui.main.MainActivity
import com.pixelpick.app.util.SessionManager
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var subscriptionRepository: SubscriptionRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        subscriptionRepository = SubscriptionRepository(RetrofitClient.apiService)
        
        setupWebView()
        loadCheckoutPage()
    }
    
    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Si la URL contiene "welcome" o "success", significa que el pago fue exitoso
                if (url?.contains("welcome") == true || url?.contains("success") == true) {
                    // Pago exitoso, volver a MainActivity
                    Toast.makeText(this@CheckoutActivity, "¡Pago exitoso! Tu suscripción ha sido activada.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@CheckoutActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
    
    private fun loadCheckoutPage() {
        // Obtener la URL base del API desde BuildConfig
        val baseUrl = com.pixelpick.app.BuildConfig.API_BASE_URL
        val checkoutUrl = "$baseUrl/checkout"
        
        // Cargar la página de checkout en el WebView
        binding.webView.loadUrl(checkoutUrl)
    }
}

