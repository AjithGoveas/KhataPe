package dev.ajithgoveas.khatape.data.repository

import dev.ajithgoveas.khatape.data.local.dao.TransactionDao
import dev.ajithgoveas.khatape.data.local.entity.TransactionEntity
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    // CREATE: Adds a new transaction.
    suspend fun addTransaction(transaction: TransactionEntity): Long =
        transactionDao.insert(transaction)

    // READ: Gets a single transaction by its ID.
    fun getTransactionById(transactionId: Long): Flow<TransactionEntity?> =
        transactionDao.getById(id = transactionId)

    // READ: Gets all transactions for a specific friend.
    fun getTransactionsByFriendId(friendId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getByFriend(friendId = friendId)

    // READ: Gets all transactions.
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    // READ: Gets all due transactions for a specific friend.
    suspend fun getUpcomingTransactionsWithFriend(daysAhead: Int): Flow<List<TransactionEntity>> {
        val now = System.currentTimeMillis()
        val end = now + TimeUnit.DAYS.toMillis(daysAhead.toLong())
        return transactionDao.getTransactionsWithFriendByDueDateRange(now, end)
    }

    // UPDATE: Marks a transaction as settled.
    suspend fun settleTransaction(transactionId: Long): Int =
        transactionDao.markSettled(id = transactionId)

    // Inside TransactionRepository.kt
    suspend fun updateTransaction(transaction: TransactionEntity): Int {
        return transactionDao.update(transaction) // Uses the @Update annotation
    }

    // DELETE: Deletes a transaction by its ID.
    suspend fun deleteTransaction(transactionId: Long): Int =
        transactionDao.deleteById(id = transactionId)

    // READ: Gets the total credit amount for a friend.
    fun getTotalCredit(friendId: Long): Flow<Double> =
        transactionDao.getTotalByDirection(
            friendId = friendId,
            direction = TransactionDirection.CREDIT
        ).map { total -> total ?: 0.0 }

    // READ: Gets the total debit amount for a friend.
    fun getTotalDebit(friendId: Long): Flow<Double> =
        transactionDao.getTotalByDirection(
            friendId = friendId,
            direction = TransactionDirection.DEBIT
        ).map { total -> total ?: 0.0 }

    suspend fun deleteAll() {
        transactionDao.deleteAll()
    }
}