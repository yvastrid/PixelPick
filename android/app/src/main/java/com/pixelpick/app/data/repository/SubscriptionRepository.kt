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
                android.util.Log.d("SubscriptionRepository", "=== SOLICITANDO ESTADO DE SUSCRIPCIÓN ===")
                val response = apiService.getSubscriptionStatus()
                android.util.Log.d("SubscriptionRepository", "Response code: ${response.code()}")
                android.util.Log.d("SubscriptionRepository", "Response isSuccessful: ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    android.util.Log.d("SubscriptionRepository", "API Response success: ${apiResponse.success}")
                    android.util.Log.d("SubscriptionRepository", "API Response user type: ${apiResponse.user?.javaClass?.simpleName}")
                    android.util.Log.d("SubscriptionRepository", "API Response user: ${apiResponse.user}")
                    
                    if (apiResponse.success) {
                        // Intentar parsear como SubscriptionStatusResponse directamente
                        val statusResponse = apiResponse.user as? SubscriptionStatusResponse
                        
                        if (statusResponse != null) {
                            android.util.Log.d("SubscriptionRepository", "✅ Parseado como SubscriptionStatusResponse directamente")
                            android.util.Log.d("SubscriptionRepository", "hasSubscription: ${statusResponse.hasSubscription}")
                            android.util.Log.d("SubscriptionRepository", "subscription planType: '${statusResponse.subscription?.planType}'")
                            Result.success(statusResponse)
                        } else {
                            // Si no funciona, intentar parsear como Map (método anterior)
                            android.util.Log.d("SubscriptionRepository", "Intentando parsear como Map...")
                            @Suppress("UNCHECKED_CAST")
                            val userData = apiResponse.user as? Map<*, *>
                            
                            if (userData != null) {
                                val hasSubscription = userData["has_subscription"] as? Boolean ?: false
                                val subscriptionMap = userData["subscription"] as? Map<*, *>
                                
                                val planType = subscriptionMap?.get("plan_type") as? String
                                android.util.Log.d("SubscriptionRepository", "🔍 plan_type RAW: '$planType'")
                                
                                val subscription = if (subscriptionMap != null) {
                                    Subscription(
                                        id = (subscriptionMap["id"] as? Number)?.toInt(),
                                        userId = (subscriptionMap["user_id"] as? Number)?.toInt(),
                                        planType = planType,
                                        amount = (subscriptionMap["amount"] as? Number)?.toDouble(),
                                        currency = subscriptionMap["currency"] as? String,
                                        status = subscriptionMap["status"] as? String,
                                        currentPeriodStart = subscriptionMap["current_period_start"] as? String,
                                        currentPeriodEnd = subscriptionMap["current_period_end"] as? String
                                    )
                                } else null
                                
                                android.util.Log.d("SubscriptionRepository", "✅ Subscription creada desde Map: planType='${subscription?.planType}'")
                                
                                Result.success(SubscriptionStatusResponse(
                                    hasSubscription = hasSubscription,
                                    subscription = subscription
                                ))
                            } else {
                                android.util.Log.e("SubscriptionRepository", "❌ userData es null y no es SubscriptionStatusResponse")
                                Result.failure(Exception("Formato de respuesta inválido: userData es null"))
                            }
                        }
                    } else {
                        android.util.Log.e("SubscriptionRepository", "❌ API Response success es false")
                        Result.failure(Exception(apiResponse.message ?: apiResponse.error ?: "Error desconocido"))
                    }
                } else {
                    android.util.Log.e("SubscriptionRepository", "❌ Response no exitoso o body es null")
                    android.util.Log.e("SubscriptionRepository", "Response error body: ${response.errorBody()?.string()}")
                    Result.failure(Exception("Error al obtener estado de suscripción"))
                }
            } catch (e: Exception) {
                android.util.Log.e("SubscriptionRepository", "❌ Excepción: ${e.message}")
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
    
    suspend fun activateBasicPlan(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.activateBasicPlan()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        // Devolver el mensaje del backend
                        val message = apiResponse.message ?: "Plan básico activado exitosamente"
                        Result.success(message)
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

