package com.vera.mobile.data.remote

import com.google.gson.annotations.SerializedName

data class AuthActionResponse(
    val success: Boolean,
    val message: String
)

typealias GenericApiResponse = AuthActionResponse

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    val terms: Boolean = true,
    @SerializedName("device_name") val deviceName: String = "android_device"
)
