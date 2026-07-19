package com.doximity.realtimewatchlist_krishna_doximity.ui.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object WatchlistWidgetUpdater {
    suspend fun updateAll(context: Context, invalidateCache: Boolean = false) {
        if (invalidateCache) {
            val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java,
            )
            entryPoint.loadWatchlistWidgetDataUseCase().invalidate()
        }
        WatchlistGlanceWidget().updateAll(context)
        QuoteGlanceWidget().updateAll(context)
    }
}
