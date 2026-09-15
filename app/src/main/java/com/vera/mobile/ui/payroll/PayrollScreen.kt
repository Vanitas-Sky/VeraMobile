package com.vera.mobile.ui.payroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.PayrollPeriod
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollScreen(viewModel: PayrollViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Nóminas", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Aquí podrías usar Icon(Icons.Default.ArrowBack, ...)
                        // Por simplicidad usaremos texto o puedes importar iconos
                        Text("<", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(8.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp)
        ) {
            when (uiState) {
                is PayrollUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                    if (payrolls.isEmpty()) {
                        Text(
                            text = "No hay nóminas calculadas aún.",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(payrolls) { payroll ->
                                PayrollCard(payroll)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PayrollCard(payroll: PayrollPeriod) {
    val locale = Locale("es", "MX")
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = payroll.period_name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                Text(text = "Neto a Pagar", fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${payroll.details_count} empleados", fontSize = 12.sp, color = Color(0xFF475569))
                Text(
                    text = "$ ${String.format(locale, "%,.2f", payroll.total_net)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF10B981) // Color vera-green
                )
            }
        }
    }
}
