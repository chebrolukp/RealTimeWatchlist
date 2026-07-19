package com.doximity.realtimewatchlist_krishna_doximity.ui.widget

import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.ConnectionState
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.HistoricalPrices
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlert
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceUpdate
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Quote
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.WatchlistItem
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadWatchlistWidgetDataUseCaseTest {

    @Test
    fun invoke_returnsQuotesForWatchlistItems() = runTest {
        val useCase = LoadWatchlistWidgetDataUseCase(
            watchlistRepository = FakeWatchlistRepository(
                listOf(
                    WatchlistItem("AAPL", "AAPL", "Apple", "Common Stock", 0L),
                    WatchlistItem("MSFT", "MSFT", "Microsoft", "Common Stock", 1L),
                ),
            ),
            marketDataRepository = FakeMarketDataRepository(
                quotes = mapOf(
                    "AAPL" to Quote(190.0, 2.0, 1.0, 188.0, 1L),
                    "MSFT" to Quote(420.0, -1.0, -0.2, 421.0, 1L),
                ),
            ),
        )

        val state = useCase(limit = 5)

        assertEquals(2, state.items.size)
        assertEquals("AAPL", state.items[0].displaySymbol)
        assertEquals(190.0, state.items[0].price)
        assertEquals(1.0, state.items[0].percentChange)
        assertEquals(420.0, state.items[1].price)
    }

    @Test
    fun invoke_respectsLimit() = runTest {
        val items = (0 until 6).map { index ->
            WatchlistItem("S$index", "S$index", "Item $index", "Common Stock", index.toLong())
        }
        val useCase = LoadWatchlistWidgetDataUseCase(
            watchlistRepository = FakeWatchlistRepository(items),
            marketDataRepository = FakeMarketDataRepository(
                quotes = items.associate { item ->
                    item.symbol to Quote(10.0, 0.0, 0.0, 10.0, 1L)
                },
            ),
        )

        val state = useCase(limit = 3)

        assertEquals(3, state.items.size)
        assertEquals("S0", state.items.first().symbol)
        assertEquals("S2", state.items.last().symbol)
    }

    @Test
    fun invoke_missingQuote_leavesPriceNull() = runTest {
        val useCase = LoadWatchlistWidgetDataUseCase(
            watchlistRepository = FakeWatchlistRepository(
                listOf(WatchlistItem("AAPL", "AAPL", "Apple", "Common Stock", 0L)),
            ),
            marketDataRepository = FakeMarketDataRepository(quotes = emptyMap()),
        )

        val state = useCase()

        assertTrue(state.items.single().price == null)
    }

    private class FakeWatchlistRepository(
        private val items: List<WatchlistItem>,
    ) : WatchlistRepository {
        override fun observeWatchlist(): Flow<List<WatchlistItem>> = flowOf(items)
        override suspend fun addInstrument(instrument: Instrument) = Unit
        override suspend fun removeInstrument(symbol: String) = Unit
        override suspend fun isInWatchlist(symbol: String): Boolean = false
        override suspend fun setPriceAlert(symbol: String, alert: PriceAlert) = Unit
        override suspend fun clearPriceAlert(symbol: String) = Unit
        override suspend fun markPriceAlertTriggered(symbol: String) = Unit
    }

    private class FakeMarketDataRepository(
        private val quotes: Map<String, Quote>,
    ) : MarketDataRepository {
        override val connectionState: StateFlow<ConnectionState> =
            MutableStateFlow(ConnectionState.Disconnected)
        override val priceUpdates: Flow<PriceUpdate> = emptyFlow()

        override suspend fun searchInstruments(query: String) = Result.success(emptyList<Instrument>())

        override suspend fun getQuote(symbol: String): Result<Quote> =
            quotes[symbol]?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("missing"))

        override suspend fun getHistoricalPrices(
            symbol: String,
            instrumentType: String,
            days: Int,
        ): Result<HistoricalPrices> = Result.success(HistoricalPrices(symbol, emptyList()))

        override fun updateLiveSubscriptions(symbols: Set<String>) = Unit
    }
}
