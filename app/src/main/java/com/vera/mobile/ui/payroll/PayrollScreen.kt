package com.vera.mobile.ui.payroll

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.PayrollPeriod
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollsScreen(viewModel: PayrollViewModel, onBack: () -> Unit, onSelectPeriod: (Int) -> Unit) {
    val uiState by viewModel.uiState
    val locale = Locale("es", "MX")

    // Fecha actual para valores predeterminados (Ej. Año 2026, Septiembre)
    val calendar = remember { Calendar.getInstance() }
    val currentYear = calendar.get(Calendar.YEAR).toString()
    val monthNames = listOf(
        "Todos los meses", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(monthNames[calendar.get(Calendar.MONTH) + 1]) }

    PullToRefreshBox(
        isRefreshing = uiState is PayrollUiState.Loading && (uiState as? PayrollUiState.Loading) != PayrollUiState.Loading,
        onRefresh = { viewModel.loadPayrolls() },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        when (uiState) {
            is PayrollUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PayrollUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = (uiState as PayrollUiState.Error).message, color = Color.Red)
                    Button(onClick = { viewModel.loadPayrolls() }) {
                        Text("Reintentar")
                    }
                }
            }
            is PayrollUiState.Success -> {
                val payrolls = (uiState as PayrollUiState.Success).payrolls
                
                // Filtrado estricto por Año y Mes seleccionados
                val filteredPayrolls = remember(selectedYear, selectedMonth, payrolls) {
                    payrolls.filter { period ->
                        val matchYear = period.period_name.contains(selectedYear, ignoreCase = true)
                        val matchMonth = if (selectedMonth == "Todos los meses") true 
                                        else period.period_name.contains(selectedMonth, ignoreCase = true)
                        matchYear && matchMonth
                    }
                }

                // Totales calculados para el bloque de resumen
                val totalDispersed = filteredPayrolls.sumOf { it.total_net }
                val totalTaxes = filteredPayrolls.sumOf { it.total_isr_retention + it.total_imss_employee }

                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Selector Compacto de Año y Mes
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Selector de Mes
                            Box(modifier = Modifier.weight(1.3f)) {
                                DropdownFilterSelector(
                                    label = "Mes",
                                    selectedOption = selectedMonth,
                                    options = monthNames,
                                    onOptionSelected = { selectedMonth = it }
                                )
                            }

                            // Selector de Año
                            Box(modifier = Modifier.weight(0.9f)) {
                                val availableYears = listOf("2026", "2025", "2024")
                                DropdownFilterSelector(
                                    label = "Año",
                                    selectedOption = selectedYear,
                                    options = availableYears,
                                    onOptionSelected = { selectedYear = it }
                                )
                            }
                        }
                    }

                    // 2. Tarjeta Resumen Ejecutivo
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val dateLabel = if (selectedMonth == "Todos los meses") selectedYear 
                                               else "$selectedMonth $selectedYear"
                                Text(
                                    text = "TOTAL DISPERSADO (${dateLabel.uppercase()})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$ ${String.format(locale, "%,.2f", totalDispersed)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Periodos cerrados: ${filteredPayrolls.size}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFCBD5E1)
                                    )
                                    Text(
                                        text = "Retenciones: $ ${String.format(locale, "%,.2f", totalTaxes)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Listado de Periodos
                    if (filteredPayrolls.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sin nóminas calculadas para $selectedMonth $selectedYear",
                                        color = Color(0xFF64748B),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredPayrolls) { payroll ->
                            PayrollCardItem(
                                payroll = payroll,
                                onClick = { onSelectPeriod(payroll.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownFilterSelector(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        text = selectedOption,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xFF64748B)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 13.sp, color = Color(0xFF0F172A)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PayrollCardItem(payroll: PayrollPeriod, onClick: () -> Unit) {
    val locale = Locale("es", "MX")
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payroll.period_name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${payroll.details_count} colaboradores",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Dispersión Neta",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Text(
                    text = "$ ${String.format(locale, "%,.2f", payroll.total_net)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF059669)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver detalle",
                tint = Color(0xFF94A3B8)
            )
        }
    }
}
