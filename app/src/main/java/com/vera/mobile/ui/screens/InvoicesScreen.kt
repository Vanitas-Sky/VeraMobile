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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.InvoiceItem
import com.vera.mobile.data.remote.InvoicesResponse
import com.vera.mobile.data.remote.RetrofitClient
import com.vera.mobile.ui.components.DropdownFilterSelector
import com.vera.mobile.ui.components.InvoiceDetailSheet
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    token: String,
    onBack: () -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    val monthNames = listOf(
        "Todos los meses", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    var selectedMonthIndex by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) } // 1 a 12
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR).toString()) }

    var selectedType by remember { mutableStateOf("todas") } // "todas", "I", "E"
    var searchQuery by remember { mutableStateOf("") }
    
    var data by remember { mutableStateOf<InvoicesResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedInvoiceId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val locale = remember { Locale("es", "MX") }

    val currentPeriod = remember(selectedYear, selectedMonthIndex) {
        if (selectedMonthIndex == 0) selectedYear
        else "$selectedYear-${selectedMonthIndex.toString().padStart(2, '0')}"
    }

    val fetchInvoices: () -> Unit = {
        scope.launch {
            try {
                val res = RetrofitClient.apiService.getInvoices(
                    token = "Bearer $token",
                    period = currentPeriod,
                    type = if (selectedType == "todas") null else selectedType,
                    status = "activas",
                    search = searchQuery.ifBlank { null }
                )
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

    LaunchedEffect(currentPeriod, selectedType, searchQuery) {
        fetchInvoices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bóveda de Facturas", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
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
                fetchInvoices()
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
                    // 1. Selector de Mes y Año
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1.3f)) {
                                DropdownFilterSelector(
                                    label = "Mes Fiscal",
                                    selectedOption = monthNames[selectedMonthIndex],
                                    options = monthNames,
                                    onOptionSelected = { name ->
                                        selectedMonthIndex = monthNames.indexOf(name)
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(0.9f)) {
                                DropdownFilterSelector(
                                    label = "Año",
                                    selectedOption = selectedYear,
                                    options = listOf("2026", "2025", "2024"),
                                    onOptionSelected = { selectedYear = it }
                                )
                            }
                        }
                    }

                    // 2. Resumen de totales del filtro
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "VOLUMEN FACTURADO EN PERIODO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$ ${String.format(locale, "%,.2f", data?.total_amount ?: 0.0)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Facturas: ${data?.count ?: 0}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFCBD5E1)
                                    )
                                    Text(
                                        text = "IVA Trasladado/Acred.: $ ${String.format(locale, "%,.2f", data?.total_iva ?: 0.0)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Buscador
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar cliente, proveedor, RFC o UUID...", fontSize = 12.sp) },
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

                    // 4. Filtro por tipo (Todas / Ingresos / Egresos)
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("todas" to "Todas", "I" to "Ingresos (Ventas)", "E" to "Egresos (Gastos)").forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedType == key,
                                    onClick = { selectedType = key },
                                    label = { Text(label, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0F172A),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // 5. Lista de Facturas
                    val invoiceList = data?.invoices ?: emptyList()
                    if (invoiceList.isEmpty()) {
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
                                    Text("No se encontraron facturas para este criterio.", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(invoiceList) { inv ->
                            InvoiceDetailCard(
                                inv = inv,
                                locale = locale,
                                onClick = { selectedInvoiceId = inv.id }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedInvoiceId?.let { id ->
        InvoiceDetailSheet(
            token = token,
            invoiceId = id,
            onDismiss = { selectedInvoiceId = null }
        )
    }
}

@Composable
fun InvoiceDetailCard(inv: InvoiceItem, locale: Locale, onClick: () -> Unit) {
    val isIngreso = inv.type.equals("I", ignoreCase = true)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(1.dp),
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
                    text = inv.partner_name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${inv.issue_date} • RFC: ${inv.partner_rfc ?: "N/D"}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "Folio: ${inv.uuid?.take(8) ?: "N/D"}...",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isIngreso) Color(0xFFECFDF5) else Color(0xFFFEF3C7)
            ) {
                Text(
                    text = "${if (isIngreso) "+" else "-"} $ ${String.format(locale, "%,.2f", inv.total)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIngreso) Color(0xFF059669) else Color(0xFFD97706),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
