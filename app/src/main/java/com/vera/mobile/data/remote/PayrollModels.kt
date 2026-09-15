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

data class PayrollDetailItem(
    val id: Int,
    val gross_salary: Double,
    val isr_retention: Double,
    val imss_employee: Double,
    val total_custom_deductions: Double,
    val net_salary: Double,
    val employee: EmployeeShortInfo?
)

data class EmployeeShortInfo(
    val id: Int,
    val full_name: String,
    val rfc: String,
    val position: String?
)

data class PayrollDetailResponse(
    val period: PayrollPeriod,
    val details: List<PayrollDetailItem>
)
