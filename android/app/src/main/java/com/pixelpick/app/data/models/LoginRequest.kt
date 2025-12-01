package com.pixelpick.app.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

data class RegisterRequest(
    @SerializedName("firstName")
    val firstName: String,
    
    @SerializedName("lastName")
    val lastName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("terms")
    val terms: Boolean
)

data class UpdateProfileRequest(
    @SerializedName("firstName")
    val firstName: String?,
    
    @SerializedName("lastName")
    val lastName: String?
)

data class AddGameRequest(
    @SerializedName("game_id")
    val gameId: Int,
    
    @SerializedName("status")
    val status: String = "playing"
)

