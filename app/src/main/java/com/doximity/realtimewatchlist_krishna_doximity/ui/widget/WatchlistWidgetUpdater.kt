package com.doximity.realtimewatchlist_krishna_doximity.ui.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object WatchlistWidgetUpdater {
    suspend fun updateAll(context: Context) {
        WatchlistGlanceWidget().updateAll(context)
        QuoteGlanceWidget().updateAll(context)
    }
}
