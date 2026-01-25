package dev.ajithgoveas.khatape.domain.usecase.analytics

import androidx.compose.ui.graphics.Color
import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import dev.ajithgoveas.khatape.ui.components.charts.lineChart.model.LineParameters
import dev.ajithgoveas.khatape.ui.components.charts.lineChart.model.LineType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.absoluteValue

class GetNetWorthTrendUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(): Flow<TrendResult> {
        return transactionRepository.getAllTransactions().map { transactions ->
            if (transactions.isEmpty()) return@map TrendResult.Empty

            val sorted = transactions.sortedBy { it.timestamp }
            var runningBalance = 0.0

            val allPoints = sorted.map { trans ->
                if (trans.direction == TransactionDirection.CREDIT) {
                    runningBalance += trans.amount
                } else {
                    runningBalance -= trans.amount
                }

                val dateLabel = SimpleDateFormat("dd MMM", Locale.getDefault())
                    .format(Date(trans.timestamp))

                ChartPoint(value = runningBalance, label = dateLabel)
            }

            // 1. Take last 10 points
            // 2. Map to a new list where negative values are coerced to 0.0
//            val recentPoints = allPoints.takeLast(10).map { point ->
//                point.copy(value = point.value.coerceAtLeast(0.0))
//            }
            val recentPoints = allPoints.takeLast(10)

            TrendResult.Success(
                lineParameters = LineParameters(
                    label = "Net Balance",
                    data = recentPoints.map { it.value.absoluteValue }, // Now this is a List<Double>
                    lineColor = Color(0xFF6C3428),
                    lineType = LineType.CURVED_LINE,
                    lineShadow = true
                ),
                labels = recentPoints.map { it.label }
            )
        }
    }
}

// Data models to keep the UI and UseCase in sync
data class ChartPoint(val value: Double, val label: String)

sealed class TrendResult {
    object Empty : TrendResult()
    data class Success(
        val lineParameters: LineParameters,
        val labels: List<String>
    ) : TrendResult()
}