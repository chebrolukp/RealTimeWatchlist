@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.doximity.realtimewatchlist_krishna_doximity.domain.interactor

import app.cash.turbine.test
import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.ConnectionState
import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.PriceStatus
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.HistoricalPrices
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceUpdate
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Quote
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.WatchlistItem
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.WatchlistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchlistInteractorTest {

    private val dispatcher = StandardTestDispatcher()

    private fun runApplicationScopeTest(
        testBody: suspend TestScope.(CoroutineScope) -> Unit,
    ) = runTest(dispatcher) {
        val applicationScope = CoroutineScope(SupervisorJob() + dispatcher)
        try {
            testBody(applicationScope)
        } finally {
            applicationScope.cancel()
        }
    }

    @Test
    fun emptyWatchlist_emitsEmptyState() = runApplicationScopeTest { scope ->
        val watchlistRepository = FakeWatchlistRepository(MutableStateFlow(emptyList()))
        val marketDataRepository = FakeMarketDataRepository()
        val interactor = WatchlistInteractor(
            watchlistRepository = watchlistRepository,
            marketDataRepository = marketDataRepository,
            applicationScope = scope,
        )

        interactor.start()

        interactor.overview.test {
            val overview = awaitItem()
            assertTrue(overview.entries.isEmpty())
            assertEquals(ConnectionState.Disconnected, overview.connectionState)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun watchlistWithQuote_buildsLiveEntry() = runApplicationScopeTest { scope ->
        val item = WatchlistItem("AAPL", "AAPL", "Apple Inc.", "Common Stock", 0L)
        val watchlistRepository = FakeWatchlistRepository(MutableStateFlow(listOf(item)))
        val marketDataRepository = FakeMarketDataRepository(
            quotes = mapOf(
                "AAPL" to Quote(
                    currentPrice = 190.0,
                    change = 2.0,
                    percentChange = 1.0,
                    previousClose = 188.0,
                    timestampSeconds = System.currentTimeMillis() / 1_000,
                ),
            ),
        )
        val interactor = WatchlistInteractor(
            watchlistRepository = watchlistRepository,
            marketDataRepository = marketDataRepository,
            applicationScope = scope,
        )

        interactor.start()
        advanceUntilIdle()

        val entry = interactor.overview.value.entries.single()
        assertEquals(190.0, entry.price)
        assertEquals(PriceStatus.Live, entry.status)
    }

    @Test
    fun liveUpdate_overridesSnapshotPrice() = runApplicationScopeTest { scope ->
        val item = WatchlistItem("AAPL", "AAPL", "Apple Inc.", "Common Stock", 0L)
        val watchlistRepository = FakeWatchlistRepository(MutableStateFlow(listOf(item)))
        val marketDataRepository = FakeMarketDataRepository(
            quotes = mapOf(
                "AAPL" to Quote(
                    currentPrice = 190.0,
                    change = 2.0,
                    percentChange = 1.0,
                    previousClose = 188.0,
                    timestampSeconds = System.currentTimeMillis() / 1_000,
                ),
            ),
        )
        val interactor = WatchlistInteractor(
            watchlistRepository = watchlistRepository,
            marketDataRepository = marketDataRepository,
            applicationScope = scope,
        )

        interactor.start()
        advanceUntilIdle()

        marketDataRepository.emitPrice(
            PriceUpdate(
                symbol = "AAPL",
                price = 191.5,
                timestampMs = System.currentTimeMillis(),
            ),
        )
        advanceUntilIdle()

        assertEquals(191.5, interactor.overview.value.entries.single().price)
    }

    private class FakeWatchlistRepository(
        private val items: StateFlow<List<WatchlistItem>>,
    ) : WatchlistRepository {
        override fun observeWatchlist(): Flow<List<WatchlistItem>> = items

        override suspend fun addInstrument(instrument: Instrument) = Unit

        override suspend fun removeInstrument(symbol: String) = Unit

        override suspend fun isInWatchlist(symbol: String): Boolean = false
    }

    private class FakeMarketDataRepository(
        private val quotes: Map<String, Quote> = emptyMap(),
    ) : MarketDataRepository {
        private val _connectionState = MutableStateFlow(ConnectionState.Connected)
        override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

        private val _priceUpdates = MutableSharedFlow<PriceUpdate>(extraBufferCapacity = 8)
        override val priceUpdates: Flow<PriceUpdate> = _priceUpdates.asSharedFlow()

        override suspend fun searchInstruments(query: String): Result<List<Instrument>> =
            Result.success(emptyList())

        override suspend fun getQuote(symbol: String): Result<Quote> =
            quotes[symbol]?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("Missing quote"))

        override suspend fun getHistoricalPrices(
            symbol: String,
            instrumentType: String,
            days: Int,
        ): Result<HistoricalPrices> =
            Result.success(
                HistoricalPrices(
                    symbol = symbol,
                    points = emptyList(),
                ),
            )

        override fun updateLiveSubscriptions(symbols: Set<String>) = Unit

        suspend fun emitPrice(update: PriceUpdate) {
            _priceUpdates.emit(update)
        }
    }
}
