package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import dev.ajithgoveas.khatape.domain.mappers.toDomain
import dev.ajithgoveas.khatape.domain.model.Transaction
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTransactionByIdUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    /**
     * Retrieves a single transaction by its ID as a cold flow.
     * @param transactionId The ID of the transaction to retrieve.
     * @return A Flow that emits the Transaction or null if not found.
     */
    operator fun invoke(transactionId: Long): Flow<Transaction?> {
        return transactionRepository.getTransactionById(transactionId)
            .map { it?.toDomain() }
    }
}