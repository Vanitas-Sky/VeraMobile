package com.vera.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.InvoiceDetailResponse
import com.vera.mobile.data.remote.InvoiceItemConcept
import com.vera.mobile.data.remote.RetrofitClient
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailSheet(
    token: String,
    invoiceId: Int,
    onDismiss: () -> Unit
) {
    var invoice by remember { mutableStateOf<InvoiceDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(invoiceId) {
        try {
            val res = RetrofitClient.apiService.getInvoiceDetail("Bearer $token", invoiceId)
            if (res.isSuccessful) invoice = res.body()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0F172A))
            }
        } else if (invoice != null) {
            InvoiceDetailSheetContent(invoice = invoice!!, onDismiss = onDismiss)
        }
    }
}

@Composable
fun InvoiceDetailSheetContent(
    invoice: InvoiceDetailResponse,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")) }
    val isIngreso = invoice.type.equals("I", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        // Cabecera Modal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isIngreso) "Factura de Ingreso" else "Factura de Gasto (Egreso)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = invoice.partnerName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
                Text(
                    text = "RFC: ${invoice.partnerRfc ?: "N/D"}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF64748B)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isIngreso) Color(0xFFECFDF5) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (isIngreso) "Ingreso" else "Egreso",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIngreso) Color(0xFF059669) else Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF64748B))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dictamen Fiscal (Solo en Egresos)
        if (!isIngreso) {
            val (badgeColor, textColor, label) = when (invoice.deducibilityStatus) {
                "ROJO" -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "🔴 No Deducible")
                "AMARILLO" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "🟡 Riesgo Fiscal")
                else -> Triple(Color(0xFFD1FAE5), Color(0xFF059669), "🟢 Deducible SAT")
            }

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textColor
                    )
                    if (!invoice.deducibilityReason.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = invoice.deducibilityReason!!,
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Tarjeta de UUID con copiado rápido
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("FOLIO FISCAL (UUID)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Text(
                        text = invoice.uuid ?: "Sin UUID",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF334155)
                    )
                }
                if (!invoice.uuid.isNullOrBlank()) {
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(invoice.uuid)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Folio", tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Desglose de Importes
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal", fontSize = 12.sp, color = Color(0xFF64748B))
            Text(currencyFormat.format(invoice.subtotal), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("IVA (16%)", fontSize = 12.sp, color = Color(0xFF64748B))
            Text(currencyFormat.format(invoice.iva), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Facturado", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(
                currencyFormat.format(invoice.total),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isIngreso) Color(0xFF059669) else Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Conceptos Facturados
        if (invoice.items.isNotEmpty()) {
            Text("CONCEPTOS FACTURADOS (${invoice.items.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(invoice.items) { item ->
                    ConceptItemRow(item, currencyFormat)
                }
            }
        }
    }
}

@Composable
fun ConceptItemRow(concept: InvoiceItemConcept, currencyFormat: NumberFormat) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = concept.description ?: "Concepto sin descripción",
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 2
                )
                if (concept.cantidad != null) {
                    Text(
                        text = "Cantidad: ${concept.cantidad}",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
                if (!concept.claveProdServ.isNullOrBlank()) {
                    Text(
                        text = "Clave SAT: ${concept.claveProdServ}",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = currencyFormat.format(concept.importe ?: 0.0),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF0F172A)
            )
        }
    }
}
