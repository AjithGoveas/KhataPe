package dev.ajithgoveas.khatape.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.ui.components.AnalyticsCard
import dev.ajithgoveas.khatape.ui.components.AvatarIcon

@Composable
fun RecentCard(summary: FriendSummary) {
    val statusText = buildString {
        when {
            summary.totalDebit > 0 && summary.totalCredit == 0.0 -> {
                append("You owe ₹${summary.totalDebit.toInt()}") // DEBIT
            }

            summary.totalCredit > 0 && summary.totalDebit == 0.0 -> {
                append("${summary.name} owes you ₹${summary.totalCredit.toInt()}") // CREDIT
            }

            summary.totalCredit > 0 && summary.totalDebit > 0 -> {
                append("You owe ₹${summary.totalDebit.toInt()} | ${summary.name} owes you ₹${summary.totalCredit.toInt()}")
            }

            else -> {
                append("${summary.name} is settled")
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
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

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val summaries by viewModel.summaries.collectAsState()

    val totalCredit = summaries.sumOf { it.totalCredit }
    val totalDebit = summaries.sumOf { it.totalDebit }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = "Dashboard Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(32.dp)
                )
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item {
            AnalyticsCard(
                debitAmount = totalDebit.toInt(),
                creditAmount = totalCredit.toInt()
            )
        }

        item {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

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