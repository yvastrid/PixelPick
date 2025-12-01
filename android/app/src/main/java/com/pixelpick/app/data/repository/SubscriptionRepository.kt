package com.pixelpick.app.data.repository

import com.pixelpick.app.data.api.ApiService
import com.pixelpick.app.data.models.Subscription
import com.pixelpick.app.data.models.SubscriptionStatusResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionRepository(
    private val apiService: ApiService
) {
    
    suspend fun getSubscriptionStatus(): Result<SubscriptionStatusResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSubscriptionStatus()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        // El API devuelve un Map con has_subscription y subscription
                        @Suppress("UNCHECKED_CAST")
                        val userData = apiResponse.user as? Map<*, *>
                        if (userData != null) {
                            val hasSubscription = userData["has_subscription"] as? Boolean ?: false
                            val subscriptionMap = userData["subscription"] as? Map<*, *>
                            val subscription = if (subscriptionMap != null) {
                                Subscription(
                                    id = (subscriptionMap["id"] as? Number)?.toInt(),
                                    userId = (subscriptionMap["user_id"] as? Number)?.toInt(),
                                    planType = subscriptionMap["plan_type"] as? String,
                                    amount = (subscriptionMap["amount"] as? Number)?.toDouble(),
                                    currency = subscriptionMap["currency"] as? String,
                                    status = subscriptionMap["status"] as? String,
                                    currentPeriodStart = subscriptionMap["current_period_start"] as? String,
                                    currentPeriodEnd = subscriptionMap["current_period_end"] as? String
                                )
                            } else null
                            
                            Result.success(SubscriptionStatusResponse(
                                hasSubscription = hasSubscription,
                                subscription = subscription
                            ))
                        } else {
                            Result.failure(Exception("Formato de respuesta inválido"))
                        }
                    } else {
                        Result.failure(Exception(apiResponse.message ?: apiResponse.error ?: "Error desconocido"))
                    }
                } else {
                    Result.failure(Exception("Error al obtener estado de suscripción"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun activateBasicPlan(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.activateBasicPlan()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(apiResponse.message ?: apiResponse.error ?: "Error desconocido"))
                    }
                } else {
                    Result.failure(Exception("Error al activar plan básico"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun activatePremiumPlan(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.activatePremiumPlan()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(apiResponse.message ?: apiResponse.error ?: "Error desconocido"))
                    }
                } else {
                    Result.failure(Exception("Error al activar plan premium"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun createPaymentIntent(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createPaymentIntent()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        // El API devuelve un Map con client_secret y payment_intent_id
                        @Suppress("UNCHECKED_CAST")
                        val userData = apiResponse.user as? Map<*, *>
                        if (userData != null) {
                            val clientSecret = userData["client_secret"] as? String
                            if (clientSecret != null) {
                                Result.success(clientSecret)
                            } else {
                                Result.failure(Exception("client_secret no encontrado en la respuesta"))
                            }
                        } else {
                            Result.failure(Exception("Formato de respuesta inválido"))
                        }
                    } else {
                        Result.failure(Exception(apiResponse.message ?: apiResponse.error ?: "Error desconocido"))
                    }
                } else {
                    Result.failure(Exception("Error al crear payment intent"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

