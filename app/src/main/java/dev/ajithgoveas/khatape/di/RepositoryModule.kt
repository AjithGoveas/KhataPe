package dev.ajithgoveas.khatape.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ajithgoveas.khatape.data.local.dao.FriendDao
import dev.ajithgoveas.khatape.data.local.dao.FriendSummaryDao
import dev.ajithgoveas.khatape.data.local.dao.TransactionDao
import dev.ajithgoveas.khatape.data.repository.FriendRepository
import dev.ajithgoveas.khatape.data.repository.FriendSummaryRepository
import dev.ajithgoveas.khatape.data.repository.TransactionRepository

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideFriendRepository(friendDao: FriendDao): FriendRepository =
        FriendRepository(friendDao)

    @Provides
    fun provideTransactionRepository(transactionDao: TransactionDao): TransactionRepository =
        TransactionRepository(transactionDao)

    @Provides
    fun provideFriendSummaryRepository(summaryDao: FriendSummaryDao): FriendSummaryRepository =
        FriendSummaryRepository(summaryDao)
}
