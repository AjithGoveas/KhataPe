package dev.ajithgoveas.khatape.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.ajithgoveas.khatape.data.local.entity.TransactionEntity
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE friendId = :friendId ORDER BY timestamp DESC")
    fun getByFriend(friendId: Long): Flow<List<TransactionEntity>>

    @Query("UPDATE transactions SET isSettled = 1 WHERE id = :id")
    suspend fun markSettled(id: Long): Int

    @Query(
        """
        UPDATE transactions 
        SET amount = :amount, direction = :direction, description = :description, timestamp = :timestamp 
        WHERE id = :transactionId
    """
    )
    suspend fun updateTransactions(
        transactionId: Long,
        amount: Double,
        direction: TransactionDirection,
        description: String,
        timestamp: Long
    ): Int

    @Query(
        """
        UPDATE transactions 
        SET amount = :amount, direction = :direction, description = :description, dueDate = :dueDate, timestamp = :timestamp 
        WHERE id = :transactionId
    """
    )
    suspend fun updateTransactionsWithDueDate(
        transactionId: Long,
        amount: Double,
        direction: TransactionDirection,
        description: String,
        dueDate: Long,
        timestamp: Long
    ): Int

    @Transaction
    @Query("SELECT * FROM transactions WHERE dueDate IS NOT NULL AND dueDate BETWEEN :start AND :end")
    fun getTransactionsWithFriendByDueDateRange(
        start: Long,
        end: Long
    ): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query(
        """
        SELECT SUM(amount) FROM transactions 
        WHERE friendId = :friendId AND direction = :direction AND isSettled = 0
    """
    )
    fun getTotalByDirection(friendId: Long, direction: TransactionDirection): Flow<Double?>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}