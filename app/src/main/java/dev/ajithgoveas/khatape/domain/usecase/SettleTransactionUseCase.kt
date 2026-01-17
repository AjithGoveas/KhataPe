package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import javax.inject.Inject

class SettleTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: Long): Int {
        return transactionRepository.settleTransaction(transactionId = transactionId)
    }
}