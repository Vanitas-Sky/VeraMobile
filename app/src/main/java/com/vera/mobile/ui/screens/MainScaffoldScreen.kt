package com.vera.mobile.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.vera.mobile.ui.theme.*
import com.vera.mobile.ui.components.VeraAiFab

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

    when (currentSecondaryScreen) {
        "ai_chat" -> {
            AiConsultantScreen(
                token = token,
                onBack = { currentSecondaryScreen = null }
            )
        }
        "profile" -> {
            ProfileScreen(
                token = token,
                onBack = { currentSecondaryScreen = null },
                onLogout = onLogout
            )
        }
        "fixed_expenses" -> {
            FixedExpensesScreen(
                token = token,
                onBack = { currentSecondaryScreen = null }
            )
        }
        "invoices" -> {
            InvoicesScreen(
                token = token,
                onBack = { currentSecondaryScreen = null }
            )
        }
        else -> {
            Scaffold(
                topBar = {
                    if (currentSecondaryScreen != "ai_chat") {
                        TopAppBar(
                            title = {
                                Text(
                                    "Vera Pocket",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A)),
                            actions = {
                                IconButton(onClick = { currentSecondaryScreen = "invoices" }) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = "Bóveda de Facturas",
                                        tint = Color.White
                                    )
                                }
                                IconButton(onClick = { currentSecondaryScreen = "fixed_expenses" }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = "Servicios y Rentas",
                                        tint = Color.White
                                    )
                                }
                                IconButton(onClick = { currentSecondaryScreen = "profile" }) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Perfil y Configuración",
                                        tint = Color.White
                                    )
                                }
                            }
                        )
                    }
                },
                floatingActionButton = {
                    if (currentSecondaryScreen != "ai_chat") {
                        VeraAiFab(
                            onClick = { currentSecondaryScreen = "ai_chat" }
                        )
                    }
                },
                bottomBar = {
                    if (currentSecondaryScreen == null) {
                        NavigationBar(
                            containerColor = SurfaceWhite,
                            tonalElevation = 0.dp,
                            modifier = Modifier.border(width = 1.dp, color = BorderSubtle)
                        ) {
                            items.forEach { screen ->
                                val selected = currentDestination?.route == screen.route
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    selected = selected,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Slate900,
                                        selectedTextColor = Slate900,
                                        indicatorColor = EmeraldLight,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
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
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.fillMaxSize()
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
}
}
