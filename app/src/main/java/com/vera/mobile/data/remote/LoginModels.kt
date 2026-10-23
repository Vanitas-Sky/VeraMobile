package com.vera.mobile.data.remote

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("device_name") val deviceName: String = "android_device"
)

data class LoginResponse(
    val token: String,
    val user: User? = null,
    val message: String? = null
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String? = null,
    @SerializedName("is_auditor") val isAuditor: Boolean = false,
    @SerializedName("terms_accepted") val termsAccepted: Boolean = false
)

// Modelos para recuperación de contraseña móvil
data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val token: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String
)
