package com.pixelpick.app.ui.game

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.pixelpick.app.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val gameFileName = intent.getStringExtra("game_file") ?: return
        
        // Configurar WebView
        binding.gameWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        
        binding.gameWebView.webViewClient = WebViewClient()
        
        // Cargar el juego desde assets
        val gameUrl = "file:///android_asset/games/$gameFileName"
        binding.gameWebView.loadUrl(gameUrl)
    }
    
    override fun onBackPressed() {
        if (binding.gameWebView.canGoBack()) {
            binding.gameWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

