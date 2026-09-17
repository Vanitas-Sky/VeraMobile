package com.vera.mobile.data.remote

import android.accessibilityservice.GestureDescription

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

data class InvoiceDetailResponse(
    val id: Int,
    val uuid: String?,
    val partner_name: String,
    val partner_rfc: String?,
    val type: String,
    val total: Double,
    val subtotal: Double,
    val iva: Double,
    val issue_date: String,
    val payment_method: String?,
    val payment_form: String?,
    val items: List<InvoiceItemConcept> = emptyList()
)

data class InvoiceItemConcept(
    val description: String?,
    val cantidad: Double?,
    val valor_unitario: Double?,
    val importe: Double?,
    val clave_prod_serv: String?
)
