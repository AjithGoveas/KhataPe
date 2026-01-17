package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.repository.FriendSummaryRepository
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetFriendSummaryByIdUseCase @Inject constructor(
    private val summaryRepository: FriendSummaryRepository
) {
    // ⚠️ Removed the 'suspend' keyword and correctly return a Flow.
    operator fun invoke(friendId: Long): Flow<FriendSummary?> {
        return summaryRepository.getFriendSummaryById(friendId)
    }
}