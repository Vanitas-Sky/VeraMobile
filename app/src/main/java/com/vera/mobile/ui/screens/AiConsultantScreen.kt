package com.vera.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.AiAskRequest
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

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "¡Hola! Soy Vera AI, tu consultor fiscal y financiero. Puedes consultarme dudas sobre tus ventas del mes, gastos deducibles o el costo de nómina de cualquier colaborador.",
                isUser = false,
                time = "Ahora"
            )
        )
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
                            Text("En línea • Análisis en tiempo real", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
            // Lista de Mensajes
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
                                "Vera AI está consultando tus registros...",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Barra Inferior de Entrada
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
                        placeholder = { Text("Pregunta sobre nómina, facturas o SAT...", fontSize = 13.sp) },
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
                        onClick = {
                            val questionToSend = inputText.trim()
                            if (questionToSend.isNotBlank() && !isLoading) {
                                messages.add(ChatMessage(text = questionToSend, isUser = true, time = "Ahora"))
                                inputText = ""
                                isLoading = true

                                coroutineScope.launch {
                                    listState.animateScrollToItem(messages.size - 1)
                                    try {
                                        val res = RetrofitClient.apiService.askAi("Bearer $token", AiAskRequest(questionToSend))
                                        if (res.isSuccessful && res.body()?.success == true) {
                                            val reply = res.body()?.answer ?: "No obtuve respuesta."
                                            val time = res.body()?.timestamp ?: ""
                                            messages.add(ChatMessage(text = reply, isUser = false, time = time))
                                        } else {
                                            val errorMsg = res.body()?.error ?: "Error al conectar con Vera AI."
                                            messages.add(ChatMessage(text = "⚠️ $errorMsg", isUser = false))
                                        }
                                    } catch (e: Exception) {
                                        messages.add(ChatMessage(text = "⚠️ Error de red al contactar al asistente.", isUser = false))
                                    } finally {
                                        isLoading = false
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            }
                        },
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
                .widthIn(max = 300.dp)
                .then(
                    if (!message.isUser) Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                    else Modifier
                )
        ) {
            MarkdownText(
                text = message.text,
                color = if (message.isUser) Color.White else Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}
