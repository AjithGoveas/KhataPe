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
import dev.ajithgoveas.khatape.ui.components.charts.base.model.LegendPosition
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

    // Listen for Side Effects
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Crossfade(targetState = uiState.isLoading, label = "loading_fade") { isLoading ->
            when {
                isLoading -> LoadingView()
                // Check if both data sets are essentially empty
                uiState.netWorthTrend == null -> {
                    EmptyAnalyticsView()
                }

                else -> AnalyticsContent(uiState, innerPadding)
            }
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
private fun AnalyticsContent(
    uiState: AnalyticsUiState,
    innerPadding: PaddingValues
) {
    // 1. Pre-format display values so they don't re-calculate during scroll
    val formattedNetBalance = remember(uiState.overview.netBalance) {
        "₹${"%,.2f".format(uiState.overview.netBalance)}"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 16.dp,
            bottom = 32.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Use keys for LazyColumn items to help Compose optimize list updates
        item(key = "health_card") {
            AnalyticsCard(
                debitAmount = uiState.overview.totalPayable.toInt(),
                creditAmount = uiState.overview.totalReceivable.toInt()
            )
        }

        uiState.netWorthTrend?.let { trend ->
            item(key = "trend_chart") {
                val accentColor = if (uiState.overview.netBalance < 0)
                    MaterialTheme.colorScheme.error else Color(0xFF4CAF50)

                // Wrap in remember to keep the line parameters stable
                val lineParams = remember(trend, accentColor) {
                    listOf(trend.copy(lineColor = accentColor))
                }

                ChartContainer(
                    title = "Net Worth Trend",
                    amount = formattedNetBalance
                ) {
                    LineChart(
                        modifier = Modifier.fillMaxSize(),
                        linesParameters = lineParams,
                        isGrid = true,
                        gridColor = MaterialTheme.colorScheme.outlineVariant,
                        xAxisData = uiState.netWorthLabels,
                        animateChart = true,
                        yAxisStyle = MaterialTheme.typography.labelSmall,
                        xAxisStyle = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        item(key = "distribution_section") {
            DistributionSection(uiState = uiState)
        }
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
    headerAction: @Composable (() -> Unit)? = null,
    chartContent: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Tighter vertical rhythm
        ) {
            // Title and Amount Row
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp // Modern tight tracking
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tabs/Actions placed below the header line
            headerAction?.let {
                Box(modifier = Modifier.fillMaxWidth()) {
                    it()
                }
            }

            // Chart Box - We give Pie charts more room by adjusting height based on content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp) // Increased height to prevent legend squishing
                    .padding(top = 8.dp)
            ) {
                chartContent()
            }
        }
    }
}

@Composable
private fun DistributionSection(uiState: AnalyticsUiState) {
    // 1. Determine what we actually have to show
    val hasDebts = uiState.payableDistribution.isNotEmpty()
    val hasCredits = uiState.receivableDistribution.isNotEmpty()

    // If absolutely nothing, don't even render the Container
    if (!hasDebts && !hasCredits) return

    // 2. Manage which tab is selected based on availability
    // Default to Debts (0) if available, otherwise Credits (1)
    var selectedTab by remember(hasDebts, hasCredits) {
        mutableIntStateOf(if (hasDebts) 0 else 1)
    }

    val currentData by remember(selectedTab, uiState) {
        derivedStateOf {
            if (selectedTab == 0) uiState.payableDistribution else uiState.receivableDistribution
        }
    }
    val currentTitle = if (selectedTab == 0) "Who you owe" else "Who owes you"

    ChartContainer(
        title = "Friend Distribution",
        amount = currentTitle,
        headerAction = {
            // Only show the toggle if BOTH data sets exist
            if (hasDebts && hasCredits) {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    modifier = Modifier.fillMaxWidth(0.8f)
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
        // PieChart gets the full Box space
        PieChart(
            modifier = Modifier.fillMaxSize(),
            pieChartData = currentData,
            legendPosition = LegendPosition.BOTTOM, // Bottom is usually safer for wide legends
            descriptionStyle = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}