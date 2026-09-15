package com.vera.mobile.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vera.mobile.data.local.SessionManager
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.DashboardSummaryResponse
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: DashboardSummaryResponse) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = mutableStateOf<HomeUiState>(HomeUiState.Loading)
    val uiState: State<HomeUiState> = _uiState

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
                val token = sessionManager.authToken.firstOrNull()
                if (token == null) {
                    _uiState.value = HomeUiState.Error("Sesión expirada")
                    return@launch
                }

                val response = apiService.getDashboardSummary("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = HomeUiState.Success(response.body()!!)
                } else {
                    _uiState.value = HomeUiState.Error("Error al cargar dashboard: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
