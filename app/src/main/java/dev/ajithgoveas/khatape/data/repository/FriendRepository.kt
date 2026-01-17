package dev.ajithgoveas.khatape.data.repository

import dev.ajithgoveas.khatape.data.local.dao.FriendDao
import dev.ajithgoveas.khatape.data.local.entity.FriendEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class FriendRepository @Inject constructor(
    private val friendDao: FriendDao
) {
    // Corrected to be a suspend function and return the Long ID,
    // as FriendDao.insert() is a suspend function.
    suspend fun createFriend(name: String): Long {
        val friend = FriendEntity(name = name)
        return friendDao.insert(friend)
    }

    // Correctly returns a Flow, as FriendDao.getById() provides a continuous stream of data.
    fun getFriendById(id: Long): Flow<FriendEntity?> = friendDao.getById(id = id)

    // Correctly returns a Flow, as FriendDao.getAll() provides a continuous stream of data.
    fun getAllFriends(): Flow<List<FriendEntity>> = friendDao.getAll()

    // Corrected to be a suspend function, as FriendDao.delete() is a one-shot operation.
    suspend fun deleteFriend(friend: FriendEntity) {
        friendDao.delete(friend = friend)
    }

    suspend fun deleteAll() {
        friendDao.deleteAll()
    }
}