package com.vera.mobile.data.repository

import com.vera.mobile.data.local.SessionManager
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.LoginRequest
import com.vera.mobile.data.remote.LoginResponse
import kotlinx.coroutines.flow.firstOrNull

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                sessionManager.saveAuthToken(loginResponse.token)
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
            val token = sessionManager.authToken.firstOrNull()
            if (token != null) {
                apiService.logout("Bearer $token")
            }
            sessionManager.clearAuthToken()
            Result.success(Unit)
        } catch (e: Exception) {
            sessionManager.clearAuthToken()
            Result.failure(e)
        }
    }
}
