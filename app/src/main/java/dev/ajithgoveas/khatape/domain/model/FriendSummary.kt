package dev.ajithgoveas.khatape.domain.model

data class FriendSummary(
    val friendId: Long,
    val name: String,
    val totalCredit: Double,
    val totalDebit: Double
)
