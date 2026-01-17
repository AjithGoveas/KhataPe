package dev.ajithgoveas.khatape.data.repository

import dev.ajithgoveas.khatape.data.local.dao.FriendSummaryDao
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class FriendSummaryRepository @Inject constructor(
    private val friendSummaryDao: FriendSummaryDao
) {

    fun getFriendSummaries(): Flow<List<FriendSummary>> =
        friendSummaryDao.getFriendSummaries()

    fun getFriendSummaryById(friendId: Long): Flow<FriendSummary?> =
        friendSummaryDao.getFriendSummary(friendId)
}
