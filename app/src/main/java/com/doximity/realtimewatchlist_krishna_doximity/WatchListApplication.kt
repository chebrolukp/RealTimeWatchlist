package com.doximity.realtimewatchlist_krishna_doximity

import android.app.Application
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.WatchlistRepository
import com.doximity.realtimewatchlist_krishna_doximity.ui.widget.WatchlistWidgetUpdater
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@OptIn(FlowPreview::class)
@HiltAndroidApp
class WatchListApplication : Application() {

    @Inject
    lateinit var watchlistRepository: WatchlistRepository

    @Inject
    @Named("applicationScope")
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            watchlistRepository.observeWatchlist()
                .map { items -> items.map { it.symbol } }
                .distinctUntilChanged()
                .debounce(WIDGET_UPDATE_DEBOUNCE_MS)
                .collect {
                    WatchlistWidgetUpdater.updateAll(
                        context = this@WatchListApplication,
                        invalidateCache = true,
                    )
                }
        }
    }

    private companion object {
        const val WIDGET_UPDATE_DEBOUNCE_MS = 400L
    }
}
