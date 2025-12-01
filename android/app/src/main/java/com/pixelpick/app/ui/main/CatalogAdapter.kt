package com.pixelpick.app.ui.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pixelpick.app.data.models.Game
import com.pixelpick.app.databinding.ItemCatalogCardBinding
import com.pixelpick.app.ui.game.GameActivity

class CatalogAdapter(
    private val games: List<Game>,
    private val isPremiumPlan: Boolean = false,
    private val onGameClick: (Game) -> Unit
) : RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val binding = ItemCatalogCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CatalogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        holder.bind(games[position], position)
    }

    override fun getItemCount(): Int = games.size

    inner class CatalogViewHolder(
        private val binding: ItemCatalogCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: Game, position: Int) {
            // Determinar si el juego está bloqueado (plan básico solo tiene acceso al primer juego)
            val gameIndex = position
            val isLocked = !isPremiumPlan && gameIndex > 0  // Solo el primer juego (índice 0) está desbloqueado en plan básico
            
            // Establecer título del juego
            android.util.Log.d("CatalogAdapter", "Binding game: ${game.name}, isLocked: $isLocked")
            binding.catalogTitle.text = game.name
            binding.catalogTitle.visibility = View.VISIBLE
            
            // Establecer descripción
            binding.catalogDescription.text = game.description ?: "Sin descripción"
            
            // Mostrar primera letra del juego en el placeholder
            val firstLetter = game.name.firstOrNull()?.toString() ?: "G"
            binding.catalogPlaceholder.text = firstLetter
            
            // Aplicar estilo según si está bloqueado
            if (isLocked) {
                // Juego bloqueado: difuminar y deshabilitar
                binding.root.alpha = 0.5f  // Difuminar
                binding.catalogTitle.setTextColor(android.graphics.Color.parseColor("#999999"))  // Gris más claro
                binding.catalogDescription.setTextColor(android.graphics.Color.parseColor("#999999"))
                binding.catalogPlaceholder.setTextColor(android.graphics.Color.parseColor("#0AFFFFFF"))  // Más transparente
            } else {
                // Juego desbloqueado: estilo normal
                binding.root.alpha = 1.0f
                binding.catalogTitle.setTextColor(android.graphics.Color.parseColor("#4A4A4A"))
                binding.catalogDescription.setTextColor(android.graphics.Color.parseColor("#999999"))  // Gris para descripción
                binding.catalogPlaceholder.setTextColor(android.graphics.Color.parseColor("#1AFFFFFF"))
            }
            
            // Mostrar badge FREE si el precio es 0
            if (game.price == 0.0) {
                binding.catalogFreeBadge.visibility = View.VISIBLE
            } else {
                binding.catalogFreeBadge.visibility = View.GONE
            }
            
            // Mapear nombres de juegos a archivos HTML (incluye nombres nuevos y antiguos para compatibilidad)
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
            
            // Hacer el card clickeable para abrir el juego o mostrar mensaje de bloqueo
            binding.root.setOnClickListener {
                if (isLocked) {
                    // Si está bloqueado, llamar al callback (que mostrará mensaje de upgrade)
                    onGameClick(game)
                } else {
                    // Si está desbloqueado, abrir el juego
                    val gameFile = gameFileMap[game.name]
                    if (gameFile != null) {
                        // Abrir el juego en GameActivity
                        val intent = Intent(binding.root.context, GameActivity::class.java)
                        intent.putExtra("game_file", gameFile)
                        intent.putExtra("game_id", game.id)
                        intent.putExtra("game_name", game.name)
                        intent.putExtra("game_index", gameIndex)  // Pasar el índice del juego
                        binding.root.context.startActivity(intent)
                    } else {
                        // Si no hay archivo, llamar al callback
                        onGameClick(game)
                    }
                }
            }
        }
    }
}

