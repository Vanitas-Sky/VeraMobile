package com.vera.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.local.SessionManager
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.FixedExpenseCreateRequest
import com.vera.mobile.data.remote.FixedExpenseItem
import com.vera.mobile.data.remote.FixedExpensesResponse
import com.vera.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedExpensesScreen(
    token: String = "",
    onBack: () -> Unit = {},
    isAuditor: Boolean = false,
    apiService: ApiService = RetrofitClient.apiService,
    sessionManager: SessionManager? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    val isAuditorState by sessionManager?.isAuditor?.collectAsState(initial = isAuditor) ?: remember { mutableStateOf(isAuditor) }

    var data by remember { mutableStateOf<FixedExpensesResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Estados para modales
    var showCreateDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<FixedExpenseItem?>(null) }
    var expenseToToggle by remember { mutableStateOf<FixedExpenseItem?>(null) }

    // Filtros
    var selectedStatusFilter by remember { mutableStateOf("TODOS") } // "TODOS", "ACTIVOS", "INACTIVOS"
    var selectedCategoryFilter by remember { mutableStateOf("TODAS") } // "TODAS", "SERVICIO", "RENTA", "SEGURO", "OTRO"

    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            val authToken = if (token.isNotBlank()) token else sessionManager?.authToken?.firstOrNull() ?: ""
            if (authToken.isBlank()) {
                isLoading = false
                return@launch
            }
            try {
                val res = apiService.getFixedExpenses("Bearer $authToken")
                if (res.isSuccessful) {
                    data = res.body()
                } else {
                    Toast.makeText(context, "Error al cargar OpEx", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    // Filtrado de la lista en memoria
    val filteredExpenses = remember(data?.expenses, selectedStatusFilter, selectedCategoryFilter) {
        val list = data?.expenses ?: emptyList()
        list.filter { item ->
            val matchesStatus = when (selectedStatusFilter) {
                "ACTIVOS"   -> item.isActive
                "INACTIVOS" -> !item.isActive
                else        -> true
            }
            val matchesCategory = when (selectedCategoryFilter) {
                "TODAS" -> true
                else    -> item.category.equals(selectedCategoryFilter, ignoreCase = true)
            }
            matchesStatus && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Servicios y Rentas (OpEx)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        floatingActionButton = {
            if (!isAuditorState) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Contrato")
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF10B981))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Banner Auditor
                if (isAuditorState) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Modo Auditoría: Consulta de contratos OpEx en solo lectura.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }

                // Tarjetas de Resumen
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("OpEx Mensual", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                Text(
                                    currencyFormat.format(data?.totalMonthlyOpex ?: 0.0),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Contratos Activos", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${data?.activeContractsCount ?: 0}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }

                // Barra de Filtros
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Filtro Estatus
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "TODOS" to "Todos (${data?.expenses?.size ?: 0})",
                                "ACTIVOS" to "Activos (${data?.activeContractsCount ?: 0})",
                                "INACTIVOS" to "Inactivos (${(data?.expenses?.size ?: 0) - (data?.activeContractsCount ?: 0)})"
                            ).forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedStatusFilter == key,
                                    onClick = { selectedStatusFilter = key },
                                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0F172A),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Filtro Categoría
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "TODAS" to "Todas",
                                "SERVICIO" to "Servicios",
                                "RENTA" to "Rentas",
                                "SEGURO" to "Seguros",
                                "OTRO" to "Otros"
                            ).forEach { (catKey, catLabel) ->
                                FilterChip(
                                    selected = selectedCategoryFilter == catKey,
                                    onClick = { selectedCategoryFilter = catKey },
                                    label = { Text(catLabel, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF10B981),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Lista de Contratos
                if (filteredExpenses.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay contratos con los filtros seleccionados", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                    }
                } else {
                    items(filteredExpenses) { item ->
                        ExpenseCard(
                            expense = item,
                            isAuditor = isAuditorState,
                            onEdit = { expenseToEdit = item },
                            onToggle = { expenseToToggle = item }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }
    }

    // Modal: Crear Contrato
    if (showCreateDialog) {
        ExpenseFormDialog(
            title = "Nuevo Contrato OpEx",
            initialExpense = null,
            onDismiss = { showCreateDialog = false },
            onSubmit = { newContract ->
                coroutineScope.launch {
                    val authToken = if (token.isNotBlank()) token else sessionManager?.authToken?.firstOrNull() ?: return@launch
                    val res = apiService.createFixedExpense("Bearer $authToken", newContract)
                    if (res.isSuccessful) {
                        Toast.makeText(context, "Contrato registrado con éxito", Toast.LENGTH_SHORT).show()
                        showCreateDialog = false
                        loadData()
                    } else {
                        Toast.makeText(context, "Error al registrar contrato", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Modal: Editar Contrato
    if (expenseToEdit != null) {
        ExpenseFormDialog(
            title = "Editar Contrato",
            initialExpense = expenseToEdit,
            onDismiss = { expenseToEdit = null },
            onSubmit = { updatedContract ->
                coroutineScope.launch {
                    val authToken = if (token.isNotBlank()) token else sessionManager?.authToken?.firstOrNull() ?: return@launch
                    val res = apiService.updateFixedExpense("Bearer $authToken", expenseToEdit!!.id, updatedContract)
                    if (res.isSuccessful) {
                        Toast.makeText(context, "Contrato actualizado", Toast.LENGTH_SHORT).show()
                        expenseToEdit = null
                        loadData()
                    } else {
                        Toast.makeText(context, "Error al actualizar contrato", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Diálogo de Confirmación: Dar de Baja / Reactivar
    if (expenseToToggle != null) {
        val target = expenseToToggle!!
        val confirmColor = if (target.isActive) Color(0xFFEF4444) else Color(0xFF10B981)

        AlertDialog(
            onDismissRequest = { expenseToToggle = null },
            title = {
                Text(
                    text = if (target.isActive) "¿Dar de baja este contrato?" else "¿Reactivar contrato?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = if (target.isActive) {
                        "¿Estás seguro de dar de baja el contrato de \"${target.providerName}\"? Dejará de sumar al total mensual de OpEx."
                    } else {
                        "¿Deseas reactivar el contrato de \"${target.providerName}\"? Volverá a sumar a las proyecciones mensuales."
                    },
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val authToken = if (token.isNotBlank()) token else sessionManager?.authToken?.firstOrNull() ?: return@launch
                            val res = apiService.toggleFixedExpense("Bearer $authToken", target.id)
                            if (res.isSuccessful) {
                                Toast.makeText(context, res.body()?.message ?: "Estado actualizado", Toast.LENGTH_SHORT).show()
                                expenseToToggle = null
                                loadData()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
                ) {
                    Text(if (target.isActive) "Sí, dar de baja" else "Sí, reactivar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToToggle = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ExpenseCard(
    expense: FixedExpenseItem,
    isAuditor: Boolean,
    onEdit: () -> Unit,
    onToggle: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(expense.providerName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    if (!expense.description.isNullOrBlank()) {
                        Text(expense.description, fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }

                Surface(
                    color = if (expense.isActive) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (expense.isActive) "ACTIVO" else "INACTIVO",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (expense.isActive) Color(0xFF047857) else Color(0xFFB91C1C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${expense.category} · Pago el día ${expense.dueDay}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = currencyFormat.format(expense.monthlyAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                }

                // Acciones de Gestión (Solo Administrador)
                if (!isAuditor) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        }

                        TextButton(
                            onClick = onToggle,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (expense.isActive) "Dar de baja" else "Reactivar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (expense.isActive) Color(0xFFEF4444) else Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormDialog(
    title: String,
    initialExpense: FixedExpenseItem?,
    onDismiss: () -> Unit,
    onSubmit: (FixedExpenseCreateRequest) -> Unit
) {
    val categories = listOf(
        "SERVICIO" to "Servicios (Agua, Luz, Internet)",
        "RENTA"    to "Renta de Inmuebles / Oficinas",
        "SEGURO"   to "Seguros y Pólizas",
        "OTRO"     to "Otro / Licencias"
    )

    var providerName by remember { mutableStateOf(initialExpense?.providerName ?: "") }
    var description by remember { mutableStateOf(initialExpense?.description ?: "") }
    var selectedCategoryCode by remember { mutableStateOf(initialExpense?.category ?: "SERVICIO") }
    var selectedCategoryLabel by remember {
        mutableStateOf(categories.find { it.first.equals(initialExpense?.category ?: "SERVICIO", ignoreCase = true) }?.second ?: "Servicios (Agua, Luz, Internet)")
    }
    var monthlyAmount by remember { mutableStateOf(initialExpense?.monthlyAmount?.toString() ?: "") }
    var dueDay by remember { mutableStateOf(initialExpense?.dueDay?.toString() ?: "15") }
    var expandedDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = providerName,
                    onValueChange = { providerName = it },
                    label = { Text("Proveedor / Empresa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monthlyAmount,
                    onValueChange = { monthlyAmount = it },
                    label = { Text("Monto Mensual ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDay,
                    onValueChange = { dueDay = it },
                    label = { Text("Día de Pago (1-31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCategoryLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        categories.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedCategoryCode = code
                                    selectedCategoryLabel = label
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notas o descripción (opcional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = monthlyAmount.toDoubleOrNull() ?: 0.0
                    val day = dueDay.toIntOrNull() ?: 1
                    if (providerName.isNotBlank() && amount > 0.0) {
                        onSubmit(
                            FixedExpenseCreateRequest(
                                providerName = providerName.trim(),
                                description = description.trim().ifBlank { null },
                                category = selectedCategoryCode,
                                monthlyAmount = amount,
                                dueDay = day
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text(if (initialExpense == null) "Guardar Contrato" else "Actualizar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
