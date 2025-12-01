package com.pixelpick.app.data.repository

import com.pixelpick.app.data.api.RetrofitClient
import com.pixelpick.app.data.models.*
import com.pixelpick.app.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(
    private val sessionManager: SessionManager
) {
    private val apiService = RetrofitClient.apiService
    
    suspend fun getProfile(): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile()
            
            // Verificar Content-Type antes de parsear
            val contentType = response.headers()["Content-Type"] ?: ""
            if (!contentType.contains("application/json") && !response.isSuccessful) {
                return@withContext Result.failure(Exception("Sesión expirada. Por favor, inicia sesión nuevamente."))
            }
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.error ?: "Error desconocido"))
                }
            } else {
                // Intentar leer el cuerpo del error
                val errorBody = response.errorBody()?.string()
                val errorMessage = when {
                    errorBody != null && errorBody.isNotEmpty() -> {
                        // Verificar si es HTML (redirección de login)
                        if (errorBody.contains("<!DOCTYPE") || errorBody.contains("<html") || errorBody.contains("login")) {
                            "Sesión expirada. Por favor, inicia sesión nuevamente."
                        } else {
                            try {
                                // Intentar parsear como JSON de error
                                val errorJson = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                                errorJson["error"]?.toString() ?: "Error en la respuesta del servidor"
                            } catch (e: Exception) {
                                "Error ${response.code()}: ${if (errorBody.length > 100) errorBody.take(100) + "..." else errorBody}"
                            }
                        }
                    }
                    else -> {
                        when (response.code()) {
                            401 -> "Sesión expirada. Por favor, inicia sesión nuevamente."
                            403 -> "No tienes permisos para acceder a este recurso."
                            else -> "Error en la respuesta del servidor (código: ${response.code()})"
                        }
                    }
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            // JSON malformado - probablemente HTML de redirección
            Result.failure(Exception("Sesión expirada. Por favor, inicia sesión nuevamente."))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Tiempo de espera agotado. El servidor no responde."))
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("No se puede conectar al servidor. Verifica tu conexión a internet."))
        } catch (e: javax.net.ssl.SSLException) {
            Result.failure(Exception("Error de seguridad SSL. Verifica la configuración del servidor."))
        } catch (e: java.io.IOException) {
            val errorMsg = e.message ?: "Error desconocido"
            when {
                errorMsg.contains("Unable to resolve host") -> {
                    Result.failure(Exception("No se puede conectar al servidor. Verifica tu conexión."))
                }
                errorMsg.contains("timeout") -> {
                    Result.failure(Exception("Tiempo de espera agotado. Intenta nuevamente."))
                }
                else -> {
                    Result.failure(Exception("Error de conexión: $errorMsg"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al cargar perfil: ${e.message ?: "Error desconocido"}"))
        }
    }
    
    suspend fun updateProfile(request: UpdateProfileRequest): Result<UpdateProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.user != null) {
                    // Actualizar usuario en sesión
                    sessionManager.saveUser(body.user!!)
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.error ?: "Error desconocido"))
                }
            } else {
                Result.failure(Exception("Error en la respuesta del servidor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteAccount()
            sessionManager.clearSession()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar cuenta"))
            }
        } catch (e: Exception) {
            sessionManager.clearSession()
            Result.failure(e)
        }
    }
}

