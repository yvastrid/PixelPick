package com.pixelpick.app.data.api

import com.pixelpick.app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Autenticación
    @POST("/api/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<User>>
    
    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<User>>
    
    @POST("/api/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
    
    @GET("/api/user")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>
    
    // Perfil
    @GET("/api/profile")
    suspend fun getProfile(): Response<ProfileResponse>
    
    @PUT("/api/profile/update")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UpdateProfileResponse>
    
    @DELETE("/api/profile/delete")
    suspend fun deleteAccount(): Response<ApiResponse<Unit>>
    
    // Juegos
    @GET("/api/games")
    suspend fun getGames(): Response<GamesResponse>
    
    @GET("/api/games/recommendations")
    suspend fun getRecommendations(): Response<RecommendationsResponse>
    
    @GET("/api/user/games")
    suspend fun getUserGames(): Response<UserGamesResponse>
    
    @POST("/api/user/games")
    suspend fun addUserGame(@Body request: AddGameRequest): Response<ApiResponse<Unit>>
    
    // Verificación de email
    @POST("/api/verify-email")
    suspend fun verifyEmail(@Body request: Map<String, String>): Response<ApiResponse<Unit>>
    
    @POST("/api/resend-verification")
    suspend fun resendVerification(@Body request: Map<String, String>): Response<ApiResponse<Unit>>
    
    // Suscripciones
    @GET("/api/subscription/status")
    suspend fun getSubscriptionStatus(): Response<ApiResponse<SubscriptionStatusResponse>>
    
    @POST("/api/subscription/activate-basic")
    suspend fun activateBasicPlan(): Response<ApiResponse<Map<String, Any>>>
    
    @POST("/api/create-payment-intent")
    suspend fun createPaymentIntent(): Response<ApiResponse<PaymentIntentResponse>>
}

