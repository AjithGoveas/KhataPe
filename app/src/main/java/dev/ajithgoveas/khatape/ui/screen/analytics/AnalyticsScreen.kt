package dev.ajithgoveas.khatape.ui.screen.analytics

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 16.dp,
            bottom = 32.dp, // Extra bottom padding for snackbar clearance
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Financial Health Card
        item {
            AnalyticsCard(
                debitAmount = uiState.overview.totalPayable.toInt(),
                creditAmount = uiState.overview.totalReceivable.toInt()
            )
        }

        // 2. Net Worth Trend
        uiState.netWorthTrend?.let { trend ->
            item {
                val isNegative = uiState.overview.netBalance < 0
                val accentColor =
                    if (isNegative) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)

                ChartContainer(
                    title = "Net Worth Trend",
                    amount = "₹${"%,.2f".format(uiState.overview.netBalance)}"
                ) {
                    LineChart(
                        modifier = Modifier.fillMaxSize(),
                        linesParameters = listOf(trend.copy(lineColor = accentColor)),
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

        // 3. Weekly Velocity (Bar Chart)
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
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat design is more modern
    ) {
        Column(
            modifier = Modifier.padding(20.dp), // Slightly tighter padding for better content space
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                headerAction?.invoke()
            }

            // Chart Box with a subtle inner background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp) // Increased height slightly for better label visibility
                    .padding(top = 8.dp)
            ) {
                chartContent()
            }
        }
    }
}

