package com.vera.mobile.data.remote

import com.vera.mobile.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val authErrorInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        // Si el backend devuelve 401, la sesión ya no es válida
        if (response.code == 401) {
            SessionManager.notifySessionExpired()
        }

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authErrorInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
