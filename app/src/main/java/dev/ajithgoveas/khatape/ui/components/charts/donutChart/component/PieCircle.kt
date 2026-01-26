package dev.ajithgoveas.khatape.ui.components.charts.donutChart.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

internal fun DrawScope.draPieCircle(
    circleColor: Color,
    radiusRatioCircle: Float
) {
    // Using a subtle alpha if not provided to make the guide less distracting
    drawCircle(
        color = circleColor,
        radius = radiusRatioCircle,
        style = Stroke(
            width = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}