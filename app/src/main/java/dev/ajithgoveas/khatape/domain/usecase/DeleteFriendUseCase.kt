package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.repository.FriendRepository
import dev.ajithgoveas.khatape.domain.mappers.toEntity
import dev.ajithgoveas.khatape.domain.model.Friend
import javax.inject.Inject

class DeleteFriendUseCase @Inject constructor(
    private val friendRepository: FriendRepository
) {
    suspend operator fun invoke(friend: Friend) {
        return friendRepository.deleteFriend(friend = friend.toEntity())
    }
}