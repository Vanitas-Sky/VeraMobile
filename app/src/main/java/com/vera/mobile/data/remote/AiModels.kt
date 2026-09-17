package com.vera.mobile.data.remote

data class AiAskRequest(
    val question: String
)

data class AiAskResponse(
    val success: Boolean,
    val answer: String?,
    val timestamp: String?,
    val error: String?
)

data class AiSummaryResponse(
    val success: Boolean,
    val summary: String?,
    val error: String?
)
