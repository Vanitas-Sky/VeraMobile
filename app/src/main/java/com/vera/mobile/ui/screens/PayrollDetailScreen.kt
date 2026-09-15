package com.vera.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.PayrollDetailItem
import com.vera.mobile.data.remote.PayrollDetailResponse
import com.vera.mobile.data.remote.RetrofitClient
import com.vera.mobile.ui.components.EmployeeReceiptSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollDetailScreen(token: String, periodId: Int, onBack: () -> Unit) {
    var data by remember { mutableStateOf<PayrollDetailResponse?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedEmployeeItem by remember { mutableStateOf<PayrollDetailItem?>(null) }
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

    // Filtrado en memoria por nombre o RFC
    val filteredDetails = remember(searchQuery, data) {
        val list = data?.details ?: emptyList()
        if (searchQuery.isBlank()) {
            list
        } else {
            list.filter { item ->
                val nameMatch = item.employee?.full_name?.contains(searchQuery, ignoreCase = true) == true
                val rfcMatch = item.employee?.rfc?.contains(searchQuery, ignoreCase = true) == true
                nameMatch || rfcMatch
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
                .padding(horizontal = 16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                errorMessage != null -> Text(errorMessage!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                data != null -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp) // Resuelve el corte del menú inferior
                    ) {
                        item {
                            PeriodSummaryHeader(data!!)
                        }

                        // Buscador
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Buscar por nombre o RFC...", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = Color.Gray)
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = Color(0xFF0F172A),
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (filteredDetails.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No se encontraron empleados.", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        } else {
                            items(filteredDetails) { item ->
                                EmployeePayrollCard(
                                    item = item,
                                    onClick = { selectedEmployeeItem = item }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedEmployeeItem?.let { selectedItem ->
        EmployeeReceiptSheet(
            item = selectedItem,
            periodId = periodId,
            periodName = data?.period?.period_name ?: "Periodo",
            token = token,
            onDismiss = { selectedEmployeeItem = null }
        )
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
fun EmployeePayrollCard(item: PayrollDetailItem, onClick: () -> Unit) {
    val rawPeriodicity = item.employee?.periodicity?.trim()?.lowercase() ?: "mensual"
    val factor = when (rawPeriodicity) {
        "semanal" -> 4.34
        "quincenal" -> 2.0
        else -> 1.0
    }
    val amountPerPeriod = item.net_salary / factor

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila 1: Nombre y Neto Mensual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.employee?.full_name ?: "Empleado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$ ${String.format("%,.2f", item.net_salary)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF059669)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Fila 2: Puesto e Importe por periodo (evita el salto de renglón)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.employee?.position ?: item.employee?.rfc ?: "",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dep. $rawPeriodicity: $ ${String.format("%,.2f", amountPerPeriod)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF047857),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Fila 3: Totales de deducciones con tipografía estándar limpia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bruto: $ ${String.format("%,.2f", item.gross_salary)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF334155)
                )
                Text(
                    text = "ISR: -$ ${String.format("%,.2f", item.isr_retention)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFDC2626)
                )
                Text(
                    text = "IMSS: -$ ${String.format("%,.2f", item.imss_employee)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFDC2626)
                )
            }

            // Desglose de Deducciones Especiales
            if (item.total_custom_deductions > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Otras Deducciones: -$ ${String.format("%,.2f", item.total_custom_deductions)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF92400E)
                        )
                        item.custom_deductions_breakdown?.forEach { deduction ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${deduction.description}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = "-$ ${String.format("%,.2f", deduction.amount)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
