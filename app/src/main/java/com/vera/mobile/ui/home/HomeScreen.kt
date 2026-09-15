package com.vera.mobile.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.DashboardSummaryResponse

import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
    onNavigateToPayroll: () -> Unit,
    onNavigateToEmployees: () -> Unit
) {
    val uiState by viewModel.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        when (uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                    Button(onClick = { viewModel.loadDashboard() }) {
                        Text("Reintentar")
                    }
                }
            }
            is HomeUiState.Success -> {
                val summary = (uiState as HomeUiState.Success).data
                DashboardContent(summary)
            }
        }
    }
}

@Composable
fun DashboardContent(summary: DashboardSummaryResponse) {
    val locale = Locale("es", "MX")
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta de Bienvenida Empresa
        Text(
            text = summary.companyName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        // Tarjeta de Discrepancia Fiscal (Riesgo)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (summary.hasRisk) Color(0xFFFEF2F2) else Color(0xFFECFDF5)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (summary.hasRisk) "DISCREPANCIA FISCAL (RIESGO)" else "ESTATUS SALUDABLE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (summary.hasRisk) Color(0xFFDC2626) else Color(0xFF059669)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$ ${String.format(locale, "%,.2f", summary.fiscalDiscrepancy)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (summary.hasRisk) Color(0xFF991B1B) else Color(0xFF065F46)
                )
                if (summary.hasRisk) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ Tienes retiros bancarios sin una factura que los ampare.",
                        fontSize = 12.sp,
                        color = Color(0xFFB91C1C)
                    )
                }
            }
        }

        // Fila de Tarjetas Secundarias (Gastos vs Retiros)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                title = "Gastos Facturados",
                amount = summary.totalInvoicedExpenses,
                locale = locale,
                modifier = Modifier.weight(1f),
                textColor = Color(0xFF0F172A)
            )
            MetricCard(
                title = "Retiros Bancarios",
                amount = summary.totalBankWithdrawals,
                locale = locale,
                modifier = Modifier.weight(1f),
                textColor = Color(0xFF475569)
            )
        }
    }
}

@Composable
fun MetricCard(title: String, amount: Double, locale: Locale, modifier: Modifier = Modifier, textColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$ ${String.format(locale, "%,.2f", amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
