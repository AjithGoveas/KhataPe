package dev.ajithgoveas.khatape.ui.components.charts.donutChart.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.IntSize

internal fun DrawScope.drawCenterText(
    textMeasure: TextMeasurer,
    centerTitle: String,
    centerTitleStyle: TextStyle,
    canvasHeight: Float,
    canvasWidth: Float,
    textSize: IntSize
) {
    // Compose drawText is more performant than accessing nativeCanvas directly
    drawText(
        textMeasurer = textMeasure,
        text = centerTitle.take(10), // Keeps logic same
        style = centerTitleStyle,
        topLeft = Offset(
            x = (canvasWidth - textSize.width) / 2f,
            y = (canvasHeight - textSize.height) / 2f
        )
    )
}