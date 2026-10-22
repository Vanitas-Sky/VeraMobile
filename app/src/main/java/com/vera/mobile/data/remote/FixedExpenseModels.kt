package com.vera.mobile.data.remote

import com.google.gson.annotations.SerializedName

data class FixedExpensesResponse(
    @SerializedName("total_monthly_opex") val totalMonthlyOpex: Double,
    @SerializedName("annual_projection") val annualProjection: Double,
    @SerializedName("active_contracts_count") val activeContractsCount: Int,
    val alerts: List<FixedExpenseAlert> = emptyList(),
    val expenses: List<FixedExpenseItem> = emptyList()
) {
    val total_monthly_opex: Double get() = totalMonthlyOpex
    val annual_projection: Double get() = annualProjection
    val active_contracts_count: Int get() = activeContractsCount
}

data class FixedExpenseAlert(
    val type: String, // "danger", "warning", "info"
    val message: String,
    val action: String? = null
)

data class FixedExpenseItem(
    val id: Int,
    @SerializedName("provider_name") val providerName: String,
    val description: String?,
    val category: String,
    @SerializedName("monthly_amount") val monthlyAmount: Double,
    @SerializedName("due_day") val dueDay: Int,
    @SerializedName("contract_start_date") val contractStartDate: String?,
    @SerializedName("contract_end_date") val contractEndDate: String?,
    @SerializedName("is_active") val isActive: Boolean
) {
    val provider_name: String get() = providerName
    val monthly_amount: Double get() = monthlyAmount
    val due_day: Int get() = dueDay
    val contract_start_date: String? get() = contractStartDate
    val contract_end_date: String? get() = contractEndDate
    val is_active: Boolean get() = isActive
}

data class FixedExpenseCreateRequest(
    @SerializedName("provider_name") val providerName: String,
    val description: String?,
    val category: String,
    @SerializedName("monthly_amount") val monthlyAmount: Double,
    @SerializedName("due_day") val dueDay: Int,
    @SerializedName("contract_start_date") val contractStartDate: String? = null,
    @SerializedName("contract_end_date") val contractEndDate: String? = null
) {
    val provider_name: String get() = providerName
    val monthly_amount: Double get() = monthlyAmount
    val due_day: Int get() = dueDay
    val contract_start_date: String? get() = contractStartDate
    val contract_end_date: String? get() = contractEndDate
}
