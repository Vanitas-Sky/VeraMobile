package com.vera.mobile.ui.payroll

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vera.mobile.data.local.TokenManager
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.PayrollPeriod
import kotlinx.coroutines.launch

class PayrollViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = mutableStateOf<PayrollUiState>(PayrollUiState.Loading)
    val uiState: State<PayrollUiState> = _uiState

    init {
        loadPayrolls()
    }

    fun loadPayrolls() {
        val token = tokenManager.getToken()
        if (token == null) {
            _uiState.value = PayrollUiState.Error("Sesión expirada")
            return
        }

        _uiState.value = PayrollUiState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getPayrolls("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = PayrollUiState.Success(response.body()!!)
                } else {
                    _uiState.value = PayrollUiState.Error("Error al cargar nóminas: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = PayrollUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}

sealed class PayrollUiState {
    object Loading : PayrollUiState()
    data class Success(val payrolls: List<PayrollPeriod>) : PayrollUiState()
    data class Error(val message: String) : PayrollUiState()
}
