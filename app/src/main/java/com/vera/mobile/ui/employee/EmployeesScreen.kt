package com.vera.mobile.ui.employee

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
import com.vera.mobile.data.remote.Employee
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(viewModel: EmployeeViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Directorio de Empleados", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                is EmployeeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is EmployeeUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = (uiState as EmployeeUiState.Error).message, color = Color.Red)
                        Button(onClick = { viewModel.loadEmployees() }) {
                            Text("Reintentar")
                        }
                    }
                }
                is EmployeeUiState.Success -> {
                    val employees = (uiState as EmployeeUiState.Success).employees
                    if (employees.isEmpty()) {
                        Text(
                            text = "No hay empleados registrados.",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(employees) { employee ->
                                EmployeeCard(employee)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeCard(employee: Employee) {
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
                Text(
                    text = employee.full_name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (employee.is_active) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                ) {
                    Text(
                        text = if (employee.is_active) "Activo" else "Inactivo",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (employee.is_active) Color(0xFF059669) else Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = employee.position ?: "Sin puesto asignado",
                fontSize = 12.sp,
                color = Color(0xFF475569),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Periodo: ${employee.periodicity.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "$ ${String.format(locale, "%,.2f", employee.base_salary)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}
