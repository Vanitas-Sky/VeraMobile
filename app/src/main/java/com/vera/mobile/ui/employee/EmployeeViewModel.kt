package com.vera.mobile.ui.employee

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vera.mobile.data.local.TokenManager
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.Employee
import kotlinx.coroutines.launch

class EmployeeViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = mutableStateOf<EmployeeUiState>(EmployeeUiState.Loading)
    val uiState: State<EmployeeUiState> = _uiState

    init {
        loadEmployees()
    }

    fun loadEmployees() {
        val token = tokenManager.getToken()
        if (token == null) {
            _uiState.value = EmployeeUiState.Error("Sesión expirada")
            return
        }

        _uiState.value = EmployeeUiState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getEmployees("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = EmployeeUiState.Success(response.body()!!)
                } else {
                    _uiState.value = EmployeeUiState.Error("Error al cargar empleados: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = EmployeeUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}

sealed class EmployeeUiState {
    object Loading : EmployeeUiState()
    data class Success(val employees: List<Employee>) : EmployeeUiState()
    data class Error(val message: String) : EmployeeUiState()
}
