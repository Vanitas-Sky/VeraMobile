package com.vera.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.PayrollDetailItem
import com.vera.mobile.data.remote.PayrollDetailResponse
import com.vera.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollDetailScreen(token: String, periodId: Int, onBack: () -> Unit) {
    var data by remember { mutableStateOf<PayrollDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(periodId) {
        scope.launch {
            try {
                val res = RetrofitClient.apiService.getPayrollDetails("Bearer $token", periodId)
                if (res.isSuccessful && res.body() != null) {
                    data = res.body()
                } else {
                    errorMessage = "Error al cargar los recibos del periodo"
                }
            } catch (e: Exception) {
                errorMessage = "Fallo de red: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(data?.period?.period_name ?: "Detalle de Nómina", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                errorMessage != null -> Text(errorMessage!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                data != null -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            PeriodSummaryHeader(data!!)
                        }
                        items(data!!.details) { item ->
                            EmployeePayrollCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodSummaryHeader(data: PayrollDetailResponse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("TOTAL DISPERSADO", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "$ ${String.format("%,.2f", data.period.total_net)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ISR Retenido: $ ${String.format("%,.2f", data.period.total_isr_retention)}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                Text("IMSS Obrero: $ ${String.format("%,.2f", data.period.total_imss_employee)}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
            }
        }
    }
}

@Composable
fun EmployeePayrollCard(item: PayrollDetailItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.employee?.full_name ?: "Empleado", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                Text("$ ${String.format("%,.2f", item.net_salary)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF10B981))
            }
            Text(item.employee?.position ?: item.employee?.rfc ?: "", fontSize = 12.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bruto: $ ${String.format("%,.2f", item.gross_salary)}", fontSize = 11.sp, color = Color(0xFF475569))
                Text("ISR: -$ ${String.format("%,.2f", item.isr_retention)}", fontSize = 11.sp, color = Color(0xFFDC2626))
                Text("IMSS: -$ ${String.format("%,.2f", item.imss_employee)}", fontSize = 11.sp, color = Color(0xFFDC2626))
            }
        }
    }
}
