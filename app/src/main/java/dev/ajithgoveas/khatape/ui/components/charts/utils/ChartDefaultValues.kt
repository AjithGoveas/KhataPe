package dev.ajithgoveas.khatape.ui.components.charts.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ajithgoveas.khatape.ui.components.charts.barChart.model.BarParameters
import dev.ajithgoveas.khatape.ui.components.charts.baseComponents.model.GridOrientation
import dev.ajithgoveas.khatape.ui.components.charts.baseComponents.model.LegendPosition
import dev.ajithgoveas.khatape.ui.components.charts.lineChart.model.LineParameters
import dev.ajithgoveas.khatape.ui.components.charts.lineChart.model.LineType

internal object ChartDefaultValues {

    val lineParameters: List<LineParameters> = listOf(
        LineParameters(
            label = "revenue",
            data = emptyList(),
            lineColor = Color.Blue,
            lineType = LineType.CURVED_LINE,
            lineShadow = true,
        )
    )

    val barParameters: List<BarParameters> = listOf(
        BarParameters(
            dataName = "revenue",
            data = emptyList(),
            barColor = Color.Blue,
        )
    )
    val barWidth = 30.dp
    val spaceBetweenBars = 10.dp
    val spaceBetweenGroups = 40.dp
    const val IS_SHOW_GRID = true
    val gridColor = Color.Gray
    const val ANIMATED_CHART = true
    val backgroundLineWidth = 1.dp
    const val SHOW_BACKGROUND_WITH_SPACER = true
    const val CHART_RATIO = 0f
    val descriptionDefaultStyle = TextStyle(
        color = Color.Black,
        fontSize = 14.sp,
        fontWeight = FontWeight.W400
    )

    val headerArrangement = Arrangement.spacedBy(24.dp)
    val axesStyle = TextStyle(
        fontSize = 12.sp,
        color = Color.Gray,
    )
    const val Y_AXIS_RANGE = 6
    const val SPECIAL_CHART = false
    const val SHOW_X_AXIS = true
    const val SHOW_Y_AXIS = true

    val gridOrientation = GridOrientation.HORIZONTAL
    val legendPosition = LegendPosition.TOP
    val barCornerRadius = 0.dp
}