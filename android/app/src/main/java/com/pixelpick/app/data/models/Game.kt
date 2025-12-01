package com.pixelpick.app.data.models

import com.google.gson.annotations.SerializedName

data class Game(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("platforms")
    val platforms: List<String>?,
    
    @SerializedName("image_url")
    val imageUrl: String?,
    
    @SerializedName("game_url")
    val gameUrl: String?,
    
    @SerializedName("category")
    val category: String?,
    
    @SerializedName("recommendation_reason")
    val recommendationReason: String? = null
)

