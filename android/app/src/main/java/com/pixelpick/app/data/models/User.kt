package com.pixelpick.app.data.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("first_name")
    val firstName: String,
    
    @SerializedName("last_name")
    val lastName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("name_change_count")
    val nameChangeCount: Int = 0,
    
    @SerializedName("last_name_change_date")
    val lastNameChangeDate: String? = null,
    
    @SerializedName("email_verified")
    val emailVerified: Boolean = false
)

