package dev.ajithgoveas.khatape.ui.components.charts.donutChart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ajithgoveas.khatape.ui.components.charts.baseComponents.model.LegendPosition
import dev.ajithgoveas.khatape.ui.components.charts.donutChart.component.PieChartDescriptionComposable
import dev.ajithgoveas.khatape.ui.components.charts.donutChart.component.draPieCircle
import dev.ajithgoveas.khatape.ui.components.charts.donutChart.component.drawPedigreeChart
import dev.ajithgoveas.khatape.ui.components.charts.donutChart.model.ChartTypes
import dev.ajithgoveas.khatape.ui.components.charts.donutChart.model.PieChartData
import dev.ajithgoveas.khatape.ui.components.charts.utils.ChartDefaultValues
import dev.ajithgoveas.khatape.ui.components.charts.utils.checkIfDataIsNegative

/**
 * Composable function to render a pie chart with an optional legend.
 *
 * @param modifier Modifier for configuring the layout and appearance of the pie chart.
 * @param pieChartData List of data for the pie chart, including labels and values.
 * @param animation Animation specification for the pie chart transitions (default is a 3-second linear animation).
 * @param textRatioStyle TextStyle for configuring the appearance of ratio text labels (default font size is 12sp).
 * @param outerCircularColor Color of the outer circular border of the pie chart (default is Gray).
 * @param ratioLineColor Color of the lines connecting ratio labels to chart segments (default is Gray).
 * @param descriptionStyle TextStyle for configuring the appearance of the chart description (legend) text.
 * @param legendPosition Position of the legend within the chart (default is [LegendPosition.TOP]).
 *
 * @see PieChartData
 * @see LegendPosition
 */

@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    pieChartData: List<PieChartData>,
    animation: AnimationSpec<Float> = TweenSpec(durationMillis = 1500), // Sped up for better UX
    textRatioStyle: TextStyle = TextStyle.Default.copy(fontSize = 12.sp),
    outerCircularColor: Color = Color.Gray,
    ratioLineColor: Color = Color.Gray,
    descriptionStyle: TextStyle = TextStyle.Default,
    legendPosition: LegendPosition = ChartDefaultValues.legendPosition,
) {
    // 1. Data Processing (Memoized to prevent redundant math on every recomposition)
    val (totalSum, pieValueWithRatio) = remember(pieChartData) {
        val sum = pieChartData.sumOf { it.data }.toFloat()
        val ratios = pieChartData.map { 360f * it.data.toFloat() / sum }
        sum to ratios
    }

    val textMeasure = rememberTextMeasurer()
    checkIfDataIsNegative(data = pieChartData.map { it.data })

    val transitionProgress = remember(pieChartData) { Animatable(initialValue = 0F) }
    LaunchedEffect(pieChartData) {
        transitionProgress.animateTo(1F, animationSpec = animation)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Layout logic based on LegendPosition
        if (legendPosition == LegendPosition.TOP) {
            PieChartDescriptionComposable(
                pieChartData = pieChartData,
                descriptionStyle = descriptionStyle,
                modifier = Modifier.fillMaxWidth()
            )
        }

        DrawPieChart(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            pieChartData = pieChartData,
            textRatioStyle = textRatioStyle,
            outerCircularColor = outerCircularColor,
            ratioLineColor = ratioLineColor,
            pieValueWithRatio = pieValueWithRatio,
            totalSum = totalSum,
            transitionProgress = transitionProgress,
            textMeasure = textMeasure
        )

        if (legendPosition == LegendPosition.BOTTOM) {
            PieChartDescriptionComposable(
                pieChartData = pieChartData,
                descriptionStyle = descriptionStyle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DrawPieChart(
    modifier: Modifier = Modifier,
    pieChartData: List<PieChartData>,
    textRatioStyle: TextStyle,
    outerCircularColor: Color,
    ratioLineColor: Color,
    pieValueWithRatio: List<Float>, // Changed to List for stability
    totalSum: Float,
    transitionProgress: Animatable<Float, AnimationVector1D>,
    textMeasure: TextMeasurer,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Determine the safe drawing area (leaving 20% room for labels)
                val safeSize = size.minDimension * 0.75f
                val minValue = safeSize.coerceAtMost(size.height).coerceAtMost(size.width)

                // Dynamic arc width relative to chart size
                val arcWidth = (minValue * 0.15f)

                // Render the main segments and callout lines
                drawPedigreeChart(
                    pieValueWithRatio = pieValueWithRatio,
                    pieChartData = pieChartData,
                    totalSum = totalSum,
                    transitionProgress = transitionProgress,
                    textMeasure = textMeasure,
                    textRatioStyle = textRatioStyle,
                    ratioLineColor = ratioLineColor,
                    arcWidth = arcWidth,
                    minValue = minValue,
                    pieChart = ChartTypes.PIE_CHART
                )

                // Render outer guide circle (slightly transparent for a modern feel)
                draPieCircle(
                    circleColor = outerCircularColor.copy(alpha = 0.4f),
                    radiusRatioCircle = (minValue / 2f) + (arcWidth / 1.5f)
                )
            }
    )
}
