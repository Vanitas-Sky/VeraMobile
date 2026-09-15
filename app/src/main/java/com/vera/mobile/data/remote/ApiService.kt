package com.vera.mobile.data.remote

import retrofit2.Response
import retrofit2.http.*

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

    @GET("payrolls/{id}/details")
    suspend fun getPayrollDetails(
        @Header("Authorization") token: String,
        @Path("id") periodId: Int
    ): Response<PayrollDetailResponse>

    @GET("employees")
    suspend fun getEmployees(
        @Header("Authorization") token: String
    ): Response<List<Employee>>

    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Map<String, String>>

    companion object {
        const val BASE_URL = "http://192.168.1.7:8000/api/v1/"
    }
}
