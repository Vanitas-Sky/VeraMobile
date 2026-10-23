package com.vera.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.AuditorDto
import com.vera.mobile.data.remote.CreateAuditorRequest
import com.vera.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditorsScreen(
    token: String = "",
    apiService: ApiService = RetrofitClient.apiService,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

    var auditors by remember { mutableStateOf<List<AuditorDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var auditorToDelete by remember { mutableStateOf<AuditorDto?>(null) }

    fun loadAuditors() {
        isLoading = true
        coroutineScope.launch {
            try {
                val res = apiService.getAuditors(bearerToken)
                if (res.isSuccessful && res.body()?.success == true) {
                    auditors = res.body()?.auditors ?: emptyList()
                } else {
                    Toast.makeText(context, "Error al cargar la lista de auditores", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Fallo de conexión al cargar auditores", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAuditors()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Auditores", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF10B981),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Auditor")
            }
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF10B981),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (auditors.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No hay auditores asignados", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    Text("Pulsa '+' para dar de alta uno nuevo.", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(auditors) { auditor ->
                        AuditorCard(
                            auditor = auditor,
                            onDelete = { auditorToDelete = auditor }
                        )
                    }
                }
            }
        }
    }

    // Modal para registrar auditor
    if (showCreateDialog) {
        CreateAuditorDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, email, pass ->
                coroutineScope.launch {
                    try {
                        val res = apiService.createAuditor(bearerToken, CreateAuditorRequest(name, email, pass))
                        if (res.isSuccessful && res.body()?.success == true) {
                            Toast.makeText(context, "Auditor registrado con éxito", Toast.LENGTH_SHORT).show()
                            showCreateDialog = false
                            loadAuditors()
                        } else {
                            val errStr = res.errorBody()?.string()
                            val msg = try { JSONObject(errStr ?: "").getString("message") } catch (e: Exception) { "Error al registrar" }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error de red al guardar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Diálogo de confirmación de baja
    auditorToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { auditorToDelete = null },
            title = { Text("Confirmar Baja de Auditor") },
            text = { Text("¿Deseas revocar el acceso a ${target.name}? Perderá inmediatamente el acceso al portal y reportes.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val res = apiService.deleteAuditor(bearerToken, target.id)
                                if (res.isSuccessful && res.body()?.success == true) {
                                    Toast.makeText(context, "Acceso revocado", Toast.LENGTH_SHORT).show()
                                    auditorToDelete = null
                                    loadAuditors()
                                } else {
                                    Toast.makeText(context, "No se pudo revocar el auditor", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Dar de Baja")
                }
            },
            dismissButton = {
                TextButton(onClick = { auditorToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AuditorCard(auditor: AuditorDto, onDelete: () -> Unit) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF334155), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = auditor.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = auditor.email, color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFF87171))
            }
        }
    }
}

@Composable
fun CreateAuditorDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, pass: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nuevo Auditor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo institucional") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña provisional") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && password.length >= 6) {
                        onConfirm(name.trim(), email.trim(), password.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
