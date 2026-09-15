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
    val email: String
)
