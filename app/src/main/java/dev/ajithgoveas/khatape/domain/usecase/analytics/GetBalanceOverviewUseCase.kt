package dev.ajithgoveas.khatape.domain.usecase.analytics

import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import dev.ajithgoveas.khatape.ui.screen.analytics.BalanceOverview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetBalanceOverviewUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<BalanceOverview> =
        repository.getAllTransactions().map { transactions ->
            // Single-pass calculation: O(n)
            val overview = transactions.fold(BalanceOverview(0.0, 0.0, 0.0)) { acc, trans ->
                when (trans.direction) {
                    TransactionDirection.CREDIT -> acc.copy(
                        totalReceivable = acc.totalReceivable + trans.amount,
                        netBalance = acc.netBalance + trans.amount
                    )

                    TransactionDirection.DEBIT -> acc.copy(
                        totalPayable = acc.totalPayable + trans.amount,
                        netBalance = acc.netBalance - trans.amount
                    )
                }
            }
            overview
        }
}