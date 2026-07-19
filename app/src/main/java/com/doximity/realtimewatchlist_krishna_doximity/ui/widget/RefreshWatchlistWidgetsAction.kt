package com.doximity.realtimewatchlist_krishna_doximity.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class RefreshWatchlistWidgetsAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        WatchlistWidgetUpdater.updateAll(context, invalidateCache = true)
    }
}
