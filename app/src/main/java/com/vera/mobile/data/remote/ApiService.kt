package com.vera.mobile.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(
        @Header("Authorization") token: String,
        @Query("period") period: String // formato "2026-09"
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

    @Streaming
    @GET("payrolls/{periodId}/employees/{employeeId}/pdf")
    suspend fun downloadPayrollPdf(
        @Header("Authorization") token: String,
        @Path("periodId") periodId: Int,
        @Path("employeeId") employeeId: Int
    ): Response<ResponseBody>

    @GET("employees")
    suspend fun getEmployees(
        @Header("Authorization") token: String
    ): Response<List<Employee>>

    @GET("fixed-expenses")
    suspend fun getFixedExpenses(
        @Header("Authorization") token: String
    ): Response<FixedExpensesResponse>

    @GET("invoices")
    suspend fun getInvoices(
        @Header("Authorization") token: String,
        @Query("period") period: String?,
        @Query("type") type: String?,
        @Query("status") status: String?,
        @Query("search") search: String?
    ): Response<InvoicesResponse>

    @GET("invoices/{id}")
    suspend fun getInvoiceDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<InvoiceDetailResponse>

    @POST("ai/ask")
    suspend fun askAi(
        @Header("Authorization") token: String,
        @Body request: AiAskRequest
    ): Response<AiAskResponse>

    @GET("ai/summary")
    suspend fun getAiSummary(
        @Header("Authorization") token: String
    ): Response<AiSummaryResponse>

    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Map<String, String>>

    companion object {
        const val BASE_URL = "http://192.168.1.7:8000/api/v1/"
    }
}
