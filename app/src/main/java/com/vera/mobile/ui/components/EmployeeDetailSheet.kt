package com.vera.mobile.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.Employee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailSheet(
    employee: Employee,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

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
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = employee.full_name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = employee.position ?: "Sin puesto asignado",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (employee.is_active) Color(0xFFECFDF5) else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = if (employee.is_active) "Activo" else "Inactivo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (employee.is_active) Color(0xFF059669) else Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de Contacto Rápido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón de Llamada (Usa Intent.ACTION_DIAL, seguro y sin pedir permisos peligrosos)
                Button(
                    onClick = {
                        employee.phone?.let { phone ->
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(dialIntent)
                        }
                    },
                    enabled = !employee.phone.isNullOrBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        disabledContainerColor = Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Llamar",
                        tint = if (!employee.phone.isNullOrBlank()) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (!employee.phone.isNullOrBlank()) "Llamar" else "Sin teléfono",
                        fontSize = 12.sp,
                        color = if (!employee.phone.isNullOrBlank()) Color.White else Color(0xFF94A3B8)
                    )
                }

                // Botón de Correo
                OutlinedButton(
                    onClick = {
                        employee.email?.let { email ->
                            val mailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                            context.startActivity(mailIntent)
                        }
                    },
                    enabled = !employee.email.isNullOrBlank(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Correo",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Correo", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bloque 1: Identidad y Contratación
            Text(
                text = "IDENTIDAD Y CONTRATACIÓN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DataRow("RFC", employee.rfc ?: "N/D")
                    DataRow("CURP", employee.curp ?: "N/D")
                    DataRow("NSS", employee.nss ?: "N/D")
                    if (!employee.phone.isNullOrBlank()) {
                        DataRow("Teléfono", employee.phone)
                    }
                    DataRow("Esquema", employee.periodicity.replaceFirstChar { it.uppercase() })
                    DataRow("Salario Base", "$ ${String.format("%,.2f", employee.base_salary)}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bloque 2: Dispersión Bancaria (CLABE con copiado rápido)
            Text(
                text = "DISPERSIÓN BANCARIA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "CLABE / Cuenta", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = employee.clabe ?: "No registrada",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        )
                        if (!employee.clabe.isNullOrBlank()) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(employee.clabe))
                                },
                                modifier = Modifier.size(24.dp).padding(start = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar CLABE",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
    }
}
