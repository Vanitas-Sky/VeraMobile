package com.vera.mobile.data.remote

data class FixedExpensesResponse(
    val total_monthly_opex: Double,
    val annual_projection: Double,
    val active_contracts_count: Int,
    val alerts: List<FixedExpenseAlert>,
    val expenses: List<FixedExpenseModel>
)

data class FixedExpenseAlert(
    val type: String, // "warning", "danger", "info"
    val title: String,
    val message: String,
    val action: String?
)

data class FixedExpenseModel(
    val id: Int,
    val provider_name: String,
    val category: String,
    val description: String?,
    val monthly_amount: Double,
    val due_day: Int,
    val contract_start_date: String?,
    val contract_end_date: String?,
    val is_active: Boolean
)
