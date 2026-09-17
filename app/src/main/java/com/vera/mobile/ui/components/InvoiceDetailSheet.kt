package com.vera.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.InvoiceDetailResponse
import com.vera.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailSheet(
    token: String,
    invoiceId: Int,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var detail by remember { mutableStateOf<InvoiceDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val mxLocale = remember { Locale("es", "MX") }

    LaunchedEffect(invoiceId) {
        try {
            val res = RetrofitClient.apiService.getInvoiceDetail("Bearer $token", invoiceId)
            if (res.isSuccessful) detail = res.body()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
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
        } else if (detail != null) {
            val item = detail!!
            val isIngreso = item.type.equals("I", ignoreCase = true)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.partner_name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "RFC: ${item.partner_rfc ?: "N/D"}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
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
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tarjeta de UUID con copiado
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
                            Text(item.uuid ?: "Sin UUID", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                        }
                        if (!item.uuid.isNullOrBlank()) {
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(item.uuid)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Importes
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text("$ ${String.format(mxLocale, "%,.2f", item.subtotal)}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("IVA (16%)", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text("$ ${String.format(mxLocale, "%,.2f", item.iva)}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Facturado", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(
                        "$ ${String.format(mxLocale, "%,.2f", item.total)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isIngreso) Color(0xFF059669) else Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Conceptos
                if (item.items.isNotEmpty()) {
                    Text("CONCEPTOS FACTURADOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.heightIn(max = 160.dp)
                    ) {
                        items(item.items) { concept ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(concept.description ?: "Concepto general", fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                        Text("Cantidad: ${concept.cantidad?.toString() ?: "No registrado"}", fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 2)
                                        Text("Clave SAT: ${concept.clave_prod_serv ?: "00000000"}", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                    Text("$ ${String.format(mxLocale, "%,.2f", concept.importe ?: 0.0)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
