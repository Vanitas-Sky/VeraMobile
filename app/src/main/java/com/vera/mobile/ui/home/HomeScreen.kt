package com.vera.mobile.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.DashboardAlert
import com.vera.mobile.data.remote.DashboardSummaryResponse
import com.vera.mobile.data.remote.RecentInvoiceItem
import com.vera.mobile.ui.components.DropdownFilterSelector
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
    onNavigateToPayroll: () -> Unit,
    onNavigateToEmployees: () -> Unit,
    onNavigateToInvoices: () -> Unit
) {
    val uiState by viewModel.uiState
    val calendar = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR).toString()) }
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    var selectedMonthIndex by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }

    val currentPeriodString = remember(selectedYear, selectedMonthIndex) {
        val monthFormatted = selectedMonthIndex.toString().padStart(2, '0')
        "$selectedYear-$monthFormatted"
    }

    // Recargar al cambiar mes o año
    LaunchedEffect(currentPeriodString) {
        viewModel.loadDashboard(period = currentPeriodString)
    }

    PullToRefreshBox(
        isRefreshing = uiState is HomeUiState.Loading && (uiState as? HomeUiState.Loading) != HomeUiState.Loading,
        onRefresh = { viewModel.loadDashboard(currentPeriodString) },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        when (uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = (uiState as HomeUiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(onClick = { viewModel.loadDashboard(currentPeriodString) }) {
                        Text("Reintentar")
                    }
                }
            }
            is HomeUiState.Success -> {
                val data = (uiState as HomeUiState.Success).data
                DashboardContent(
                    data = data,
                    selectedMonthIndex = selectedMonthIndex,
                    selectedYear = selectedYear,
                    monthNames = monthNames,
                    onMonthSelected = { selectedMonthIndex = it },
                    onYearSelected = { selectedYear = it },
                    onNavigateToInvoices = onNavigateToInvoices
                )
            }
        }
    }
}

@Composable
fun DashboardContent(
    data: DashboardSummaryResponse,
    selectedMonthIndex: Int,
    selectedYear: String,
    monthNames: List<String>,
    onMonthSelected: (Int) -> Unit,
    onYearSelected: (String) -> Unit,
    onNavigateToInvoices: () -> Unit
) {
    val mxLocale = remember { Locale("es", "MX") }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Selector de Mes Fiscal
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1.3f)) {
                    DropdownFilterSelector(
                        label = "Mes Fiscal",
                        selectedOption = monthNames[selectedMonthIndex - 1],
                        options = monthNames,
                        onOptionSelected = { name ->
                            onMonthSelected(monthNames.indexOf(name) + 1)
                        }
                    )
                }
                Box(modifier = Modifier.weight(0.9f)) {
                    DropdownFilterSelector(
                        label = "Año",
                        selectedOption = selectedYear,
                        options = listOf("2026", "2025", "2024"),
                        onOptionSelected = { onYearSelected(it) }
                    )
                }
            }
        }

        // 2. Semáforo Fiscal de Discrepancia
        item {
            val semaforoColor = when (data.semaforo) {
                "verde" -> Color(0xFF10B981)
                "amarillo" -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            }

            val semaforoBg = when (data.semaforo) {
                "verde" -> Color(0xFFECFDF5)
                "amarillo" -> Color(0xFFFFFBEB)
                else -> Color(0xFFFEF2F2)
            }

            val textColor = when (data.semaforo) {
                "verde" -> Color(0xFF047857)
                "amarillo" -> Color(0xFFB45309)
                else -> Color(0xFFB91C1C)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = semaforoBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = semaforoColor,
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (data.semaforo == "verde") "ESTATUS SALUDABLE" 
                                   else if (data.semaforo == "amarillo") "ATENCIÓN REQUERIDA" 
                                   else "DISCREPANCIA DETECTADA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$ ${String.format(mxLocale, "%,.2f", data.discrepancy)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    Text(
                        text = data.mensaje_semaforo,
                        fontSize = 12.sp,
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 3. Tarjetas Cuadrículas: Ingresos, Gastos y Retiros
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Fila 1: Ingresos y Gastos
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Ingresos Totales",
                        amount = data.total_income,
                        subtitle = "Ventas facturadas",
                        locale = mxLocale,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Gastos Facturados",
                        amount = data.total_expense,
                        subtitle = "Egresos deducibles",
                        locale = mxLocale,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 2: Nómina Bruta y Retiros Bancarios
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Nómina Operativa",
                        amount = data.total_payroll_gross,
                        subtitle = "Percepciones brutas",
                        locale = mxLocale,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Retiros Bancarios",
                        amount = data.bank_withdrawals,
                        subtitle = "Salidas bancarias",
                        locale = mxLocale,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Centro de Notificaciones / Alertas
        if (data.alerts.isNotEmpty()) {
            item {
                Text(
                    text = "ALERTAS DEL MES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }
            items(data.alerts) { alert ->
                AlertCard(alert)
            }
        }

        // 5. Movimientos Recientes (Facturas)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToInvoices() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FACTURAS RECIENTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "Ver bóveda →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
            }
        }

        if (data.recent_invoices.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Sin facturas registradas en este periodo.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(data.recent_invoices) { invoice ->
                RecentInvoiceRow(invoice, mxLocale)
            }
        }
    }
}

@Composable
fun MetricCard(title: String, amount: Double, subtitle: String, locale: Locale, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$ ${String.format(locale, "%,.2f", amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AlertCard(alert: DashboardAlert) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF92400E))
                Spacer(modifier = Modifier.height(2.dp))
                Text(alert.description, fontSize = 12.sp, color = Color(0xFF78350F))
            }
        }
    }
}

@Composable
fun RecentInvoiceRow(invoice: RecentInvoiceItem, locale: Locale) {
    val isIngreso = invoice.type.equals("I", ignoreCase = true)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.partner_name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${invoice.issue_date} • ${invoice.uuid?.take(8) ?: "Sin UUID"}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isIngreso) Color(0xFFECFDF5) else Color(0xFFFEF3C7)
            ) {
                Text(
                    text = "${if (isIngreso) "+" else "-"} $ ${String.format(locale, "%,.2f", invoice.total)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIngreso) Color(0xFF059669) else Color(0xFFD97706),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
