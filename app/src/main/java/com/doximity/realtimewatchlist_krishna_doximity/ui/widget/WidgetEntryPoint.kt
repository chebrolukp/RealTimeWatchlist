package com.doximity.realtimewatchlist_krishna_doximity.ui.widget

import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.WatchlistRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun watchlistRepository(): WatchlistRepository
    fun marketDataRepository(): MarketDataRepository
    fun loadWatchlistWidgetDataUseCase(): LoadWatchlistWidgetDataUseCase
}
