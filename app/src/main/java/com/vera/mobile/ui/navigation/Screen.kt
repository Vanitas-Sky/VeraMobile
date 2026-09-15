package com.vera.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Home)
    object Payrolls : Screen("payrolls", "Nóminas", Icons.Default.DateRange)
    object Employees : Screen("employees", "Equipo", Icons.Default.Person)
    object PayrollDetail : Screen("payroll_detail/{periodId}", "Detalle Nómina", Icons.Default.DateRange) {
        fun createRoute(periodId: Int) = "payroll_detail/$periodId"
    }
}
