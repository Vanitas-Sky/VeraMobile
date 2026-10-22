package com.vera.mobile.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- AUTENTICACIÓN Y RECUPERACIÓN ---
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<AuthActionResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<AuthActionResponse>

    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Map<String, String>>

    // --- DASHBOARD ---
    @GET("dashboard/summary")
    suspend fun getDashboardSummary(
        @Header("Authorization") token: String,
        @Query("period") period: String
    ): Response<DashboardSummaryResponse>

    // --- NÓMINA ---
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

    // --- EMPLEADOS ---
    @GET("employees")
    suspend fun getEmployees(
        @Header("Authorization") token: String
    ): Response<List<Employee>>

    // --- OpEx (GASTOS FIJOS) ---
    @GET("fixed-expenses")
    suspend fun getFixedExpenses(
        @Header("Authorization") token: String
    ): Response<FixedExpensesResponse>

    @POST("fixed-expenses")
    suspend fun createFixedExpense(
        @Header("Authorization") token: String,
        @Body request: FixedExpenseCreateRequest
    ): Response<GenericApiResponse>

    @PUT("fixed-expenses/{id}")
    suspend fun updateFixedExpense(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: FixedExpenseCreateRequest
    ): Response<GenericApiResponse>

    @PATCH("fixed-expenses/{id}/toggle")
    suspend fun toggleFixedExpense(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<GenericApiResponse>

    // --- BÓVEDA DE FACTURAS ---
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

    // --- CONSULTOR VERA AI ---
    @POST("ai/ask")
    suspend fun askAi(
        @Header("Authorization") token: String,
        @Body request: AiAskRequest
    ): Response<AiAskResponse>

    @GET("ai/summary")
    suspend fun getAiSummary(
        @Header("Authorization") token: String
    ): Response<AiSummaryResponse>

    // --- PERFIL DE EMPRESA ---
    @GET("company/profile")
    suspend fun getCompanyProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>
}
