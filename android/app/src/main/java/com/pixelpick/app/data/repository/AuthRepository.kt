package com.pixelpick.app.data.repository

import com.pixelpick.app.data.api.RetrofitClient
import com.pixelpick.app.data.models.*
import com.pixelpick.app.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val sessionManager: SessionManager
) {
    private val apiService = RetrofitClient.apiService
    
    suspend fun register(request: RegisterRequest): Result<ApiResponse<User>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.user != null) {
                    // Guardar sesión
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
    
    suspend fun login(request: LoginRequest): Result<ApiResponse<User>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.user != null) {
                    // Guardar sesión
                    sessionManager.saveUser(body.user!!)
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.error ?: "Email o contraseña incorrectos"))
                }
            } else {
                Result.failure(Exception("Error en la respuesta del servidor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.logout()
            sessionManager.clearSession()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al cerrar sesión"))
            }
        } catch (e: Exception) {
            sessionManager.clearSession()
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.user != null) {
                    Result.success(body.user!!)
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
}

