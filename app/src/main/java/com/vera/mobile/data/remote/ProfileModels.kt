package com.vera.mobile.data.remote

data class ProfileResponse(
    val user: UserInfo,
    val company: CompanyInfo,
    val server_status: String,
    val api_version: String
)

data class UserInfo(
    val name: String,
    val email: String
)

data class CompanyInfo(
    val legal_name: String,
    val trade_name: String,
    val rfc: String,
    val tax_regime: String,
    val postal_code: String
)
