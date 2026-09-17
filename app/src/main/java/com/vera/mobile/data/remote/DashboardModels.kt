package com.vera.mobile.data.remote

data class DashboardSummaryResponse(
    val company_name: String,
    val selected_period: String,
    val semaforo: String, // "verde", "amarillo", "rojo"
    val mensaje_semaforo: String,
    val total_income: Double,
    val total_expense: Double,
    val bank_withdrawals: Double,
    val total_payroll_gross: Double = 0.0,
    val discrepancy: Double,
    val missing_invoices_amount: Double,
    val alerts: List<DashboardAlert>,
    val recent_invoices: List<RecentInvoiceItem>
)

data class DashboardAlert(
    val title: String,
    val description: String
)

data class RecentInvoiceItem(
    val id: Int,
    val uuid: String?,
    val partner_name: String,
    val type: String, // "I" o "E"
    val total: Double,
    val issue_date: String
)
