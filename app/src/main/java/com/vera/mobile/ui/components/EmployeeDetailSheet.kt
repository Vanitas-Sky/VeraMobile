package com.vera.mobile.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.Employee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailSheet(employee: Employee, onDismiss: () -> Unit) {
    val context = LocalContext.current

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
            // Nombre y estatus
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
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (employee.is_active) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                ) {
                    Text(
                        text = if (employee.is_active) "Activo" else "Inactivo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (employee.is_active) Color(0xFF059669) else Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción rápida: Llamar y Correo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        employee.email?.let { email ->
                            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                            context.startActivity(emailIntent)
                        }
                    },
                    enabled = !employee.email.isNullOrBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Correo", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Identidad y Registro Laboral
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("IDENTIDAD Y CONTRATACIÓN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    InfoRow("RFC", employee.rfc ?: "N/D")
                    InfoRow("CURP", employee.curp ?: "N/D")
                    InfoRow("NSS", employee.nss ?: "N/D")
                    InfoRow("Esquema", employee.periodicity.replaceFirstChar { it.uppercase() })
                    InfoRow("Salario Base", "$ ${String.format("%,.2f", employee.base_salary)}")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Datos Bancarios
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("DISPERSIÓN BANCARIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    InfoRow("CLABE / Cuenta", employee.clabe ?: "No registrada")
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
    }
}
