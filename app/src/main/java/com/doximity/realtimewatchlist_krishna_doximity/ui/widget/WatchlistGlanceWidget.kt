package com.doximity.realtimewatchlist_krishna_doximity.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.doximity.realtimewatchlist_krishna_doximity.MainActivity
import com.doximity.realtimewatchlist_krishna_doximity.R
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.util.formatPercentChange
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.util.formatPrice
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

private val WidgetPrimary = ColorProvider(Color(0xFF004AC6), Color(0xFF004AC6))
private val WidgetBackground = ColorProvider(Color(0xFFF5F5F5), Color(0xFFF5F5F5))
private val WidgetSurface = ColorProvider(Color(0xFFFFFFF0), Color(0xFFFFFFF0))
private val WidgetOnSurface = ColorProvider(Color(0xFF131B2E), Color(0xFF131B2E))
private val WidgetOnSurfaceVariant = ColorProvider(Color(0xFF434655), Color(0xFF434655))
private val WidgetPositive = ColorProvider(Color(0xFF006242), Color(0xFF006242))
private val WidgetNegative = ColorProvider(Color(0xFFBA1A1A), Color(0xFFBA1A1A))

class WatchlistGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        val state = entryPoint.loadWatchlistWidgetDataUseCase()(
            limit = LoadWatchlistWidgetDataUseCase.DEFAULT_LIMIT,
        )

        provideContent {
            GlanceTheme {
                WatchlistWidgetContent(state = state)
            }
        }
    }
}

@Composable
private fun WatchlistWidgetContent(state: WatchlistWidgetState) {
    val context = LocalContext.current
    val openApp = actionStartActivity(
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBackground)
            .cornerRadius(16.dp)
            .padding(12.dp)
            .clickable(openApp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = context.getString(R.string.widget_watchlist_title),
                style = TextStyle(
                    color = WidgetPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier.defaultWeight(),
            )
            Text(
                text = "↻",
                style = TextStyle(
                    color = WidgetPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier
                    .padding(4.dp)
                    .clickable(actionRunCallback<RefreshWatchlistWidgetsAction>()),
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (state.items.isEmpty()) {
            Text(
                text = context.getString(R.string.widget_empty_title),
                style = TextStyle(
                    color = WidgetOnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = context.getString(R.string.widget_empty_message),
                style = TextStyle(
                    color = WidgetOnSurfaceVariant,
                    fontSize = 12.sp,
                ),
            )
        } else {
            state.items.forEach { item ->
                WatchlistWidgetRow(item = item)
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
        }
    }
}

@Composable
internal fun WatchlistWidgetRow(item: WatchlistWidgetItem) {
    val context = LocalContext.current
    val changeColor = when {
        item.percentChange == null -> WidgetOnSurfaceVariant
        item.percentChange >= 0 -> WidgetPositive
        else -> WidgetNegative
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(WidgetSurface)
            .cornerRadius(10.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.displaySymbol,
            style = TextStyle(
                color = WidgetOnSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.price?.let(::formatPrice)
                    ?: context.getString(R.string.widget_price_unavailable),
                style = TextStyle(
                    color = WidgetOnSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            item.percentChange?.let { percent ->
                Text(
                    text = formatPercentChange(percent),
                    style = TextStyle(
                        color = changeColor,
                        fontSize = 11.sp,
                    ),
                )
            }
        }
    }
}

@AndroidEntryPoint
class WatchlistWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WatchlistGlanceWidget()
}
