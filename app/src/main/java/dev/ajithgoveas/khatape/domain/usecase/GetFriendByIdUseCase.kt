package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.local.entity.FriendEntity
import dev.ajithgoveas.khatape.data.repository.FriendRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use case to fetch a friend by their ID.
 * Returns a domain-level Friend object.
 */
class GetFriendByIdUseCase @Inject constructor(
    private val friendRepository: FriendRepository
) {
    operator fun invoke(friendId: Long): Flow<FriendEntity?> =
        friendRepository.getFriendById(friendId)
}