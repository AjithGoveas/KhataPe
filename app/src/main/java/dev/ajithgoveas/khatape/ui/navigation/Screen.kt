package dev.ajithgoveas.khatape.ui.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    // Graphs
    @Serializable
    object DashboardGraph : Route()
    @Serializable
    object FriendsGraph : Route()
    @Serializable
    object SettingsGraph : Route()

    // Dashboard Screens
    @Serializable
    object Dashboard : Route()

    // Friends Screens
    @Serializable
    object FriendsList : Route()
    @Serializable
    data class FriendDetail(val friendId: Long) : Route()
    @Serializable
    data class AddExpense(val friendId: Long) : Route()
    @Serializable
    data class ViewExpense(val expenseId: Long) : Route()
    @Serializable
    data class EditExpense(val expenseId: Long) : Route()

    // Settings Screens
    @Serializable
    object Settings : Route()
}