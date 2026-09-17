package com.vera.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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
import com.vera.mobile.data.remote.FixedExpenseAlert
import com.vera.mobile.data.remote.FixedExpenseModel
import com.vera.mobile.data.remote.FixedExpensesResponse
import com.vera.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedExpensesScreen(
    token: String,
    onBack: () -> Unit
) {
    var data by remember { mutableStateOf<FixedExpensesResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("Activos") }
    val scope = rememberCoroutineScope()
    val locale = remember { Locale("es", "MX") }

    val fetchData: () -> Unit = {
        scope.launch {
            try {
                val res = RetrofitClient.apiService.getFixedExpenses("Bearer $token")
                if (res.isSuccessful) {
                    data = res.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchData()
    }

    val filteredList = remember(searchQuery, filterStatus, data) {
        val list = data?.expenses ?: emptyList()
        list.filter { exp ->
            val matchQuery = exp.provider_name.contains(searchQuery, ignoreCase = true) ||
                    exp.category.contains(searchQuery, ignoreCase = true) ||
                    (exp.description?.contains(searchQuery, ignoreCase = true) == true)

            val matchStatus = when (filterStatus) {
                "Activos" -> exp.is_active
                "Inactivos" -> !exp.is_active
                else -> true
            }

            matchQuery && matchStatus
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Servicios y Rentas",
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
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                fetchData()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
        ) {
            if (isLoading && data == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0F172A))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Tarjeta Resumen OpEx (KPIs ejecutivos)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "GASTO FIJO MENSUAL (OPEX)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$ ${String.format(locale, "%,.2f", data?.total_monthly_opex ?: 0.0)}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Contratos: ${data?.active_contracts_count ?: 0} activos",
                                        fontSize = 12.sp,
                                        color = Color(0xFFCBD5E1)
                                    )
                                    Text(
                                        text = "Proy. Anual: $ ${String.format(locale, "%,.2f", data?.annual_projection ?: 0.0)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Alertas de Contratos y Pagos Próximos
                    if (!data?.alerts.isNullOrEmpty()) {
                        item {
                            Text(
                                "ALERTAS DE CONTRATOS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        items(data!!.alerts) { alert ->
                            FixedExpenseAlertCard(alert)
                        }
                    }

                    // 3. Buscador en tiempo real
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar proveedor, renta o servicio...", fontSize = 13.sp) },
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

                    // 4. Filtro por estatus
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

                    // 5. Listado de contratos/gastos fijos
                    if (filteredList.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No se encontraron gastos fijos.", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(filteredList) { item ->
                            FixedExpenseCardItem(item, locale)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FixedExpenseAlertCard(alert: FixedExpenseAlert) {
    val (bgColor, iconColor, textColor) = when (alert.type) {
        "danger" -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFF991B1B))
        "warning" -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Color(0xFF92400E))
        else -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), Color(0xFF1E40AF))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = if (alert.type == "info") Icons.Default.Info else Icons.Default.Warning,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                Spacer(modifier = Modifier.height(2.dp))
                Text(alert.message, fontSize = 11.sp, color = textColor)
            }
        }
    }
}

@Composable
fun FixedExpenseCardItem(item: FixedExpenseModel, locale: Locale) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.provider_name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                }
                Text(
                    text = "$ ${String.format(locale, "%,.2f", item.monthly_amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
            }

            if (!item.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Día de pago: ${item.due_day} de cada mes",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                if (item.contract_end_date != null) {
                    Text(
                        text = "Vence: ${item.contract_end_date}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}
