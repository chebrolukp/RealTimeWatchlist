package com.doximity.realtimewatchlist_krishna_doximity.ui.widget

import com.doximity.realtimewatchlist_krishna_doximity.BuildConfig
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.WatchlistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LoadWatchlistWidgetDataUseCase @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val marketDataRepository: MarketDataRepository,
) {
    suspend operator fun invoke(limit: Int = DEFAULT_LIMIT): WatchlistWidgetState {
        val items = watchlistRepository.observeWatchlist().first().take(limit)
        val quotes = coroutineScope {
            items.map { item ->
                async {
                    item.symbol to marketDataRepository.getQuote(item.symbol).getOrNull()
                }
            }.awaitAll().toMap()
        }

        return WatchlistWidgetState(
            items = items.map { item ->
                val quote = quotes[item.symbol]
                WatchlistWidgetItem(
                    symbol = item.symbol,
                    displaySymbol = item.displaySymbol,
                    price = quote?.currentPrice?.takeIf { it > 0.0 },
                    change = quote?.change,
                    percentChange = quote?.percentChange,
                )
            },
            isDemoMode = BuildConfig.DEMO_MODE,
        )
    }

    companion object {
        const val DEFAULT_LIMIT = 5
    }
}
