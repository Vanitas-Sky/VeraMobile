package com.vera.mobile.data.remote

data class InvoicesResponse(
    val total_amount: Double,
    val total_iva: Double,
    val count: Int,
    val invoices: List<InvoiceItem>
)

data class InvoiceItem(
    val id: Int,
    val uuid: String?,
    val partner_name: String,
    val partner_rfc: String?,
    val type: String, // "I" o "E"
    val total: Double,
    val iva: Double,
    val subtotal: Double,
    val issue_date: String,
    val is_canceled: Boolean
)
