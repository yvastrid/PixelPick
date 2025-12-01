package com.pixelpick.app.data.models

import com.google.gson.annotations.SerializedName

data class UserGame(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("game_id")
    val gameId: Int,
    
    @SerializedName("status")
    val status: String, // "playing", "completed", "wishlist"
    
    @SerializedName("last_played")
    val lastPlayed: String?,
    
    @SerializedName("game")
    val game: Game?
)

