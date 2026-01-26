package dev.ajithgoveas.khatape.domain.usecase.analytics

import androidx.compose.ui.graphics.Color
import dev.ajithgoveas.khatape.data.repository.TransactionRepository
import dev.ajithgoveas.khatape.domain.model.TransactionDirection
import dev.ajithgoveas.khatape.ui.components.charts.barChart.model.BarParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

// Update GetWeeklyFlowUseCase.kt
data class WeeklyFlowResult(
    val barParameters: List<BarParameters>,
    val labels: List<String>
)

// GetWeeklyFlowUseCase.kt
class GetWeeklyFlowUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(): Flow<WeeklyFlowResult> {
        return transactionRepository.getAllTransactions().map { transactions ->
            val zoneId = ZoneId.systemDefault()
            val today = LocalDate.now(zoneId)

            // Create a list of the last 7 dates
            val lastSevenDays = (0..6).map { today.minusDays(it.toLong()) }.reversed()

            val inboundData = mutableListOf<Double>()
            val outboundData = mutableListOf<Double>()

            lastSevenDays.forEach { date ->
                // Filter transactions that fall on this specific date
                val dayTransactions = transactions.filter {
                    val transactionDate = Instant.ofEpochMilli(it.timestamp)
                        .atZone(zoneId)
                        .toLocalDate()
                    transactionDate == date
                }

                inboundData.add(dayTransactions.filter { it.direction == TransactionDirection.CREDIT }
                    .sumOf { it.amount })
                outboundData.add(dayTransactions.filter { it.direction == TransactionDirection.DEBIT }
                    .sumOf { it.amount })
            }

            WeeklyFlowResult(
                barParameters = listOf(
                    BarParameters("To Get", inboundData, Color(0xFF81BE88)),
                    BarParameters("To Give", outboundData, Color(0xFFFF7F50))
                ),
                labels = lastSevenDays.map {
                    it.dayOfWeek.getDisplayName(
                        TextStyle.SHORT,
                        Locale.getDefault()
                    )
                }
            )
        }
    }
}