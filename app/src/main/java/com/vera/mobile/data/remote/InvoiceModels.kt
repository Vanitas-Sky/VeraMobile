package com.vera.mobile.data.remote

data class InvoicesResponse(
    val total_amount: Double,
    val total_iva: Double,
    val count: Int,
    val invoices: List<InvoiceItem>
) {
    val totalAmount: Double get() = total_amount
    val totalIva: Double get() = total_iva
}

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
    val is_canceled: Boolean,
    val deducibility_status: String? = null
) {
    val partnerName: String get() = partner_name
    val partnerRfc: String? get() = partner_rfc
    val issueDate: String get() = issue_date
    val isCanceled: Boolean get() = is_canceled
    val deducibilityStatus: String? get() = deducibility_status
}

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
    val deducibility_status: String? = null,
    val deducibility_reason: String? = null,
    val items: List<InvoiceItemConcept> = emptyList()
) {
    val partnerName: String get() = partner_name
    val partnerRfc: String? get() = partner_rfc
    val issueDate: String get() = issue_date
    val paymentMethod: String? get() = payment_method
    val paymentForm: String? get() = payment_form
    val deducibilityStatus: String? get() = deducibility_status
    val deducibilityReason: String? get() = deducibility_reason
}

data class InvoiceItemConcept(
    val description: String?,
    val cantidad: Double?,
    val valor_unitario: Double?,
    val importe: Double?,
    val clave_prod_serv: String?,
    val deducibility_status: String? = null,
    val deducibility_reason: String? = null
) {
    val valorUnitario: Double? get() = valor_unitario
    val claveProdServ: String? get() = clave_prod_serv
    val deducibilityStatus: String? get() = deducibility_status
    val deducibilityReason: String? get() = deducibility_reason
}
