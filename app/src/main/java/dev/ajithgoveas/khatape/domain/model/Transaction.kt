package dev.ajithgoveas.khatape.domain.model

data class Transaction(
    val id: Long,
    val friendId: Long,
    val amount: Double,
    val direction: TransactionDirection,
    val description: String,
    val isSettled: Boolean,
    val timestamp: Long
)

