package com.pixelpick.app.data.models

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("error")
    val error: String?,
    
    @SerializedName("user")
    val user: T?,
    
    @SerializedName("redirect_to_checkout")
    val redirectToCheckout: Boolean? = false
)

data class GamesResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("games")
    val games: List<Game>?,
    
    @SerializedName("error")
    val error: String?
)

data class RecommendationsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("recommendations")
    val recommendations: List<Game>?,
    
    @SerializedName("error")
    val error: String?
)

data class UserGamesResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("games")
    val games: List<UserGame>?,
    
    @SerializedName("error")
    val error: String?
)

data class ProfileResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("user")
    val user: User?,
    
    @SerializedName("games")
    val games: List<UserGame>?,
    
    @SerializedName("stats")
    val stats: Stats?,
    
    @SerializedName("error")
    val error: String?
)

data class Stats(
    @SerializedName("completed")
    val completed: Int,
    
    @SerializedName("playing")
    val playing: Int
)

data class UpdateProfileResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("user")
    val user: User?,
    
    @SerializedName("changes_remaining")
    val changesRemaining: Int?,
    
    @SerializedName("error")
    val error: String?
)

