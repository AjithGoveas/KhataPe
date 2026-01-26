package dev.ajithgoveas.khatape.ui.screen.analytics

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajithgoveas.khatape.ui.components.AnalyticsCard
import dev.ajithgoveas.khatape.ui.components.KhataPeAppTopBar
import dev.ajithgoveas.khatape.ui.components.charts.baseComponents.model.LegendPosition
import dev.ajithgoveas.khatape.ui.components.charts.donutChart.PieChart
import dev.ajithgoveas.khatape.ui.components.charts.lineChart.LineChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AnalyticsViewModel.AnalyticsEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = effect.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KhataPeAppTopBar(
                title = "Analytics",
                subtitle = "Analyze your cash flow",
                emoji = "📈",
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Crossfade(targetState = uiState.isLoading, label = "content_fade") { isLoading ->
            when {
                isLoading -> LoadingView()
                uiState.netWorthTrend == null && uiState.payableDistribution.isEmpty() -> EmptyAnalyticsView()
                else -> AnalyticsContent(uiState, innerPadding)
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    uiState: AnalyticsUiState,
    innerPadding: PaddingValues
) {
    val formattedNetBalance = remember(uiState.overview.netBalance) {
        "₹${"%,.2f".format(uiState.overview.netBalance)}"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 16.dp,
            bottom = 40.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Health Summary Card
        item(key = "health_card") {
            AnalyticsCard(
                debitAmount = uiState.overview.totalPayable.toInt(),
                creditAmount = uiState.overview.totalReceivable.toInt()
            )
        }

        // 2. Net Worth Line Chart
        uiState.netWorthTrend?.let { trend ->
            item(key = "trend_chart") {
                val accentColor = if (uiState.overview.netBalance < 0)
                    MaterialTheme.colorScheme.error else Color(0xFF4CAF50)

                val filteredLabels = remember(uiState.netWorthLabels) {
                    var lastSeen = ""
                    uiState.netWorthLabels.map { date ->
                        if (date != lastSeen) {
                            lastSeen = date; date
                        } else ""
                    }
                }

                ChartContainer(
                    title = "Net Worth Trend",
                    amount = formattedNetBalance,
                    amountColor = accentColor
                ) {
                    LineChart(
                        modifier = Modifier.fillMaxSize(),
                        linesParameters = listOf(trend.copy(lineColor = accentColor)),
                        isGrid = true,
                        gridColor = MaterialTheme.colorScheme.outlineVariant,
                        xAxisData = filteredLabels,
                        animateChart = true,
                        yAxisStyle = MaterialTheme.typography.labelSmall,
                        xAxisStyle = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // 3. Debt/Credit Distribution Pie Chart
        item(key = "distribution_section") {
            DistributionSection(uiState = uiState)
        }
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}

@Composable
fun EmptyAnalyticsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // A large, soft-colored icon or emoji
        Surface(
            modifier = Modifier.size(120.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "📊", fontSize = 48.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Analytics Yet",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start adding transactions with your friends to see your financial insights here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChartContainer(
    title: String,
    amount: String,
    modifier: Modifier = Modifier,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
    headerAction: @Composable (() -> Unit)? = null,
    chartContent: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = amountColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            headerAction?.invoke()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp) // Giving slightly more room for Pedigree lines
            ) {
                chartContent()
            }
        }
    }
}

@Composable
private fun DistributionSection(uiState: AnalyticsUiState) {
    // 1. Extract distributions for clarity
    val payable = uiState.payableDistribution
    val receivable = uiState.receivableDistribution

    // 2. The "Nothing to show" check
    // Using isEmpty() is safer and more readable than !isNotEmpty()
    if (payable.isEmpty() && receivable.isEmpty()) return

    val hasDebts = payable.isNotEmpty()
    val hasCredits = receivable.isNotEmpty()

    // 3. Robust Tab State
    var selectedTab by remember(hasDebts, hasCredits) {
        mutableIntStateOf(if (hasDebts) 0 else 1)
    }

    val currentData by remember(selectedTab, uiState) {
        derivedStateOf {
            if (selectedTab == 0) payable else receivable
        }
    }

    val displayAmount = remember(selectedTab, uiState.overview) {
        if (selectedTab == 0) uiState.overview.totalPayable else uiState.overview.totalReceivable
    }

    ChartContainer(
        title = "Distribution",
        amount = "₹${"%,.0f".format(displayAmount)}",
        headerAction = {
            // Only show switch if user has both types of data
            if (hasDebts && hasCredits) {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Debts", style = MaterialTheme.typography.labelLarge) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Credits", style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }
        }
    ) {
        PieChart(
            modifier = Modifier.fillMaxSize(),
            pieChartData = currentData,
            legendPosition = LegendPosition.BOTTOM,
            descriptionStyle = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}