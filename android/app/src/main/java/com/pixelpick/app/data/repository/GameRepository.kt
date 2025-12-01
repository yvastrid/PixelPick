package com.pixelpick.app.data.repository

import com.pixelpick.app.data.api.RetrofitClient
import com.pixelpick.app.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepository {
    private val apiService = RetrofitClient.apiService
    
    suspend fun getGames(): Result<List<Game>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getGames()
            android.util.Log.d("GameRepository", "Response code: ${response.code()}")
            android.util.Log.d("GameRepository", "Response isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                android.util.Log.d("GameRepository", "Response success: ${body.success}")
                android.util.Log.d("GameRepository", "Games count: ${body.games?.size ?: 0}")
                
                if (body.success && body.games != null) {
                    android.util.Log.d("GameRepository", "Returning ${body.games!!.size} games")
                    Result.success(body.games!!)
                } else {
                    val errorMsg = body.error ?: "Error desconocido"
                    android.util.Log.e("GameRepository", "Error en respuesta: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("GameRepository", "Error response: $errorBody")
                Result.failure(Exception("Error en la respuesta del servidor: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("GameRepository", "Exception al obtener juegos: ${e.message}", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun getRecommendations(): Result<List<Game>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRecommendations()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.recommendations != null) {
                    Result.success(body.recommendations!!)
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
    
    suspend fun getUserGames(): Result<List<UserGame>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserGames()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.games != null) {
                    Result.success(body.games!!)
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
    
    suspend fun addUserGame(request: AddGameRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.addUserGame(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(Unit)
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

