package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        transactionId: Long,
        amount: Double,
        direction: TransactionDirection,
        description: String,
        dueDate: Long?,
        timestamp: Long
    ) {
        // 1. Fetch the current state of the transaction from the DB
        val existingTxn = transactionRepository.getTransactionById(transactionId).firstOrNull()
            ?: return // Handle error if txn doesn't exist

        // 2. Create an updated version of the Entity
        // We use .copy() or manually map to preserve friendId and isSettled status
        val updatedEntity = existingTxn.copy(
            amount = amount,
            direction = direction,
            description = description,
            dueDate = dueDate,
            timestamp = timestamp
        )

        // 3. Save the full entity
        transactionRepository.updateTransaction(updatedEntity)
    }
}