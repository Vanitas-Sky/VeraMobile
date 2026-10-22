package com.vera.mobile.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.AiAskRequest
import com.vera.mobile.data.remote.AiHistoryItem
import com.vera.mobile.data.remote.RetrofitClient
import com.vera.mobile.ui.components.MarkdownText
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val time: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConsultantScreen(
    token: String,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val initialGreeting = "¡Hola! Soy Vera AI, tu consultor fiscal y financiero. Puedes consultarme dudas sobre el balance del mes, gastos deducibles, contratos OpEx o nómina de colaboradores."

    val messages = remember {
        mutableStateListOf(
            ChatMessage(text = initialGreeting, isUser = false, time = "Ahora")
        )
    }

    val quickSuggestions = listOf(
        "🟡 Auditoría de deducibilidad",
        "📊 Balance financiero del mes",
        "🏢 Resumen de contratos OpEx",
        "👥 Costo de plantilla y nómina"
    )

    fun sendQuestion(question: String) {
        val trimmed = question.trim()
        if (trimmed.isBlank() || isLoading) return

        // Tomar los últimos 10 mensajes previos para memoria conversacional
        val historyPayload = messages.map { msg ->
            AiHistoryItem(
                role = if (msg.isUser) "user" else "assistant",
                content = msg.text
            )
        }.takeLast(10)

        messages.add(ChatMessage(text = trimmed, isUser = true, time = "Ahora"))
        inputText = ""
        isLoading = true

        coroutineScope.launch {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
            try {
                val req = AiAskRequest(question = trimmed, history = historyPayload)
                val res = RetrofitClient.apiService.askAi("Bearer $token", req)
                if (res.isSuccessful && res.body()?.success == true) {
                    val reply = res.body()?.answer ?: "No obtuve respuesta del asistente."
                    val time = res.body()?.timestamp ?: ""
                    messages.add(ChatMessage(text = reply, isUser = false, time = time))
                } else {
                    val errorMsg = res.body()?.error ?: "No fue posible procesar la consulta."
                    messages.add(ChatMessage(text = "⚠️ $errorMsg", isUser = false))
                }
            } catch (e: Exception) {
                messages.add(ChatMessage(text = "⚠️ Error de conexión con el servicio de IA.", isUser = false))
            } finally {
                isLoading = false
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFECFDF5),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Consultor Vera AI", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("En línea • Análisis fiscal y contable", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        messages.clear()
                        messages.add(ChatMessage(text = initialGreeting, isUser = false, time = "Ahora"))
                    }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar Chat", tint = Color(0xFF94A3B8))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Conversación
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF059669)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Vera AI está analizando tus registros...",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Barra de Sugerencias Rápidas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickSuggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { sendQuestion(suggestion) },
                        label = { Text(suggestion, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color.White,
                            labelColor = Color(0xFF0F172A)
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color(0xFFE2E8F0)
                        )
                    )
                }
            }

            // Entrada de texto
            Surface(
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Pregunta sobre deducciones, nómina o gastos...", fontSize = 13.sp) },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFF0F172A)
                        )
                    )

                    IconButton(
                        onClick = { sendQuestion(inputText) },
                        enabled = inputText.isNotBlank() && !isLoading,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF0F172A),
                            disabledContainerColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = if (inputText.isNotBlank() && !isLoading) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (message.isUser) 14.dp else 2.dp,
                bottomEnd = if (message.isUser) 2.dp else 14.dp
            ),
            color = if (message.isUser) Color(0xFF0F172A) else Color.White,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .then(
                    if (!message.isUser) Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                    else Modifier
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                MarkdownText(
                    text = message.text,
                    color = if (message.isUser) Color.White else Color(0xFF1E293B)
                )
                if (message.time.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.time,
                        fontSize = 10.sp,
                        color = if (message.isUser) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}