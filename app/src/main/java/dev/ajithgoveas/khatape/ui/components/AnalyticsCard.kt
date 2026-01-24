package dev.ajithgoveas.khatape.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

//@Composable
//fun AnalyticsCard(
//    debitAmount: Int,
//    creditAmount: Int,
//    modifier: Modifier = Modifier
//) {
//    val net = creditAmount - debitAmount
//    val netColor = when {
//        net > 0 -> MaterialTheme.colorScheme.primary
//        net < 0 -> MaterialTheme.colorScheme.error
//        else -> MaterialTheme.colorScheme.onSurfaceVariant
//    }
//
//    Card(
//        modifier = modifier.fillMaxWidth(),
//        shape = MaterialTheme.shapes.extraLarge,
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
//        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(20.dp),
//            verticalArrangement = Arrangement.spacedBy(20.dp)
//        ) {
//            // Header
//            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                Text(
//                    text = "💰 Balance Overview",
//                    style = MaterialTheme.typography.headlineSmall,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                Text(
//                    text = "Your current financial standing",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
//            // Balance cards
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                BalanceCard(
//                    modifier = Modifier.weight(1f),
//                    value = debitAmount,
//                    label = "You Owe",
//                    icon = Icons.Rounded.ArrowUpward,
//                    iconTint = MaterialTheme.colorScheme.error,
//                    valueColor = MaterialTheme.colorScheme.error
//                )
//                BalanceCard(
//                    modifier = Modifier.weight(1f),
//                    value = creditAmount,
//                    label = "They Owe You",
//                    icon = Icons.Rounded.ArrowDownward,
//                    iconTint = MaterialTheme.colorScheme.primary,
//                    valueColor = MaterialTheme.colorScheme.primary
//                )
//            }
//
//            // Proportional bar
//            val total = debitAmount + creditAmount
//            if (total > 0) {
//                LinearProgressIndicator(
//                    progress = { creditAmount.toFloat() / total.toFloat() },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(8.dp)
//                        .clip(MaterialTheme.shapes.small),
//                    color = MaterialTheme.colorScheme.primary,
//                    trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
//                )
//            }
//
//            // Net balance
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text("Net Balance", style = MaterialTheme.typography.bodyLarge)
//                Text(
//                    text = "₹$net",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = netColor
//                )
//            }
//        }
//    }
//}

@Composable
fun AnalyticsCard(
    debitAmount: Int,
    creditAmount: Int,
    modifier: Modifier = Modifier
) {
    val net = creditAmount - debitAmount
    val total = debitAmount + creditAmount
    val progress = if (total > 0) creditAmount.toFloat() / total.toFloat() else 0.5f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp), // More modern, softer corners
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with a soft chip-style emoji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Worth",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${net.absoluteValue}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                // Visual Indicator for Net status
                Surface(
                    color = (if (net >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error).copy(
                        alpha = 0.1f
                    ),
                    shape = CircleShape
                ) {
                    val indicatorText = when {
                        net > 0 -> "TO GET"     // Simple: You have money to collect
                        net < 0 -> "TO GIVE"    // Simple: You have money to pay back
                        else -> "SETTLED"       // Simple: Everything is at zero
                    }
                    Text(
                        text = indicatorText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (net >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Balanced Progress Bar with labels above it
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp) // Thicker for a "pill" look
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "₹$debitAmount Owed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "₹$creditAmount Receivable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Detailed Stats in a light sub-container
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        "Outbound",
                        "₹$debitAmount",
                        Icons.Rounded.ArrowUpward,
                        MaterialTheme.colorScheme.error
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .height(32.dp)
                            .padding(horizontal = 8.dp)
                    )
                    StatItem(
                        "Inbound",
                        "₹$creditAmount",
                        Icons.Rounded.ArrowDownward,
                        MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}