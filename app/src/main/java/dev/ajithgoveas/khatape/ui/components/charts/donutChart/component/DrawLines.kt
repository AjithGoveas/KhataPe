package dev.ajithgoveas.khatape.ui.components.charts.donutChart.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

internal fun DrawScope.drawLines(
    ratioLineColor: Color,
    lineStart: Offset,
    lineEnd: Offset,
    secondLineEnd: Offset,
) {
    val thickness = 2.dp.toPx()

    // Primary radial line
    drawLine(
        color = ratioLineColor,
        start = lineStart,
        end = lineEnd,
        strokeWidth = thickness,
        cap = StrokeCap.Round
    )
    // Horizontal callout line
    drawLine(
        color = ratioLineColor,
        start = lineEnd,
        end = secondLineEnd,
        strokeWidth = thickness,
        cap = StrokeCap.Round
    )
}