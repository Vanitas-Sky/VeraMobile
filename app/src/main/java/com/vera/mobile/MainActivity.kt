package com.vera.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.vera.mobile.data.local.TokenManager
import com.vera.mobile.data.remote.RetrofitClient
import com.vera.mobile.data.repository.AuthRepository
import com.vera.mobile.ui.employee.EmployeeViewModel
import com.vera.mobile.ui.employee.EmployeesScreen
import com.vera.mobile.ui.home.HomeScreen
import com.vera.mobile.ui.home.HomeViewModel
import com.vera.mobile.ui.login.LoginScreen
import com.vera.mobile.ui.login.LoginViewModel
import com.vera.mobile.ui.payroll.PayrollScreen
import com.vera.mobile.ui.payroll.PayrollViewModel
import com.vera.mobile.ui.theme.VeraMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val tokenManager = TokenManager(applicationContext)
        val apiService = RetrofitClient.apiService
        val repository = AuthRepository(apiService, tokenManager)
        val loginViewModel = LoginViewModel(repository)
        val homeViewModel = HomeViewModel(apiService, tokenManager)
        val payrollViewModel = PayrollViewModel(apiService, tokenManager)
        val employeeViewModel = EmployeeViewModel(apiService, tokenManager)

        enableEdgeToEdge()
        setContent {
            VeraMobileTheme {
                var isLoggedIn by remember { mutableStateOf(tokenManager.getToken() != null) }
                var currentScreen by remember { mutableStateOf("home") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (isLoggedIn) {
                            when (currentScreen) {
                                "home" -> HomeScreen(
                                    viewModel = homeViewModel,
                                    onLogout = {
                                        tokenManager.deleteToken()
                                        isLoggedIn = false
                                    },
                                    onNavigateToPayroll = {
                                        currentScreen = "payroll"
                                    },
                                    onNavigateToEmployees = {
                                        currentScreen = "employees"
                                    }
                                )
                                "payroll" -> PayrollScreen(
                                    viewModel = payrollViewModel,
                                    onBack = {
                                        currentScreen = "home"
                                    }
                                )
                                "employees" -> EmployeesScreen(
                                    viewModel = employeeViewModel,
                                    onBack = {
                                        currentScreen = "home"
                                    }
                                )
                            }
                        } else {
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = {
                                    isLoggedIn = true
                                    homeViewModel.loadDashboard()
                                    payrollViewModel.loadPayrolls()
                                    employeeViewModel.loadEmployees()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
