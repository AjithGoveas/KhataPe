package dev.ajithgoveas.khatape.domain.usecase.analytics

import androidx.compose.ui.graphics.Color
import dev.ajithgoveas.khatape.data.repository.FriendRepository
import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import dev.ajithgoveas.khatape.ui.components.charts.donutChart.model.PieChartData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.abs

data class DistributionResult(
    val payableDistribution: List<PieChartData>,
    val receivableDistribution: List<PieChartData>
)

// GetDistributionUseCase.kt
class GetDistributionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val friendRepository: FriendRepository
) {
    operator fun invoke(): Flow<DistributionResult> {
        return combine(
            transactionRepository.getAllTransactions(),
            friendRepository.getAllFriends()
        ) { transactions, friends ->
            val friendNames = friends.associate { it.id to it.name }

            // 1. Group by friend and calculate Net Balance
            val netBalances = transactions.groupBy { it.friendId }
                .mapValues { (_, trans) ->
                    trans.sumOf {
                        if (it.direction == TransactionDirection.CREDIT) it.amount else -it.amount
                    }
                }

            // 2. Separate into Payables (Negative) and Receivables (Positive)
            val payablesRaw = netBalances.filter { it.value < 0 }
                .map { (id, bal) -> (friendNames[id] ?: "Unknown") to abs(bal) }
                .sortedByDescending { it.second }

            val receivablesRaw = netBalances.filter { it.value > 0 }
                .map { (id, bal) -> (friendNames[id] ?: "Unknown") to bal }
                .sortedByDescending { it.second }

            // 3. Define the UI palette
            val colors = listOf(
                Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
                Color(0xFFFFD54F), Color(0xFFBA68C8)
            )

            DistributionResult(
                payableDistribution = formatToPieData(payablesRaw, colors),
                receivableDistribution = formatToPieData(receivablesRaw, colors)
            )
        }
    }

    // Helper to keep the code DRY (Don't Repeat Yourself)
    private fun formatToPieData(
        rawList: List<Pair<String, Double>>,
        colors: List<Color>,
        limit: Int = 4
    ): List<PieChartData> {
        if (rawList.isEmpty()) return emptyList()

        return if (rawList.size <= limit + 1) {
            rawList.mapIndexed { i, p ->
                PieChartData(p.second, colors[i % colors.size], p.first)
            }
        } else {
            val topSlices = rawList.take(limit).mapIndexed { i, p ->
                PieChartData(p.second, colors[i % colors.size], p.first)
            }
            val otherSum = rawList.drop(limit).sumOf { it.second }
            topSlices + PieChartData(otherSum, Color.Gray, "Others")
        }
    }
}