package com.vera.mobile.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.PayrollDetailItem
import com.vera.mobile.data.remote.RetrofitClient
import com.vera.mobile.utils.saveAndSharePdf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeReceiptSheet(
    item: PayrollDetailItem,
    periodId: Int,
    periodName: String,
    token: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

    val rawPeriodicity = item.employee?.periodicity?.trim()?.lowercase() ?: "mensual"
    val factor = when (rawPeriodicity) {
        "semanal" -> 4.34
        "quincenal" -> 2.0
        else -> 1.0
    }
    val amountPerPeriod = item.net_salary / factor

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Encabezado del Recibo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.employee?.full_name ?: "Empleado",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "${item.employee?.position ?: "Sin puesto"} • $periodName",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Datos Laborales y Fiscales
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("RFC: ${item.employee?.rfc ?: "N/D"}", fontSize = 11.sp, color = Color(0xFF475569))
                        Text("NSS: ${item.employee?.nss ?: "N/D"}", fontSize = 11.sp, color = Color(0xFF475569))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Régimen: ${item.employee?.work_regime ?: "02 - Sueldos y Salarios"}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resumen de Montos
            Text("DESGLOSE DE PAGO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))

            ReceiptRow(label = "Sueldo Bruto Mensual", amount = item.gross_salary, isDeduction = false)
            ReceiptRow(label = "(-) Retención ISR", amount = item.isr_retention, isDeduction = true)
            ReceiptRow(label = "(-) Cuota IMSS Obrero", amount = item.imss_employee, isDeduction = true)

            // Deducciones Especiales si aplican
            if (item.total_custom_deductions > 0) {
                item.custom_deductions_breakdown?.forEach { deduction ->
                    ReceiptRow(label = "(-) ${deduction.description}", amount = deduction.amount, isDeduction = true)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2E8F0))

            // Neto a Pagar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Neto Mensual", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                Text("$ ${String.format("%,.2f", item.net_salary)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF059669))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dispersión por periodo ($rawPeriodicity)", fontSize = 12.sp, color = Color(0xFF64748B))
                Text("$ ${String.format("%,.2f", amountPerPeriod)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF059669))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Envío Rápido (Compartir PDF)
            Button(
                onClick = {
                    if (isDownloading) return@Button
                    isDownloading = true
                    coroutineScope.launch {
                        try {
                            val res = RetrofitClient.apiService.downloadPayrollPdf(
                                token = "Bearer $token",
                                periodId = periodId,
                                employeeId = item.employee?.id ?: 0
                            )
                            if (res.isSuccessful && res.body() != null) {
                                val fileName = "Recibo_${item.employee?.rfc ?: "empleado"}.pdf"
                                saveAndSharePdf(context, res.body()!!, fileName)
                            } else {
                                // Ver el código de error devuelto
                                println("Error del servidor: ${res.code()}")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isDownloading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Descargando PDF...")
                } else {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir PDF del Recibo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, amount: Double, isDeduction: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF475569))
        Text(
            text = "${if (isDeduction) "- " else ""}$ ${String.format("%,.2f", amount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDeduction) Color(0xFFDC2626) else Color(0xFF0F172A)
        )
    }
}
