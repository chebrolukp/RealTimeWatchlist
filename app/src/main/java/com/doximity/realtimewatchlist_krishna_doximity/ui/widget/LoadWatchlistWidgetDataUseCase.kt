package com.doximity.realtimewatchlist_krishna_doximity.ui.widget

import com.doximity.realtimewatchlist_krishna_doximity.BuildConfig
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.WatchlistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadWatchlistWidgetDataUseCase @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val marketDataRepository: MarketDataRepository,
) {
    private val cacheMutex = Mutex()
    private var cachedState: WatchlistWidgetState? = null
    private var cachedAtMs: Long = 0L
    private var cachedLimit: Int = -1

    suspend operator fun invoke(limit: Int = DEFAULT_LIMIT): WatchlistWidgetState {
        val now = System.currentTimeMillis()
        cacheMutex.withLock {
            val cached = cachedState
            if (cached != null && cachedLimit == limit && now - cachedAtMs < CACHE_TTL_MS) {
                return cached
            }
        }

        val items = watchlistRepository.observeWatchlist().first().take(limit)
        val quotes = coroutineScope {
            items.map { item ->
                async {
                    item.symbol to marketDataRepository.getQuote(item.symbol).getOrNull()
                }
            }.awaitAll().toMap()
        }

        val state = WatchlistWidgetState(
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

        cacheMutex.withLock {
            cachedState = state
            cachedAtMs = now
            cachedLimit = limit
        }
        return state
    }

    suspend fun invalidate() {
        cacheMutex.withLock {
            cachedState = null
            cachedAtMs = 0L
            cachedLimit = -1
        }
    }

    companion object {
        const val DEFAULT_LIMIT = 5
        private const val CACHE_TTL_MS = 30_000L
    }
}
