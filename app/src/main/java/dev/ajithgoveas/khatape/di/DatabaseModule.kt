package dev.ajithgoveas.khatape.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ajithgoveas.khatape.data.local.AppDatabase
import dev.ajithgoveas.khatape.data.local.dao.FriendDao
import dev.ajithgoveas.khatape.data.local.dao.FriendSummaryDao
import dev.ajithgoveas.khatape.data.local.dao.TransactionDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "khata_pe").build()

    @Provides
    fun provideFriendDao(db: AppDatabase): FriendDao = db.friendDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideFriendSummaryDao(db: AppDatabase): FriendSummaryDao = db.friendSummaryDao()
}
