package com.vera.mobile.data.remote

data class AuthActionResponse(
    val success: Boolean,
    val message: String
)

typealias GenericApiResponse = AuthActionResponse
