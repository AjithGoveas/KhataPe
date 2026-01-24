package dev.ajithgoveas.khatape

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import dev.ajithgoveas.khatape.ui.navigation.BottomNavigationBar
import dev.ajithgoveas.khatape.ui.navigation.Route
import dev.ajithgoveas.khatape.ui.screen.addExpense.AddExpenseScreen
import dev.ajithgoveas.khatape.ui.screen.dashboard.DashboardScreen
import dev.ajithgoveas.khatape.ui.screen.editExpense.EditExpenseScreen
import dev.ajithgoveas.khatape.ui.screen.friendDetail.FriendDetailScreen
import dev.ajithgoveas.khatape.ui.screen.friends.FriendsScreen
import dev.ajithgoveas.khatape.ui.screen.settings.SettingsScreen
import dev.ajithgoveas.khatape.ui.screen.viewExpense.ViewExpenseScreen
import dev.ajithgoveas.khatape.ui.theme.KhataPeTheme

@Composable
fun KhataPe(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Proper way to check for route visibility using reified hasRoute
    val shouldShowBottomBar = currentDestination?.let { dest ->
        dest.hasRoute<Route.Dashboard>() ||
                dest.hasRoute<Route.FriendsList>() ||
                dest.hasRoute<Route.Settings>()
    } ?: false

    KhataPeTheme {
        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar) BottomNavigationBar(navController)
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.DashboardGraph,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(400)) },
                exitTransition = { fadeOut(animationSpec = tween(400)) }
            ) {
                // Dashboard Graph
                navigation<Route.DashboardGraph>(startDestination = Route.Dashboard) {
                    composable<Route.Dashboard> { DashboardScreen() }
                }

                // Friends Graph
                navigation<Route.FriendsGraph>(startDestination = Route.FriendsList) {
                    composable<Route.FriendsList> {
                        FriendsScreen(navController = navController)
                    }
                    composable<Route.FriendDetail> { backStackEntry ->
                        val args = backStackEntry.toRoute<Route.FriendDetail>()
                        FriendDetailScreen(friendId = args.friendId, navController = navController)
                    }
                    composable<Route.AddExpense> { backStackEntry ->
                        val args = backStackEntry.toRoute<Route.AddExpense>()
                        AddExpenseScreen(friendId = args.friendId, navController = navController)
                    }
                    composable<Route.ViewExpense> { backStackEntry ->
                        val args = backStackEntry.toRoute<Route.ViewExpense>()
                        ViewExpenseScreen(expenseId = args.expenseId, navController = navController)
                    }
                    composable<Route.EditExpense> { backStackEntry ->
                        val args = backStackEntry.toRoute<Route.EditExpense>()
                        EditExpenseScreen(expenseId = args.expenseId, navController = navController)
                    }
                }

                // Settings Graph
                navigation<Route.SettingsGraph>(startDestination = Route.Settings) {
                    composable<Route.Settings> { SettingsScreen() }
                }
            }
        }
    }
}