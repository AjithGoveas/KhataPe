package dev.ajithgoveas.khatape.ui.components.charts.barChart.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import dev.ajithgoveas.khatape.ui.components.charts.barChart.model.BarParameters

internal fun DrawScope.drawBarGroups(
    barsParameters: List<BarParameters>,
    upperValue: Double,
    barWidth: Dp,
    xRegionWidth: Dp,
    spaceBetweenBars: Dp,
    maxWidth: Dp,
    height: Dp, // This is your chart height (e.g., 220.dp)
    animatedProgress: Animatable<Float, AnimationVector1D>,
    barCornerRadius: Dp
) {
    // 1. Ensure we don't divide by zero
    val safeUpperValue = if (upperValue <= 0.0) 1.0 else upperValue

    barsParameters.forEachIndexed { barIndex, bar ->
        bar.data.forEachIndexed { index, data ->
            // 2. Simple ratio: (Current Value / Max Value)
            val ratio = (data.toFloat() / safeUpperValue.toFloat())

            // 3. Calculate length: Height * Progress * Ratio
            val barLength = (height.toPx() * animatedProgress.value) * ratio

            // 4. Calculate X position
            val xAxisLength = (index * xRegionWidth)
            val lengthWithRatio = xAxisLength + (barIndex * (barWidth + spaceBetweenBars))

            // 5. Draw the bar
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(bar.barColor, bar.barColor)),
                topLeft = Offset(
                    x = lengthWithRatio.toPx(),
                    // Draw from the bottom (height) upwards (minus barLength)
                    y = height.toPx() - barLength
                ),
                size = Size(
                    width = barWidth.toPx(),
                    height = barLength
                ),
                cornerRadius = CornerRadius(barCornerRadius.toPx())
            )
        }
    }
}