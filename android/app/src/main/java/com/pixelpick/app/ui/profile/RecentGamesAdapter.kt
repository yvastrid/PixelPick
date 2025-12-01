package com.pixelpick.app.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pixelpick.app.data.models.UserGame
import com.pixelpick.app.databinding.ItemRecentGameBinding
import java.text.SimpleDateFormat
import java.util.*

class RecentGamesAdapter(
    private val games: List<UserGame>
) : RecyclerView.Adapter<RecentGamesAdapter.RecentGameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentGameViewHolder {
        val binding = ItemRecentGameBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecentGameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentGameViewHolder, position: Int) {
        holder.bind(games[position])
    }

    override fun getItemCount(): Int = games.size

    inner class RecentGameViewHolder(
        private val binding: ItemRecentGameBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(userGame: UserGame) {
            val game = userGame.game
            if (game != null) {
                // Mostrar primera letra del juego
                val firstLetter = game.name.firstOrNull()?.toString() ?: "G"
                binding.recentGamePlaceholder.text = firstLetter

                // Mostrar nombre del juego en gris oscuro
                binding.recentGameTitle.text = game.name
                binding.recentGameTitle.setTextColor(android.graphics.Color.parseColor("#4A4A4A"))
                binding.recentGameTitle.visibility = View.VISIBLE

                // Formatear tiempo relativo
                userGame.lastPlayed?.let { lastPlayedStr ->
                    binding.recentGameTime.text = formatRelativeTime(lastPlayedStr)
                } ?: run {
                    binding.recentGameTime.text = "Última vez: Recientemente"
                }
            }
        }

        private fun formatRelativeTime(dateStr: String): String {
            try {
                val formats = arrayOf(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd"
                )

                var date: Date? = null
                for (format in formats) {
                    try {
                        val sdf = SimpleDateFormat(format, Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        date = sdf.parse(dateStr)
                        break
                    } catch (e: Exception) {
                        // Intentar siguiente formato
                    }
                }

                if (date == null) {
                    return "Última vez: Recientemente"
                }

                val now = Date()
                val diff = now.time - date.time
                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24

                return when {
                    days > 0 -> "Última vez: Hace ${days} ${if (days == 1L) "día" else "días"}"
                    hours > 0 -> "Última vez: Hace ${hours} ${if (hours == 1L) "hora" else "horas"}"
                    minutes > 0 -> "Última vez: Hace ${minutes} ${if (minutes == 1L) "minuto" else "minutos"}"
                    else -> "Última vez: Hace unos momentos"
                }
            } catch (e: Exception) {
                return "Última vez: Recientemente"
            }
        }
    }
}

