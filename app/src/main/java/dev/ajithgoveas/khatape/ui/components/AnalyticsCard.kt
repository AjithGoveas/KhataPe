package dev.ajithgoveas.khatape.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun AnalyticsCard(
    modifier: Modifier = Modifier,
    debitAmount: Int,   // DEBIT: You owe friends
    creditAmount: Int   // CREDIT: Friends owe you
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "💰 Your Balance",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BalanceCard(
                modifier = Modifier.weight(1f),
                value = debitAmount,
                label = "You Owe", // DEBIT
                icon = Icons.Rounded.ArrowUpward,
                iconTint = MaterialTheme.colorScheme.error,
                valueColor = MaterialTheme.colorScheme.error
            )
            BalanceCard(
                modifier = Modifier.weight(1f),
                value = creditAmount,
                label = "They Owe You", // CREDIT
                icon = Icons.Rounded.ArrowDownward,
                iconTint = MaterialTheme.colorScheme.primary,
                valueColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

