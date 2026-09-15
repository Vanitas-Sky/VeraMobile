package com.vera.mobile.ui.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vera.mobile.data.remote.LoginRequest
import com.vera.mobile.data.remote.LoginResponse
import com.vera.mobile.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = mutableStateOf<LoginResult>(LoginResult.Idle)
    val loginState: State<LoginResult> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginResult.Loading
        viewModelScope.launch {
            val result = repository.login(LoginRequest(email, password))
            _loginState.value = result.fold(
                onSuccess = { LoginResult.Success(it) },
                onFailure = { LoginResult.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            _loginState.value = LoginResult.Idle
            onComplete()
        }
    }
}

sealed class LoginResult {
    object Idle : LoginResult()
    object Loading : LoginResult()
    data class Success(val data: LoginResponse) : LoginResult()
    data class Error(val message: String) : LoginResult()
}
