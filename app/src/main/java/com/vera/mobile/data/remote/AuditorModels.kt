package com.vera.mobile.data.remote

data class AuditorDto(
    val id: Long,
    val name: String,
    val email: String,
    val created_at: String?
)

data class AuditorsResponse(
    val success: Boolean,
    val auditors: List<AuditorDto>
)

data class CreateAuditorRequest(
    val name: String,
    val email: String,
    val password: String
)
