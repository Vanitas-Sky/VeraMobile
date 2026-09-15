package com.vera.mobile.data.remote

data class DashboardSummaryResponse(
    val companyName: String,
    val totalBankWithdrawals: Double,
    val totalInvoicedExpenses: Double,
    val fiscalDiscrepancy: Double,
    val hasRisk: Boolean
)
