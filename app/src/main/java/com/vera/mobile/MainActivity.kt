package com.vera.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vera.mobile.data.local.SessionManager
import com.vera.mobile.data.remote.RetrofitClient
import com.vera.mobile.data.repository.AuthRepository
import com.vera.mobile.ui.employee.EmployeeViewModel
import com.vera.mobile.ui.home.HomeViewModel
import com.vera.mobile.ui.login.LoginScreen
import com.vera.mobile.ui.login.LoginViewModel
import com.vera.mobile.ui.payroll.PayrollViewModel
import com.vera.mobile.ui.screens.MainScaffoldScreen
import com.vera.mobile.ui.theme.VeraMobileTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(applicationContext)
        val apiService = RetrofitClient.apiService
        val repository = AuthRepository(apiService, sessionManager)
        val loginViewModel = LoginViewModel(repository)
        val homeViewModel = HomeViewModel(apiService, sessionManager)
        val payrollViewModel = PayrollViewModel(apiService, sessionManager)
        val employeeViewModel = EmployeeViewModel(apiService, sessionManager)

        enableEdgeToEdge()
        setContent {
            VeraMobileTheme {
                val tokenState by sessionManager.authToken.collectAsState(initial = "LOADING")

                when (tokenState) {
                    "LOADING" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    null -> {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                homeViewModel.loadDashboard()
                                payrollViewModel.loadPayrolls()
                                employeeViewModel.loadEmployees()
                            }
                        )
                    }
                    else -> {
                        val token = tokenState!!
                        MainScaffoldScreen(
                            token = token,
                            homeViewModel = homeViewModel,
                            payrollViewModel = payrollViewModel,
                            employeeViewModel = employeeViewModel,
                            onLogout = {
                                loginViewModel.logout {
                                    // El cambio en tokenState gatillará la UI de Login automáticamente
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
