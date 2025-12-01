package com.pixelpick.app.ui.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pixelpick.app.data.models.Game
import com.pixelpick.app.databinding.ItemGameCardBinding
import com.pixelpick.app.ui.game.GameActivity

class RecommendationsAdapter(
    private val games: List<Game>,
    private val onGameClick: (Game) -> Unit
) : RecyclerView.Adapter<RecommendationsAdapter.RecommendationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding = ItemGameCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(games[position])
    }

    override fun getItemCount(): Int = games.size

    inner class RecommendationViewHolder(
        private val binding: ItemGameCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: Game) {
            // Establecer título del juego
            binding.gameTitle.text = game.name
            
            // Establecer precio
            if (game.price == 0.0) {
                binding.gamePrice.text = "GRATIS"
                binding.gamePrice.setTextColor(android.graphics.Color.parseColor("#FF4444"))
            } else {
                binding.gamePrice.text = "$${game.price}"
                binding.gamePrice.setTextColor(android.graphics.Color.parseColor("#00D4FF"))
            }
            
            // Mostrar plataformas
            if (game.platforms != null && game.platforms.isNotEmpty()) {
                val platforms = if (game.platforms is List<*>) {
                    game.platforms as List<String>
                } else {
                    (game.platforms as? String)?.split(",")?.map { it.trim() } ?: emptyList()
                }
                
                if (platforms.isNotEmpty()) {
                    binding.platform1.text = platforms[0]
                    binding.platform1.visibility = View.VISIBLE
                    if (platforms.size > 1) {
                        binding.platform2.text = platforms[1]
                        binding.platform2.visibility = View.VISIBLE
                    } else {
                        binding.platform2.visibility = View.GONE
                    }
                } else {
                    binding.platform1.visibility = View.GONE
                    binding.platform2.visibility = View.GONE
                }
            } else {
                binding.platform1.visibility = View.GONE
                binding.platform2.visibility = View.GONE
            }
            
            // Ocultar badge por defecto (se puede mostrar si es nuevo)
            binding.gameBadge.visibility = View.GONE
            
            // Ocultar razón de IA por ahora (se puede implementar después)
            binding.aiReasonLayout.visibility = View.GONE
            
            // Configurar imagen placeholder con primera letra del juego
            val firstLetter = game.name.firstOrNull()?.toString()?.uppercase() ?: "G"
            binding.gamePlaceholder.text = firstLetter
            
            // Mapear nombres de juegos a archivos HTML
            val gameFileMap = mapOf(
                // Nombres nuevos
                "Frootilupis Match" to "flootilupis.html",
                "Chocopops Volador" to "chocopops.html",
                "SnackAttack Laberinto" to "snackattack.html",
                "CerealKiller Connect" to "cerealkiller.html",
                "Munchies Memory" to "munchies.html",
                // Nombres antiguos (para compatibilidad)
                "Flootilupis" to "flootilupis.html",
                "Chocopops" to "chocopops.html",
                "SnackAttack" to "snackattack.html",
                "CerealKiller" to "cerealkiller.html",
                "Munchies" to "munchies.html"
            )
            
            // Hacer el card clickeable para abrir el juego
            binding.root.setOnClickListener {
                val gameFile = gameFileMap[game.name]
                if (gameFile != null) {
                    // Abrir el juego en GameActivity
                    val intent = Intent(binding.root.context, GameActivity::class.java)
                    intent.putExtra("game_file", gameFile)
                    intent.putExtra("game_id", game.id)
                    intent.putExtra("game_name", game.name)
                    binding.root.context.startActivity(intent)
                } else {
                    // Si no hay archivo, llamar al callback
                    onGameClick(game)
                }
            }
        }
    }
}

