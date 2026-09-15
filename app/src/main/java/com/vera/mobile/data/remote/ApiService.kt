package com.vera.mobile.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(
        @Header("Authorization") token: String
    ): Response<DashboardSummaryResponse>

    @GET("payrolls")
    suspend fun getPayrolls(
        @Header("Authorization") token: String
    ): Response<List<PayrollPeriod>>

    @GET("employees")
    suspend fun getEmployees(
        @Header("Authorization") token: String
    ): Response<List<Employee>>

    companion object {
        const val BASE_URL = "http://192.168.1.7:8000/api/v1/"
    }
}
