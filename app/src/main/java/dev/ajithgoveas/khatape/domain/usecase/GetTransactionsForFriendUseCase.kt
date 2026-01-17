package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import dev.ajithgoveas.khatape.domain.mappers.toDomain
import dev.ajithgoveas.khatape.domain.model.Transaction
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTransactionsForFriendUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(friendId: Long): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByFriendId(friendId)
            .map { transactionEntities ->
                transactionEntities.map { it.toDomain() }
            }
    }
}