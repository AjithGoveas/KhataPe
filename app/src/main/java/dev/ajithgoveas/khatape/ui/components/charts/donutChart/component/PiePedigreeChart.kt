package dev.ajithgoveas.khatape.ui.components.charts.donutChart.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dev.ajithgoveas.khatape.ui.components.charts.donutChart.model.ChartTypes
import dev.ajithgoveas.khatape.ui.components.charts.donutChart.model.PieChartData
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

internal fun DrawScope.drawPedigreeChart(
    pieValueWithRatio: List<Float>,
    pieChartData: List<PieChartData>,
    totalSum: Float,
    transitionProgress: Animatable<Float, AnimationVector1D>,
    textMeasure: TextMeasurer,
    textRatioStyle: TextStyle,
    ratioLineColor: Color,
    arcWidth: Float,
    minValue: Float,
    pieChart: ChartTypes
) {
    // Math Refinement: outer radius calculation for callout lines
    val outerCircularRadius = (minValue / 2) + (arcWidth / 1.2f)
    val arcOffset = Offset(center.x - (minValue / 2), center.y - (minValue / 2))
    val arcSize = Size(minValue, minValue)

    var startArc = -90F
    var startArcWithoutAnimation = -90f

    pieValueWithRatio.forEachIndexed { index, _ ->
        val currentData = pieChartData[index].data.toFloat()

        // Calculate angles
        val arcWithAnimation = calculateAngle(currentData, totalSum, transitionProgress.value)
        val arcWithoutAnimation = calculateAngle(currentData, totalSum)

        // Use the static angle for stable callout line positioning
        val angleInRadians = (startArcWithoutAnimation + arcWithoutAnimation / 2).degreeToAngle

        // Line Coordinates
        val lineStart = Offset(
            x = center.x + (outerCircularRadius * 0.95f) * cos(angleInRadians),
            y = center.y + (outerCircularRadius * 0.95f) * sin(angleInRadians)
        )
        val lineEnd = Offset(
            x = center.x + (outerCircularRadius * 1.25f) * cos(angleInRadians),
            y = center.y + (outerCircularRadius * 1.25f) * sin(angleInRadians)
        )

        // Determine which side the line goes (Region-based)
        val region = pieValueWithRatio.subList(0, index).sum()
        val regionSign = if (region >= 180f) 1 else -1
        val secondLineEnd = Offset(lineEnd.x + (arcWidth * regionSign), lineEnd.y)

        // 1. Draw callout lines
        drawLines(ratioLineColor, lineStart, lineEnd, secondLineEnd)

        // 2. Draw Arc (Pie vs Donut logic)
        if (pieChart == ChartTypes.PIE_CHART) {
            scale(1.1f) { // Subtle pop for Pie Charts
                drawArc(
                    color = pieChartData[index].color,
                    startAngle = startArc,
                    sweepAngle = arcWithAnimation,
                    useCenter = true,
                    size = arcSize,
                    topLeft = arcOffset
                )
            }
        } else {
            drawArc(
                color = pieChartData[index].color,
                startAngle = startArc,
                sweepAngle = arcWithAnimation,
                useCenter = false,
                style = Stroke(arcWidth, cap = StrokeCap.Butt),
                size = arcSize,
                topLeft = arcOffset
            )
        }

        // 3. Draw Percentage Text
        val ratioValue = getPartRatio(pieValueWithRatio, index)
        val textLayout = textMeasure.measure("$ratioValue%", textRatioStyle)
        val textOffset = getTextOffsetByRegion(regionSign, lineEnd.x, secondLineEnd.y, arcWidth)

        // Mindful vertical alignment: place text slightly above the callout line
        ratioText(
            textMeasurer = textMeasure,
            ratio = ratioValue,
            textRatioStyle = textRatioStyle,
            topLeft = Offset(
                x = textOffset.x,
                y = textOffset.y - textLayout.size.height - 4.dp.toPx()
            )
        )

        // Update angles
        startArc += arcWithAnimation
        startArcWithoutAnimation += arcWithoutAnimation
    }
}

// Optimized conversion using precise PI
private val Float.degreeToAngle
    get() = (this * Math.PI.toFloat() / 180f)

private fun calculateAngle(dataLength: Float, totalLength: Float, progress: Float): Float =
    -360F * (dataLength / totalLength) * progress

private fun calculateAngle(dataLength: Float, totalLength: Float): Float =
    -360F * (dataLength / totalLength)

private fun getPartRatio(pieValueWithRatio: List<Float>, index: Int): Int {
    // Normalizing against 360 degrees to get percentage
    return (pieValueWithRatio[index] / 360f * 100f).roundToInt()
}

private fun getTextOffsetByRegion(regionSign: Int, x: Float, y: Float, arcWidth: Float): Offset {
    return if (regionSign == 1) {
        Offset(x + arcWidth / 4, y)
    } else {
        Offset(x - arcWidth, y)
    }
}