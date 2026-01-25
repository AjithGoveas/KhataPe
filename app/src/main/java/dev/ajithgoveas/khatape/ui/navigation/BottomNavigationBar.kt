package dev.ajithgoveas.khatape.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StackedBarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Triple(Route.DashboardGraph, "Home", Icons.Default.Home),
        Triple(Route.AnalyticsGraph, "Analytics", Icons.Default.StackedBarChart),
        Triple(Route.FriendsGraph, "Friends", Icons.Default.Group),
        Triple(Route.SettingsGraph, "Settings", Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        // Lowering elevation slightly for M3 look, tonal elevation handles it
        tonalElevation = 3.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { (route, label, icon) ->
            // Use hierarchy to keep tab active when in sub-screens
            val isSelected = currentDestination?.hierarchy?.any {
                it.hasRoute(route::class)
            } == true

            NavigationBarItem(
                selected = isSelected,
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                icon = { Icon(icon, contentDescription = label) },
                onClick = {
                    navController.navigate(route) {
                        // Standard Tab switching logic
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