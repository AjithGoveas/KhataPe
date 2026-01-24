package dev.ajithgoveas.khatape.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.ui.components.AnalyticsCard
import dev.ajithgoveas.khatape.ui.components.AvatarIcon
import dev.ajithgoveas.khatape.ui.components.KhataPeAppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    // 1. Define Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val summaries by viewModel.summaries.collectAsState()

    val totalCredit = summaries.sumOf { it.totalCredit }
    val totalDebit = summaries.sumOf { it.totalDebit }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), // 2. Connect to Scroll
        topBar = {
            KhataPeAppTopBar(
                title = "Dashboard",
                subtitle = "Track expense with trust.",
                emoji = "🏠",
                scrollBehavior = scrollBehavior // 3. Pass it to the bar
            )
        }
    ) { innerPadding ->
        // Use LazyColumn for the entire screen to leverage nested scrolling
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                AnalyticsCard(
                    debitAmount = totalDebit.toInt(),
                    creditAmount = totalCredit.toInt()
                )
            }

            item { SectionTitle("Recent Activity") }

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
}

/**
 * Old header component (now replaced by AppTopBar).
 * Keeping it commented for reference.
 */
//@Composable
//private fun DashboardHeader() {
//    Column(
//        verticalArrangement = Arrangement.spacedBy(4.dp)
//    ) {
//        Text(
//            "🏠 Dashboard",
//            style = MaterialTheme.typography.headlineMedium,
//            color = MaterialTheme.colorScheme.primary
//        )
//        Text(
//            "Track expense with trust.",
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        Spacer(modifier = Modifier.height(4.dp))
//        HorizontalDivider()
//    }
//}

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