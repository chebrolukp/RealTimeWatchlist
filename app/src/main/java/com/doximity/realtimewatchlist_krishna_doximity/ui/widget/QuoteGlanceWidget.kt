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

private val QuotePrimary = ColorProvider(Color(0xFF004AC6), Color(0xFF004AC6))
private val QuoteBackground = ColorProvider(Color(0xFFFFFFF0), Color(0xFFFFFFF0))
private val QuoteOnSurface = ColorProvider(Color(0xFF131B2E), Color(0xFF131B2E))
private val QuoteOnSurfaceVariant = ColorProvider(Color(0xFF434655), Color(0xFF434655))
private val QuotePositive = ColorProvider(Color(0xFF006242), Color(0xFF006242))
private val QuoteNegative = ColorProvider(Color(0xFFBA1A1A), Color(0xFFBA1A1A))

class QuoteGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        val state = entryPoint.loadWatchlistWidgetDataUseCase()(limit = 1)

        provideContent {
            GlanceTheme {
                QuoteWidgetContent(state = state)
            }
        }
    }
}

@Composable
private fun QuoteWidgetContent(state: WatchlistWidgetState) {
    val context = LocalContext.current
    val openApp = actionStartActivity(
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
    val item = state.items.firstOrNull()
    val changeColor = when {
        item?.percentChange == null -> QuoteOnSurfaceVariant
        item.percentChange >= 0 -> QuotePositive
        else -> QuoteNegative
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(QuoteBackground)
            .cornerRadius(16.dp)
            .padding(12.dp)
            .clickable(openApp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item == null) {
            Text(
                text = context.getString(R.string.widget_empty_title),
                style = TextStyle(
                    color = QuoteOnSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        } else {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.displaySymbol,
                    style = TextStyle(
                        color = QuotePrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                )
                Text(
                    text = "↻",
                    style = TextStyle(
                        color = QuotePrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = GlanceModifier.clickable(
                        actionRunCallback<RefreshWatchlistWidgetsAction>(),
                    ),
                )
            }
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = item.price?.let(::formatPrice)
                    ?: context.getString(R.string.widget_price_unavailable),
                style = TextStyle(
                    color = QuoteOnSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            item.percentChange?.let { percent ->
                Text(
                    text = formatPercentChange(percent),
                    style = TextStyle(
                        color = changeColor,
                        fontSize = 12.sp,
                    ),
                )
            }
        }
    }
}

@AndroidEntryPoint
class QuoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteGlanceWidget()
}
