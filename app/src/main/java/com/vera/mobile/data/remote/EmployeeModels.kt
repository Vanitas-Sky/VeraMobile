package com.vera.mobile.data.remote

data class Employee(
    val id: Int,
    val full_name: String,
    val position: String?,
    val email: String?,
    val base_salary: Double,
    val periodicity: String,
    val work_regime: String,
    val is_active: Boolean // Cambiado de Int a Boolean para coincidir con JSON de Laravel
)
