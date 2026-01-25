package dev.ajithgoveas.khatape.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    // Graphs
    @Serializable
    data object DashboardGraph : Route()
    @Serializable
    data object AnalyticsGraph : Route()
    @Serializable
    data object FriendsGraph : Route()
    @Serializable
    data object SettingsGraph : Route()

    // Dashboard Screens
    @Serializable
    data object Dashboard : Route()

    // Analytics Screens
    @Serializable
    data object Analytics : Route()

    // Friends Screens
    @Serializable
    data object FriendsList : Route()

    // Use data classes for things with parameters
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
    data object Settings : Route()
}