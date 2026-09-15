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
