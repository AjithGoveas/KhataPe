package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    /**
     * Deletes a transaction from the database.
     * This is a one-off operation, so it's a suspend function, not a Flow.
     * @param transactionId The ID of the transaction to delete.
     */
    suspend operator fun invoke(transactionId: Long) {
        transactionRepository.deleteTransaction(transactionId)
    }
}