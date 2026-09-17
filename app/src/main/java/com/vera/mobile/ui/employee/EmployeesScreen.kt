package com.vera.mobile.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import com.vera.mobile.data.remote.Employee
import com.vera.mobile.ui.components.EmployeeDetailSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(viewModel: EmployeeViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("Activos") } // "Todos", "Activos", "Inactivos"
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }

    PullToRefreshBox(
        isRefreshing = uiState is EmployeeUiState.Loading && (uiState as? EmployeeUiState.Loading) != EmployeeUiState.Loading,
        onRefresh = { viewModel.loadEmployees() },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        when (uiState) {
            is EmployeeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
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

                // Filtrado por buscador y estado
                val filteredEmployees = remember(searchQuery, filterStatus, employees) {
                    employees.filter { emp ->
                        val matchesQuery = emp.full_name.contains(searchQuery, ignoreCase = true) ||
                                (emp.position?.contains(searchQuery, ignoreCase = true) == true) ||
                                (emp.rfc?.contains(searchQuery, ignoreCase = true) == true) ||
                                (emp.phone?.contains(searchQuery) == true)

                        val matchesStatus = when (filterStatus) {
                            "Activos" -> emp.is_active
                            "Inactivos" -> !emp.is_active
                            else -> true
                        }

                        matchesQuery && matchesStatus
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Buscador
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar por nombre, puesto o RFC...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
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

                    // Chips de estado
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Activos", "Inactivos", "Todos").forEach { status ->
                                FilterChip(
                                    selected = filterStatus == status,
                                    onClick = { filterStatus = status },
                                    label = { Text(status, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0F172A),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Listado de empleados
                    if (filteredEmployees.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No se encontraron colaboradores.", color = Color(0xFF64748B), fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(filteredEmployees) { emp ->
                            EmployeeItemCard(
                                employee = emp,
                                onClick = { selectedEmployee = emp }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedEmployee?.let { emp ->
        EmployeeDetailSheet(
            employee = emp,
            onDismiss = { selectedEmployee = null }
        )
    }
}

@Composable
fun EmployeeItemCard(employee: Employee, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.full_name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${employee.position ?: "Sin puesto"} • ${employee.periodicity.replaceFirstChar { it.uppercase() }}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (employee.is_active) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
            ) {
                Text(
                    text = if (employee.is_active) "Activo" else "Inactivo",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (employee.is_active) Color(0xFF059669) else Color(0xFFDC2626),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8)
            )
        }
    }
}
