package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.local.entity.TransactionEntity
import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        friendId: Long,
        amount: Double,
        direction: TransactionDirection,
        description: String,
        timeStamp: Long
    ): Long { // Corrected the return type to Long
        val transaction = TransactionEntity(
            friendId = friendId,
            amount = amount,
            direction = direction,
            description = description,
            timestamp = timeStamp
        )
        return transactionRepository.addTransaction(transaction)
    }
}