package dev.ajithgoveas.khatape.ui.components.charts.lineChart.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import dev.ajithgoveas.khatape.ui.components.charts.lineChart.model.LineParameters
import dev.ajithgoveas.khatape.ui.components.charts.utils.clickedOnThisPoint
import dev.ajithgoveas.khatape.ui.components.charts.utils.formatToThousandsMillionsBillions

private var lastClickedPoint: Pair<Float, Float>? = null

internal fun DrawScope.drawQuarticLineWithShadow(
    line: LineParameters,
    lowerValue: Float,
    upperValue: Float,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    spacingX: Dp,
    spacingY: Dp,
    specialChart: Boolean,
    clickedPoints: MutableList<Pair<Float, Float>>,
    xRegionWidth: Dp,
    textMeasurer: TextMeasurer,
) {
    val strokePathOfQuadraticLine = drawLineAsQuadratic(
        line = line,
        lowerValue = lowerValue,
        upperValue = upperValue,
        animatedProgress = animatedProgress,
        spacingY = spacingY,
        specialChart = specialChart,
        clickedPoints = clickedPoints,
        textMeasurer = textMeasurer,
        xRegionWidth = xRegionWidth
    )

    if (line.lineShadow && !specialChart) {
        val fillPath = strokePathOfQuadraticLine.apply {
            lineTo(x = (size.width - xRegionWidth.toPx()) + 40.dp.toPx(), y = size.height * 40)
            lineTo(x = spacingX.toPx() * 2, y = size.height * 40)
            close()
        }
        clipRect(right = size.width * animatedProgress.value) {
            drawPath(
                path = fillPath, brush = Brush.verticalGradient(
                    colors = listOf(
                        line.lineColor.copy(alpha = .3f), Color.Transparent
                    ), endY = (size.height.toDp() - spacingY).toPx()
                )
            )
        }
    }
}

fun DrawScope.drawLineAsQuadratic(
    line: LineParameters,
    lowerValue: Float,
    upperValue: Float,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    spacingY: Dp,
    specialChart: Boolean,
    clickedPoints: MutableList<Pair<Float, Float>>,
    textMeasurer: TextMeasurer,
    xRegionWidth: Dp
) = Path().apply {
    var medX: Float
    val height = size.height.toDp()

    drawPathLineWrapper(
        lineParameter = line,
        strokePath = this,
        animatedProgress = animatedProgress,
    ) { lineParameter, index ->

        val yTextLayoutResult = textMeasurer.measure(
            text = AnnotatedString(upperValue.formatToThousandsMillionsBillions()),
        ).size.width
        val textSpace = yTextLayoutResult - (yTextLayoutResult / 4)

        val info = lineParameter.data[index]
        val nextInfo = lineParameter.data.getOrNull(index + 1) ?: lineParameter.data.last()
        val firstRatio = (info - lowerValue) / (upperValue - lowerValue)
        val secondRatio = (nextInfo - lowerValue) / (upperValue - lowerValue)

        val xFirstPoint = (textSpace * 1.5.toFloat().toDp()) + index * xRegionWidth
        val xSecondPoint =
            (textSpace * 1.5.toFloat().toDp()) + (index + checkLastIndex(
                lineParameter.data,
                index
            )) * xRegionWidth

        val yFirstPoint = (height.toPx()
                + 11.dp.toPx()
                - spacingY.toPx()
                - (firstRatio * (size.height.toDp() - spacingY).toPx())
                )
        val ySecondPoint = (height.toPx()
                + 11.dp.toPx()
                - spacingY.toPx()
                - (secondRatio * (size.height.toDp() - spacingY).toPx())
                )

        val tolerance = 20.dp.toPx()
        val savedClicks =
            clickedOnThisPoint(
                clickedPoints,
                x = xFirstPoint.toPx(),
                y = yFirstPoint,
                tolerance = tolerance
            )
        if (savedClicks) {
            if (lastClickedPoint != null) {
                clickedPoints.clear()
                lastClickedPoint = null
            } else {
                lastClickedPoint = Pair(first = xFirstPoint.toPx(), second = yFirstPoint.toFloat())
                circleWithRectAndText(
                    x = xFirstPoint,
                    y = yFirstPoint,
                    textMeasure = textMeasurer,
                    info = info,
                    stroke = Stroke(width = 2.dp.toPx()),
                    line = line,
                    animatedProgress = animatedProgress
                )
            }

        }

        if (index == 0) {
            moveTo(x = xFirstPoint.toPx(), y = yFirstPoint.toFloat())
            medX = ((xFirstPoint + xSecondPoint) / 2f).toPx()
            cubicTo(
                x1 = medX,
                y1 = yFirstPoint.toFloat(),
                x2 = medX,
                y2 = ySecondPoint.toFloat(),
                x3 = xSecondPoint.toPx(),
                y3 = ySecondPoint.toFloat()
            )
        } else {
            medX = ((xFirstPoint + xSecondPoint) / 2f).toPx()
            cubicTo(
                x1 = medX,
                y1 = yFirstPoint.toFloat(),
                x2 = medX,
                y2 = ySecondPoint.toFloat(),
                x3 = xSecondPoint.toPx(),
                y3 = ySecondPoint.toFloat()
            )
        }

        if (index == 0 && specialChart) {
            chartCircle(
                x = xFirstPoint.toPx(),
                y = yFirstPoint.toFloat(),
                color = lineParameter.lineColor,
                animatedProgress = animatedProgress,
            )
        }
    }
}

private fun checkLastIndex(data: List<Double>, index: Int): Int {
    return if (data[index] == data[data.lastIndex])
        0
    else
        1
}
