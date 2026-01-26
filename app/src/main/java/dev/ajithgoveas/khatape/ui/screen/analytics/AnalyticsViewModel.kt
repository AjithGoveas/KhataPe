package dev.ajithgoveas.khatape.ui.screen.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ajithgoveas.khatape.domain.usecase.analytics.GetBalanceOverviewUseCase
import dev.ajithgoveas.khatape.domain.usecase.analytics.GetDistributionUseCase
import dev.ajithgoveas.khatape.domain.usecase.analytics.GetNetWorthTrendUseCase
import dev.ajithgoveas.khatape.domain.usecase.analytics.TrendResult
import dev.ajithgoveas.khatape.ui.components.charts.donutChart.model.PieChartData
import dev.ajithgoveas.khatape.ui.components.charts.lineChart.model.LineParameters
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// UI State
data class AnalyticsUiState(
    val overview: BalanceOverview = BalanceOverview(0.0, 0.0, 0.0),
    val netWorthTrend: LineParameters? = null,
    val netWorthLabels: List<String> = emptyList(),
    val payableDistribution: List<PieChartData> = emptyList(),
    val receivableDistribution: List<PieChartData> = emptyList(),
    val isLoading: Boolean = true
)

data class BalanceOverview(
    val totalReceivable: Double,
    val totalPayable: Double,
    val netBalance: Double
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getNetWorthTrendUseCase: GetNetWorthTrendUseCase,
    private val getBalanceOverviewUseCase: GetBalanceOverviewUseCase,
    private val getDistributionUseCase: GetDistributionUseCase
) : ViewModel() {

    sealed class AnalyticsEffect {
        data class ShowSnackbar(val message: String) : AnalyticsEffect()
    }

    private val _effect = Channel<AnalyticsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val uiState: StateFlow<AnalyticsUiState> = combine(
        getBalanceOverviewUseCase(),
        getNetWorthTrendUseCase(),
        getDistributionUseCase() // This now returns both distributions
    ) { overview, trendResult, distributionResult ->

        // 1. Proactive Side Effects
        // Debt Risk check
        val topDebt = distributionResult.payableDistribution.firstOrNull()
        if (topDebt != null && overview.totalPayable > 0 && (topDebt.data / overview.totalPayable) > 0.6) {
            _effect.trySend(AnalyticsEffect.ShowSnackbar("Concentration Risk: High debt owed to ${topDebt.partName}"))
        }

        // Credit Opportunity check (If a lot of money is stuck with one person)
        val topCredit = distributionResult.receivableDistribution.firstOrNull()
        if (topCredit != null && overview.totalReceivable > 5000) {
            _effect.trySend(AnalyticsEffect.ShowSnackbar("Friendly Reminder: ${topCredit.partName} owes you a significant amount."))
        }

        // 2. Trend Mapping
        val (trendLine, trendLabels) = when (trendResult) {
            is TrendResult.Success -> trendResult.lineParameters to trendResult.labels
            is TrendResult.Empty -> null to emptyList()
        }

        AnalyticsUiState(
            overview = overview,
            netWorthTrend = trendLine,
            netWorthLabels = trendLabels,
            payableDistribution = distributionResult.payableDistribution,
            receivableDistribution = distributionResult.receivableDistribution,
            isLoading = false
        )
    }
        .distinctUntilChanged()
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )
}