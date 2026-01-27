package dev.ajithgoveas.khatape.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ajithgoveas.khatape.domain.model.TransactionDirection

@Composable
fun DirectionToggleCard(
    selectedDirection: TransactionDirection,
    onDirectionChange: (TransactionDirection) -> Unit
) {
    val isCredit = selectedDirection == TransactionDirection.CREDIT

    // Animate bias: -1f (Credit) to 1f (Debit)
    val targetBias by animateFloatAsState(
        targetValue = if (isCredit) -1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow
        ),
        label = "PillBias"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        // Box handles the stacking of the background pill and the foreground text
        Box(
            modifier = Modifier.padding(6.dp),
            contentAlignment = BiasAlignment(
                horizontalBias = targetBias,
                verticalBias = 0f
            )
        ) {
            // THE SLIDING PILL
            // It automatically moves because of the parent's BiasAlignment
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                color = if (isCredit) Color(0xFF2E7D32) else Color(0xFFC62828),
                tonalElevation = 4.dp
            ) {}

            // THE LABELS
            Row(modifier = Modifier.fillMaxSize()) {
                ToggleButton(
                    label = "YOU PAID",
                    isSelected = isCredit,
                    modifier = Modifier.weight(1f),
                    onClick = { onDirectionChange(TransactionDirection.CREDIT) })
                ToggleButton(
                    label = "THEY PAID",
                    isSelected = !isCredit,
                    modifier = Modifier.weight(1f),
                    onClick = { onDirectionChange(TransactionDirection.DEBIT) })
            }
        }
    }
}

@Composable
private fun ToggleButton(
    label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.outline,
        label = "Color"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) { onClick() }, contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}