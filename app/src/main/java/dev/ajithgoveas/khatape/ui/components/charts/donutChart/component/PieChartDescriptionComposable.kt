package dev.ajithgoveas.khatape.ui.components.charts.donutChart.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dev.ajithgoveas.khatape.ui.components.charts.baseComponents.ChartDescription
import dev.ajithgoveas.khatape.ui.components.charts.donutChart.model.PieChartData

@Composable
internal fun PieChartDescriptionComposable(
    modifier: Modifier = Modifier,
    pieChartData: List<PieChartData>,
    descriptionStyle: TextStyle = TextStyle.Default
) {
    // Ensure the description row is accessible and smooth
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth() // Changed to maxWidth for better layout integration
            .padding(bottom = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(
            items = pieChartData,
            key = { it.partName } // Performance: Key helps LazyRow skip recomposition
        ) { details ->
            ChartDescription(
                chartColor = details.color,
                chartName = details.partName,
                descriptionStyle = descriptionStyle,
            )
        }
    }
}