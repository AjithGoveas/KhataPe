package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.repository.FriendRepository
import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import javax.inject.Inject

class DeleteAllUseCase @Inject constructor(
    private val friendRepository: FriendRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke() {
//        transactionRepository.deleteAll()
        friendRepository.deleteAll()
    }
}
