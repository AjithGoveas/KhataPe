package dev.ajithgoveas.khatape.ui.screen.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.ajithgoveas.khatape.domain.model.FriendSummary
import dev.ajithgoveas.khatape.ui.components.AnalyticsCard
import dev.ajithgoveas.khatape.ui.components.AvatarIcon
import dev.ajithgoveas.khatape.ui.components.KhataPeAppTopBar
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val summaries by viewModel.summaries.collectAsState()

    val totalCredit = summaries.sumOf { it.totalCredit }
    val totalDebit = summaries.sumOf { it.totalDebit }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KhataPeAppTopBar(
                title = "Namaste!",
                subtitle = "Your accounts are looking good.",
                emoji = "🙏",
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = 32.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Hero Analytics Card (The "Money" View)
            item {
                AnalyticsCard(
                    debitAmount = totalDebit.toInt(),
                    creditAmount = totalCredit.toInt()
                )
            }

            // 2. Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("Recent Activity", Icons.Default.History)
                    TextButton(onClick = { /* Navigate to All */ }) {
                        Text("See All", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. The Activity List
            if (summaries.isEmpty()) {
                item { EmptyDashboardState() }
            } else {
                items(summaries) { summary ->
                    DashboardRecentCard(summary = summary)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun DashboardRecentCard(summary: FriendSummary) {
    val netBalance = summary.totalCredit - summary.totalDebit

    // Premium FinTech Palette
    val positiveColor = Color(0xFF00C853) // Vibrant Success Green
    val negativeColor = Color(0xFFFF3D00) // Vibrant Warning Orange/Red
    val neutralColor = MaterialTheme.colorScheme.outline

    val isPositive = netBalance > 0
    val isSettled = netBalance == 0.0
    val accentColor = when {
        isSettled -> neutralColor
        isPositive -> positiveColor
        else -> negativeColor
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isSettled) Color.Transparent else accentColor.copy(alpha = 0.15f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with a "Status Ring"
                Box(contentAlignment = Alignment.Center) {
                    // Soft glow ring behind avatar
                    if (!isSettled) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.1f),
                            modifier = Modifier.size(60.dp)
                        ) {}
                    }

                    Surface(
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        AvatarIcon(name = summary.name)
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )

                    // Styled Badge
                    Surface(
                        color = if (isSettled) MaterialTheme.colorScheme.surfaceVariant else accentColor.copy(
                            alpha = 0.1f
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = when {
                                isPositive -> "RECEIVABLE"
                                netBalance < 0 -> "PAYABLE"
                                else -> "SETTLED"
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Amount Section
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isSettled) "₹0" else "₹${abs(netBalance).toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isSettled) MaterialTheme.colorScheme.onSurfaceVariant else accentColor
                    )
                    Text(
                        text = if (isPositive) "They owe" else if (isSettled) "All clear" else "You owe",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // A "Quick Track" Footer - Shows Total Flow
            if (!isSettled) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(accentColor.copy(alpha = 0.03f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Given: ₹${summary.totalCredit.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Total Taken: ₹${summary.totalDebit.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDashboardState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("📈", fontSize = 32.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Your ledger is empty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Start adding khatas to see your flow.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}