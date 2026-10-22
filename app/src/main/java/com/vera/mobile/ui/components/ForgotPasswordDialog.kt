package com.vera.mobile.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.ForgotPasswordRequest
import com.vera.mobile.data.remote.ResetPasswordRequest
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    apiService: ApiService,
    onPasswordResetSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var step by remember { mutableStateOf(1) } // Paso 1 = Solicitar; Paso 2 = Token + Password
    var email by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Column {
                Text(
                    text = if (step == 1) "Recuperar Contraseña" else "Nueva Contraseña",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = if (step == 1) "Paso 1: Validación de Correo" else "Paso 2: Código y Nueva Clave",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // ==========================================
                // PASO 1: SOLICITAR CÓDIGO
                // ==========================================
                if (step == 1) {
                    Text(
                        text = "Ingresa tu correo institucional registrado. Te enviaremos un código para validar tu identidad.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Correo Electrónico") },
                        placeholder = { Text("ejemplo@empresa.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64748B)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ==========================================
                // PASO 2: INGRESAR TOKEN Y NUEVA CONTRASEÑA
                // ==========================================
                else {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Para: $email",
                                fontSize = 11.sp,
                                color = Color(0xFF334155),
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(
                                onClick = {
                                    step = 1
                                    errorMessage = null
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Cambiar", fontSize = 11.sp, color = Color(0xFF059669))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = token,
                        onValueChange = {
                            token = it
                            errorMessage = null
                        },
                        label = { Text("Código de Recuperación") },
                        placeholder = { Text("Pega el código del correo") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF64748B)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        label = { Text("Nueva Contraseña") },
                        placeholder = { Text("Mínimo 6 caracteres") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        if (email.isBlank()) {
                            errorMessage = "Por favor ingresa un correo electrónico."
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            try {
                                val res = apiService.forgotPassword(ForgotPasswordRequest(email.trim()))
                                if (res.isSuccessful && res.body()?.success == true) {
                                    Toast.makeText(context, res.body()?.message ?: "Código enviado", Toast.LENGTH_SHORT).show()
                                    step = 2
                                } else {
                                    val errStr = res.errorBody()?.string()
                                    val parsedMsg = try {
                                        JSONObject(errStr ?: "").getString("message")
                                    } catch (e: Exception) {
                                        "No se pudo enviar el código. Revisa el correo."
                                    }
                                    errorMessage = parsedMsg
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error de red al conectar con el servidor."
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        if (token.isBlank() || newPassword.isBlank()) {
                            errorMessage = "Completa todos los campos."
                            return@Button
                        }
                        if (newPassword.length < 6) {
                            errorMessage = "La contraseña debe tener al menos 6 caracteres."
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            errorMessage = "Las contraseñas no coinciden."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            try {
                                val res = apiService.resetPassword(
                                    ResetPasswordRequest(
                                        email = email.trim(),
                                        token = token.trim(),
                                        password = newPassword,
                                        passwordConfirmation = confirmPassword
                                    )
                                )
                                if (res.isSuccessful && res.body()?.success == true) {
                                    Toast.makeText(context, res.body()?.message ?: "Contraseña actualizada", Toast.LENGTH_LONG).show()
                                    onPasswordResetSuccess()
                                    onDismiss()
                                } else {
                                    val errStr = res.errorBody()?.string()
                                    val parsedMsg = try {
                                        JSONObject(errStr ?: "").getString("message")
                                    } catch (e: Exception) {
                                        "Código inválido o expirado."
                                    }
                                    errorMessage = parsedMsg
                                }
                            } catch (e: Exception) {
                                errorMessage = "Fallo de conexión al actualizar contraseña."
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (step == 1) "Enviar Código" else "Actualizar Clave", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar", color = Color(0xFF64748B))
            }
        }
    )
}
