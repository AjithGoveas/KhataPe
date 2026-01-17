package dev.ajithgoveas.khatape.domain.usecase

import dev.ajithgoveas.khatape.data.repository.FriendRepository
import javax.inject.Inject

class CreateFriendUseCase @Inject constructor(
    private val friendRepository: FriendRepository
) {
    suspend operator fun invoke(name: String): Long {
        return friendRepository.createFriend(name)
    }
}