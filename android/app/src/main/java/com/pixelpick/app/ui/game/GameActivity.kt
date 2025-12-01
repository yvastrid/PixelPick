package com.pixelpick.app.ui.game

import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pixelpick.app.databinding.ActivityGameBinding
import com.pixelpick.app.data.models.AddGameRequest
import com.pixelpick.app.data.repository.GameRepository
import com.pixelpick.app.util.SessionManager
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var gameRepository: GameRepository
    private lateinit var sessionManager: SessionManager
    private var gameId: Int = 0
    private var gameName: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        gameRepository = GameRepository()
        sessionManager = SessionManager(this)
        
        val gameFileName = intent.getStringExtra("game_file") ?: return
        gameId = intent.getIntExtra("game_id", 0)
        gameName = intent.getStringExtra("game_name") ?: ""
        
        // Registrar que el usuario está jugando este juego
        if (gameId > 0) {
            registerGamePlaying()
        }
        
        // Configurar WebView
        binding.gameWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            allowFileAccess = true
            allowContentAccess = true
        }
        
        // Agregar interfaz JavaScript para comunicación con el juego
        binding.gameWebView.addJavascriptInterface(WebAppInterface(), "Android")
        
        binding.gameWebView.webViewClient = WebViewClient()
        
        // Cargar el juego desde assets
        val gameUrl = "file:///android_asset/games/$gameFileName"
        binding.gameWebView.loadUrl(gameUrl)
    }
    
    private fun registerGamePlaying() {
        lifecycleScope.launch {
            try {
                val request = AddGameRequest(gameId = gameId, status = "playing")
                val result = gameRepository.addUserGame(request)
                result.onSuccess {
                    android.util.Log.d("GameActivity", "Juego registrado como 'playing': $gameName")
                }.onFailure { error ->
                    android.util.Log.e("GameActivity", "Error al registrar juego: ${error.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("GameActivity", "Exception al registrar juego: ${e.message}")
            }
        }
    }
    
    private fun completeGame() {
        if (gameId <= 0) return
        
        lifecycleScope.launch {
            try {
                val result = gameRepository.completeGame(gameId)
                result.onSuccess {
                    android.util.Log.d("GameActivity", "Juego marcado como completado: $gameName")
                }.onFailure { error ->
                    android.util.Log.e("GameActivity", "Error al completar juego: ${error.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("GameActivity", "Exception al completar juego: ${e.message}")
            }
        }
    }
    
    inner class WebAppInterface {
        @JavascriptInterface
        fun gameCompleted() {
            runOnUiThread {
                completeGame()
            }
        }
    }
    
    override fun onBackPressed() {
        if (binding.gameWebView.canGoBack()) {
            binding.gameWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

