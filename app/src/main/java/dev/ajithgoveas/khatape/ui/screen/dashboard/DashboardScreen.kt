package dev.ajithgoveas.khatape.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.ui.components.AnalyticsCard
import dev.ajithgoveas.khatape.ui.components.AvatarIcon

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val layoutDirection = LocalLayoutDirection.current
    val summaries by viewModel.summaries.collectAsState()

    val totalCredit = summaries.sumOf { it.totalCredit }
    val totalDebit = summaries.sumOf { it.totalDebit }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            DashboardHeader()

            AnalyticsCard(
                debitAmount = totalDebit.toInt(),
                creditAmount = totalCredit.toInt()
            )

            SectionTitle("Recent Activity")

            RecentActivityList(
                summaries = summaries,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun DashboardHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "🏠 Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Track expense with trust.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun RecentActivityList(
    summaries: List<FriendSummary>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (summaries.isEmpty()) {
            item {
                Text(
                    text = "No recent transactions yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(summaries) { summary ->
                RecentCard(summary = summary)
            }
        }
    }
}

@Composable
private fun RecentCard(summary: FriendSummary) {
    val statusText = when {
        summary.totalDebit > 0 && summary.totalCredit == 0.0 ->
            "You owe ₹${summary.totalDebit.toInt()}"

        summary.totalCredit > 0 && summary.totalDebit == 0.0 ->
            "${summary.name} owes you ₹${summary.totalCredit.toInt()}"

        summary.totalCredit > 0 && summary.totalDebit > 0 ->
            "You owe ₹${summary.totalDebit.toInt()} | ${summary.name} owes you ₹${summary.totalCredit.toInt()}"

        else -> "${summary.name} is settled"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarIcon(name = summary.name)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = summary.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
