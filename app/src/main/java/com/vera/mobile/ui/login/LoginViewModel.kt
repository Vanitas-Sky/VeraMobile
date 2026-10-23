package com.vera.mobile.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vera.mobile.data.remote.LoginResponse
import com.vera.mobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val data: LoginResponse) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class PasswordResetState {
    object Idle : PasswordResetState()
    object Loading : PasswordResetState()
    data class CodeSent(val message: String) : PasswordResetState()
    data class ResetSuccess(val message: String) : PasswordResetState()
    data class Error(val message: String) : PasswordResetState()
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _passwordResetState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val passwordResetState: StateFlow<PasswordResetState> = _passwordResetState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    /**
     * Ejecuta el inicio de sesión.
     */
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _loginState.value = AuthUiState.Error("Por favor completa todos los campos.")
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            val result = authRepository.login(email.trim(), pass)
            result.fold(
                onSuccess = { response ->
                    _loginState.value = AuthUiState.Success(response)
                    _navigationEvent.tryEmit("home")
                },
                onFailure = { error ->
                    _loginState.value = AuthUiState.Error(error.message ?: "Error al autenticar")
                }
            )
        }
    }

    /**
     * Ejecuta el registro de un nuevo usuario.
     */
    fun register(name: String, email: String, pass: String, confirmPass: String, terms: Boolean) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _loginState.value = AuthUiState.Error("Por favor completa todos los campos.")
            return
        }

        if (!terms) {
            _loginState.value = AuthUiState.Error("Debes aceptar los Términos y Condiciones.")
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            val result = authRepository.register(name.trim(), email.trim(), pass, confirmPass, terms)
            result.fold(
                onSuccess = { response ->
                    _loginState.value = AuthUiState.Success(response)
                    _navigationEvent.tryEmit("home")
                },
                onFailure = { error ->
                    _loginState.value = AuthUiState.Error(error.message ?: "Error al registrar la cuenta")
                }
            )
        }
    }

    /**
     * Dispara la solicitud de token al correo.
     */
    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _passwordResetState.value = PasswordResetState.Error("Ingresa tu correo registrado.")
            return
        }

        viewModelScope.launch {
            _passwordResetState.value = PasswordResetState.Loading
            val result = authRepository.forgotPassword(email.trim())
            result.fold(
                onSuccess = { response ->
                    _passwordResetState.value = PasswordResetState.CodeSent(
                        response.message.ifBlank { "Código de restablecimiento enviado." }
                    )
                },
                onFailure = { error ->
                    _passwordResetState.value = PasswordResetState.Error(
                        error.message ?: "No se pudo enviar el correo de recuperación."
                    )
                }
            )
        }
    }

    /**
     * Aplica el cambio de contraseña con el token recibido.
     */
    fun confirmPasswordReset(email: String, token: String, newPass: String, confirmPass: String) {
        if (token.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            _passwordResetState.value = PasswordResetState.Error("Todos los campos son obligatorios.")
            return
        }

        if (newPass != confirmPass) {
            _passwordResetState.value = PasswordResetState.Error("Las contraseñas no coinciden.")
            return
        }

        viewModelScope.launch {
            _passwordResetState.value = PasswordResetState.Loading
            val result = authRepository.resetPassword(email.trim(), token.trim(), newPass, confirmPass)
            result.fold(
                onSuccess = { response ->
                    _passwordResetState.value = PasswordResetState.ResetSuccess(
                        response.message.ifBlank { "Contraseña actualizada exitosamente." }
                    )
                },
                onFailure = { error ->
                    _passwordResetState.value = PasswordResetState.Error(
                        error.message ?: "Error al actualizar la contraseña."
                    )
                }
            )
        }
    }

    fun resetPasswordState() {
        _passwordResetState.value = PasswordResetState.Idle
    }

    fun resetLoginState() {
        _loginState.value = AuthUiState.Idle
    }

    fun clearLoginError() {
        if (_loginState.value is AuthUiState.Error) {
            _loginState.value = AuthUiState.Idle
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = AuthUiState.Idle
            onComplete()
        }
    }
}
