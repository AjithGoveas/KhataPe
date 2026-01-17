package dev.ajithgoveas.khatape.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.ajithgoveas.khatape.data.local.converter.InstantConverter
import dev.ajithgoveas.khatape.data.local.converter.TransactionDirectionConverter
import dev.ajithgoveas.khatape.data.local.dao.FriendDao
import dev.ajithgoveas.khatape.data.local.dao.FriendSummaryDao
import dev.ajithgoveas.khatape.data.local.dao.TransactionDao
import dev.ajithgoveas.khatape.data.local.entity.FriendEntity
import dev.ajithgoveas.khatape.data.local.entity.TransactionEntity

@Database(
    entities = [FriendEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(InstantConverter::class, TransactionDirectionConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun transactionDao(): TransactionDao
    abstract fun friendSummaryDao(): FriendSummaryDao
}