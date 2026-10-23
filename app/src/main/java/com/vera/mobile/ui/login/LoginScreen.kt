package com.vera.mobile.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vera.mobile.R
import com.vera.mobile.data.local.SessionManager
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.RetrofitClient
import com.vera.mobile.ui.components.ForgotPasswordDialog
import com.vera.mobile.ui.components.TermsAcceptanceModal
import com.vera.mobile.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    apiService: ApiService = RetrofitClient.apiService,
    sessionManager: SessionManager? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo y Branding
            Icon(
                painter = painterResource(id = R.drawable.ic_vera_logo),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Vera Pocket",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Slate900,
                letterSpacing = (-0.5).sp
            )
            
            Text(
                text = "Gestión Fiscal Inteligente",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campos de Entrada
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Correo corporativo", color = TextMuted) },
                leadingIcon = { 
                    Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldPrimary) 
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Contraseña", color = TextMuted) },
                leadingIcon = { 
                    Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary) 
                },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, contentDescription = null, tint = TextSecondary)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // Botón "¿Olvidaste tu contraseña?"
            TextButton(
                onClick = { showForgotPasswordDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("¿Olvidaste tu contraseña?", fontSize = 12.sp, color = Color(0xFF64748B))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Acceso
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = loginState !is AuthUiState.Loading && email.isNotBlank() && password.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    disabledContainerColor = EmeraldPrimary.copy(alpha = 0.3f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (loginState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Mensajes de Error
            if (loginState is AuthUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (loginState as AuthUiState.Error).message,
                    color = RedDanger,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Modal de recuperación de contraseña
        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                apiService = apiService,
                onDismiss = { showForgotPasswordDialog = false },
                onPasswordResetSuccess = {
                    showForgotPasswordDialog = false
                }
            )
        }

        // Footer o versión
        Text(
            text = "v1.0.0 • Vera Technology",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            fontSize = 11.sp,
            color = TextMuted,
            letterSpacing = 1.sp
        )
    }

    // Efecto de Navegación y Verificación de Términos
    if (loginState is AuthUiState.Success) {
        val successData = (loginState as AuthUiState.Success).data
        val userTermsAccepted = successData.user?.termsAccepted ?: false

        if (!userTermsAccepted) {
            TermsAcceptanceModal(
                token = successData.token,
                apiService = apiService,
                baseUrl = "https://vera.sarl",
                onAccepted = {
                    coroutineScope.launch {
                        sessionManager?.saveSession(
                            token = successData.token,
                            role = successData.user?.role,
                            isAuditorUser = successData.user?.isAuditor ?: false
                        )
                        onLoginSuccess()
                    }
                },
                onDeclined = {
                    viewModel.resetLoginState()
                    Toast.makeText(context, "Debes aceptar los términos para usar la aplicación", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            LaunchedEffect(Unit) {
                onLoginSuccess()
            }
        }
    }
}
