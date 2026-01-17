package dev.ajithgoveas.khatape.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

// A sealed class hierarchy to define all screens and their logical grouping.
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector? = null,
    val showInBottomNav: Boolean = false
) {
    // Top-level navigation items that appear in the Bottom Navigation Bar.
    // These act as the root of their own navigation graphs.
    sealed class BottomNavItem(
        route: String,
        label: String,
        icon: ImageVector
    ) : Screen(route = route, label = label, icon = icon, showInBottomNav = true) {

        object Dashboard : BottomNavItem(
            route = "dashboard_graph",
            label = "Dashboard",
            icon = Icons.Default.Home
        )

        object Friends : BottomNavItem(
            route = "friends_graph",
            label = "Friends",
            icon = Icons.Default.Group
        )

        object Settings : BottomNavItem(
            route = "settings_graph",
            label = "Settings",
            icon = Icons.Default.Settings
        )
    }

    // Screens that are part of the 'Friends' navigation graph.
    // These are not shown in the bottom navigation bar.
    sealed class FriendsScreens(
        path: String,
        label: String,
        icon: ImageVector? = null,
    ) : Screen(route = path, label = label, icon = icon) {

        object FriendsList : FriendsScreens(
            path = "friends_list",
            label = "Friends List",
            icon = Icons.Default.Group
        )

        object FriendDetail : FriendsScreens(
            path = "friend_detail/{friendId}",
            label = "Friend Detail",
        ) {
            fun createRoute(friendId: Long) = "friend_detail/$friendId"
        }

        object AddExpense : FriendsScreens(
            path = "add_expense/{friendId}", // Using path parameter for consistency
            label = "Add Expense"
        ) {
            fun createRoute(friendId: Long?): String =
                if (friendId != null) "add_expense/$friendId" else "add_expense/-1"
        }

        object ViewExpense : FriendsScreens(
            path = "view_expense/{expenseId}", // Using path parameter for consistency
            label = "View Expense"
        ) {
            fun createRoute(expenseId: Long?): String =
                if (expenseId != null) "view_expense/$expenseId" else "view_expense/-1"
        }

        object EditExpense : FriendsScreens(
            path = "edit_expense/{expenseId}", // Using path parameter for consistency
            label = "edit Expense"
        ) {
            fun createRoute(expenseId: Long?): String =
                if (expenseId != null) "edit_expense/$expenseId" else "edit_expense/-1"
        }

    }
}