package com.pixelpick.app.data.api

import com.pixelpick.app.BuildConfig
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // CookieManager para manejar cookies de sesión de Flask
    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.BASIC // Cambiar a BASIC para ver errores incluso en release
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(cookieManager))
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            
            // Agregar headers comunes
            requestBuilder.header("Content-Type", "application/json")
            requestBuilder.header("Accept", "application/json")
            
            val request = requestBuilder.build()
            
            // Logging de cookies para debug
            val cookieHeader = request.header("Cookie")
            android.util.Log.d("RetrofitClient", "🔍 Request URL: ${request.url}")
            android.util.Log.d("RetrofitClient", "🔍 Cookie header: $cookieHeader")
            
            val response = chain.proceed(request)
            
            // Logging de cookies recibidas
            val setCookieHeaders = response.headers("Set-Cookie")
            if (setCookieHeaders.isNotEmpty()) {
                android.util.Log.d("RetrofitClient", "🔍 Set-Cookie headers recibidos: $setCookieHeaders")
            }
            
            response
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(if (BuildConfig.API_BASE_URL.endsWith("/")) BuildConfig.API_BASE_URL else "${BuildConfig.API_BASE_URL}/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

