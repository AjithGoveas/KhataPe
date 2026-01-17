package dev.ajithgoveas.khatape

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import dev.ajithgoveas.khatape.ui.navigation.BottomNavigationBar
import dev.ajithgoveas.khatape.ui.navigation.Screen
import dev.ajithgoveas.khatape.ui.screen.addExpense.AddExpenseScreen
import dev.ajithgoveas.khatape.ui.screen.dashboard.DashboardScreen
import dev.ajithgoveas.khatape.ui.screen.editExpense.EditExpenseScreen
import dev.ajithgoveas.khatape.ui.screen.friendDetail.FriendDetailScreen
import dev.ajithgoveas.khatape.ui.screen.friends.FriendsScreen
import dev.ajithgoveas.khatape.ui.screen.settings.SettingsScreen
import dev.ajithgoveas.khatape.ui.screen.viewExpense.ViewExpenseScreen
import dev.ajithgoveas.khatape.ui.theme.KhataPeTheme

@Composable
fun KhataPe(navController: NavHostController = rememberNavController()) {
    KhataPeTheme {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    // The start destination is now the route of the top-level graph
                    startDestination = Screen.BottomNavItem.Dashboard.route
                ) {

                    // Dashboard Navigation Graph
                    navigation(
                        route = Screen.BottomNavItem.Dashboard.route,
                        startDestination = "dashboard_screen"
                    ) {
                        composable("dashboard_screen") {
                            DashboardScreen()
                        }
                    }

                    // Friends Navigation Graph
                    navigation(
                        route = Screen.BottomNavItem.Friends.route,
                        startDestination = Screen.FriendsScreens.FriendsList.route
                    ) {
                        composable(Screen.FriendsScreens.FriendsList.route) {
                            FriendsScreen(navController = navController)
                        }

                        composable(
                            route = Screen.FriendsScreens.FriendDetail.route,
                            arguments = listOf(navArgument("friendId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val friendId = backStackEntry.arguments?.getLong("friendId") ?: -1L
                            FriendDetailScreen(friendId = friendId, navController = navController)
                        }

                        composable(
                            route = Screen.FriendsScreens.AddExpense.route,
                            arguments = listOf(
                                navArgument("friendId") {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) { backStackEntry ->
                            val friendId = backStackEntry.arguments?.getLong("friendId") ?: -1L
                            AddExpenseScreen(friendId = friendId, navController = navController)
                        }

                        composable(
                            route = Screen.FriendsScreens.ViewExpense.route,
                            arguments = listOf(
                                navArgument("expenseId") {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) { backStackEntry ->
                            val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: -1L
                            ViewExpenseScreen(expenseId = expenseId, navController = navController)
                        }

                        composable(
                            route = Screen.FriendsScreens.EditExpense.route,
                            arguments = listOf(
                                navArgument("expenseId") {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) { backStackEntry ->
                            val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: -1L
                            EditExpenseScreen(expenseId = expenseId, navController = navController)
                        }
                    }

                    // Settings Navigation Graph
                    navigation(
                        route = Screen.BottomNavItem.Settings.route,
                        startDestination = "settings_screen"
                    ) {
                        composable("settings_screen") {
                            SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}