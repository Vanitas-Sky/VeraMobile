package com.vera.mobile.data.repository

import com.vera.mobile.data.local.TokenManager
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.LoginRequest
import com.vera.mobile.data.remote.LoginResponse
import retrofit2.Response

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                tokenManager.saveToken(loginResponse.token)
                Result.success(loginResponse)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val token = tokenManager.getToken()
            if (token != null) {
                apiService.logout("Bearer $token")
            }
            tokenManager.deleteToken()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.deleteToken() // Igual borramos localmente
            Result.failure(e)
        }
    }
}
