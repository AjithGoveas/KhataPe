package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.repository.FriendSummaryRepository
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetFriendSummariesUseCase @Inject constructor(
    private val summaryRepository: FriendSummaryRepository
) {
    operator fun invoke(): Flow<List<FriendSummary>> = summaryRepository.getFriendSummaries()
}