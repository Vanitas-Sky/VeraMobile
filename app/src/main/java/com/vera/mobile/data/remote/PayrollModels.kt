package com.vera.mobile.data.remote

data class PayrollPeriod(
    val id: Int,
    val period_name: String,
    val start_date: String,
    val end_date: String,
    val total_gross: Double,
    val total_isr_retention: Double,
    val total_imss_employee: Double,
    val total_net: Double,
    val details_count: Int
)

data class CustomDeductionBreakdown(
    val sat_key: String?,
    val description: String,
    val amount: Double
)

data class PayrollDetailItem(
    val id: Int,
    val gross_salary: Double,
    val isr_retention: Double,
    val imss_employee: Double,
    val total_custom_deductions: Double,
    val custom_deductions_breakdown: List<CustomDeductionBreakdown>?, // Mapea la columna JSON
    val net_salary: Double,
    val employee: EmployeeShortInfo?
)

data class EmployeeShortInfo(
    val id: Int,
    val full_name: String,
    val rfc: String,
    val position: String?,
    val periodicity: String?, // "semana", "quincena", "mensua"
    val nss: String?,
    val work_regime: String?
)

data class PayrollDetailResponse(
    val period: PayrollPeriod,
    val details: List<PayrollDetailItem>
)
