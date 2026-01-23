package dev.ajithgoveas.khatape.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun AnalyticsCard(
    debitAmount: Int,
    creditAmount: Int,
    modifier: Modifier = Modifier
) {
    val net = creditAmount - debitAmount
    val netColor = when {
        net > 0 -> MaterialTheme.colorScheme.primary
        net < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "💰 Balance Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Your current financial standing",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Balance cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BalanceCard(
                    modifier = Modifier.weight(1f),
                    value = debitAmount,
                    label = "You Owe",
                    icon = Icons.Rounded.ArrowUpward,
                    iconTint = MaterialTheme.colorScheme.error,
                    valueColor = MaterialTheme.colorScheme.error
                )
                BalanceCard(
                    modifier = Modifier.weight(1f),
                    value = creditAmount,
                    label = "They Owe You",
                    icon = Icons.Rounded.ArrowDownward,
                    iconTint = MaterialTheme.colorScheme.primary,
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }

            // Proportional bar
            val total = debitAmount + creditAmount
            if (total > 0) {
                LinearProgressIndicator(
                    progress = { creditAmount.toFloat() / total.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                )
            }

            // Net balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Net Balance", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "₹$net",
                    style = MaterialTheme.typography.titleMedium,
                    color = netColor
                )
            }
        }
    }
}