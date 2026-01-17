package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        transactionId: Long,
        amount: Double,
        direction: TransactionDirection,
        description: String,
        timestamp: Long
    ) {
        transactionRepository.updateTransaction(
            transactionId, amount, direction, description, timestamp
        )
    }
}