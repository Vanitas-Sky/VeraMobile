package com.vera.mobile.data.remote

import com.vera.mobile.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Reemplaza con tu dominio exacto de Ngrok
    private const val BASE_URL = "https://rosy-refold-denture.ngrok-free.dev/api/v1/"

    private val authErrorInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        // Si el backend devuelve 401, la sesión ya no es válida
        if (response.code == 401) {
            SessionManager.notifySessionExpired()
        }

        response
    }

    private val headersInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            // Evita la pantalla HTML de advertencia de la versión gratuita de Ngrok
            .addHeader("ngrok-skip-browser-warning", "true")
            .addHeader("Accept", "application/json")
            .build()
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(headersInterceptor)
        .addInterceptor(authErrorInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
