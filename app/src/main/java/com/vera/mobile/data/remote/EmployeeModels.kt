package com.vera.mobile.data.remote

data class Employee(
    val id: Int,
    val full_name: String,
    val position: String?,
    val email: String?,
    val phone: String?,
    val rfc: String?,
    val curp: String?,
    val nss: String?,
    val clabe: String?,
    val base_salary: Double,
    val periodicity: String,
    val work_regime: String?,
    val is_active: Boolean
)
