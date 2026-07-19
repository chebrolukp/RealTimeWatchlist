package com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.doximity.realtimewatchlist_krishna_doximity.R
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.Error
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.Tertiary

@Composable
fun PriceSparkline(
    chart: ChartUiState,
    displaySymbol: String,
    modifier: Modifier = Modifier.height(56.dp),
) {
    val chartDescription = when (chart) {
        ChartUiState.Loading -> stringResource(R.string.a11y_chart_loading, displaySymbol)
        ChartUiState.Unavailable -> stringResource(R.string.a11y_chart_unavailable, displaySymbol)
        is ChartUiState.Ready -> {
            val isUp = chart.prices.last() >= chart.prices.first()
            stringResource(
                if (isUp) R.string.a11y_chart_up else R.string.a11y_chart_down,
                displaySymbol,
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = chartDescription },
        contentAlignment = Alignment.Center,
    ) {
        when (chart) {
            ChartUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            }

            ChartUiState.Unavailable -> {
                Text(
                    text = stringResource(R.string.chart_unavailable),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is ChartUiState.Ready -> {
                val isUp = chart.prices.last() >= chart.prices.first()
                val lineColor = if (isUp) Tertiary else Error
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val prices = chart.prices
                    if (prices.size < 2) return@Canvas

                    val min = prices.minOrNull() ?: return@Canvas
                    val max = prices.maxOrNull() ?: return@Canvas
                    val range = max - min
                    val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    val path = Path()

                    fun yFor(price: Float): Float =
                        if (range <= 0f) {
                            size.height / 2f
                        } else {
                            size.height - ((price - min) / range) * size.height
                        }

                    prices.forEachIndexed { index, price ->
                        val x = size.width * index / (prices.lastIndex).toFloat()
                        val y = yFor(price)
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    drawPath(path = path, color = lineColor, style = stroke)

                    drawCircle(
                        color = lineColor,
                        radius = 3.dp.toPx(),
                        center = Offset(size.width, yFor(prices.last())),
                    )
                }
            }
        }
    }
}
