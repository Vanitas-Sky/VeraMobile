package com.vera.mobile.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vera.mobile.ui.navigation.Screen
import com.vera.mobile.ui.home.DashboardScreen
import com.vera.mobile.ui.home.HomeViewModel
import com.vera.mobile.ui.payroll.PayrollsScreen
import com.vera.mobile.ui.payroll.PayrollViewModel
import com.vera.mobile.ui.employee.EmployeesScreen
import com.vera.mobile.ui.employee.EmployeeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffoldScreen(
    token: String,
    homeViewModel: HomeViewModel,
    payrollViewModel: PayrollViewModel,
    employeeViewModel: EmployeeViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var currentSecondaryScreen by remember { mutableStateOf<String?>(null) }

    val items = listOf(
        Screen.Dashboard,
        Screen.Payrolls,
        Screen.Employees
    )

    if (currentSecondaryScreen == "fixed_expenses") {
        FixedExpensesScreen(
            token = token,
            onBack = { currentSecondaryScreen = null }
        )
    } else if (currentSecondaryScreen == "invoices") {
        InvoicesScreen(
            token = token,
            onBack = { currentSecondaryScreen = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Vera Pocket", fontWeight = FontWeight.Bold, color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A)),
                    actions = {
                        IconButton(onClick = { currentSecondaryScreen = "fixed_expenses" }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = "Servicios y Rentas",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar Sesión",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    items.forEach { screen ->
                        val selected = currentDestination?.route == screen.route
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0F172A),
                                selectedTextColor = Color(0xFF0F172A),
                                indicatorColor = Color(0xFFE2E8F0)
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = homeViewModel,
                        onLogout = onLogout,
                        onNavigateToPayroll = { navController.navigate(Screen.Payrolls.route) },
                        onNavigateToEmployees = { navController.navigate(Screen.Employees.route) },
                        onNavigateToInvoices = { currentSecondaryScreen = "invoices" }
                    )
                }
                composable(Screen.Payrolls.route) {
                    PayrollsScreen(
                        viewModel = payrollViewModel,
                        onBack = { navController.popBackStack() },
                        onSelectPeriod = { periodId ->
                            navController.navigate(Screen.PayrollDetail.createRoute(periodId))
                        }
                    )
                }
                composable(
                    route = Screen.PayrollDetail.route,
                    arguments = listOf(navArgument("periodId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val periodId = backStackEntry.arguments?.getInt("periodId") ?: 0
                    PayrollDetailScreen(
                        token = token,
                        periodId = periodId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Employees.route) {
                    EmployeesScreen(
                        viewModel = employeeViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
