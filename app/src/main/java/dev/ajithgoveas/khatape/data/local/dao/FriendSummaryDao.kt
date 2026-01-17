package dev.ajithgoveas.khatape.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendSummaryDao {

    @Query("""
        SELECT f.id AS friendId, f.name,
        COALESCE(SUM(CASE WHEN t.direction = 'CREDIT' AND t.isSettled = 0 THEN t.amount ELSE 0 END), 0) AS totalCredit,
        COALESCE(SUM(CASE WHEN t.direction = 'DEBIT' AND t.isSettled = 0 THEN t.amount ELSE 0 END), 0) AS totalDebit
        FROM friends f
        LEFT JOIN transactions t ON f.id = t.friendId
        GROUP BY f.id
        ORDER BY f.createdAt DESC
    """)
    fun getFriendSummaries(): Flow<List<FriendSummary>>

    @Query("""
        SELECT f.id AS friendId, f.name,
        COALESCE(SUM(CASE WHEN t.direction = 'CREDIT' AND t.isSettled = 0 THEN t.amount ELSE 0 END), 0) AS totalCredit,
        COALESCE(SUM(CASE WHEN t.direction = 'DEBIT' AND t.isSettled = 0 THEN t.amount ELSE 0 END), 0) AS totalDebit
        FROM friends f
        LEFT JOIN transactions t ON f.id = t.friendId
        WHERE f.id = :friendId
        GROUP BY f.id
        LIMIT 1
    """)
    fun getFriendSummary(friendId: Long): Flow<FriendSummary?>
}
