package com.vera.mobile.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vera.mobile.data.local.TokenManager
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.DashboardSummaryResponse
import kotlinx.coroutines.launch

class HomeViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = mutableStateOf<HomeUiState>(HomeUiState.Loading)
    val uiState: State<HomeUiState> = _uiState

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        val token = tokenManager.getToken()
        if (token == null) {
            _uiState.value = HomeUiState.Error("Sesión expirada")
            return
        }

        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
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

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: DashboardSummaryResponse) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
