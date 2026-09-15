package com.vera.mobile.ui.employee

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vera.mobile.data.local.SessionManager
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.Employee
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class EmployeeUiState {
    object Loading : EmployeeUiState()
    data class Success(val employees: List<Employee>) : EmployeeUiState()
    data class Error(val message: String) : EmployeeUiState()
}

class EmployeeViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = mutableStateOf<EmployeeUiState>(EmployeeUiState.Loading)
    val uiState: State<EmployeeUiState> = _uiState

    init {
        loadEmployees()
    }

    fun loadEmployees() {
        _uiState.value = EmployeeUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionManager.authToken.firstOrNull()
                if (token == null) {
                    _uiState.value = EmployeeUiState.Error("Sesión expirada")
                    return@launch
                }

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
