@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist

import com.doximity.realtimewatchlist_krishna_doximity.MainDispatcherRule
import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.ConnectionState
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.model.UiText
import com.doximity.realtimewatchlist_krishna_doximity.domain.interactor.WatchlistInteractor
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.HistoricalPrices
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PricePoint
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceUpdate
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Quote
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.WatchlistItem
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.WatchlistRepository
import com.doximity.realtimewatchlist_krishna_doximity.domain.usecase.GetHistoricalPricesUseCase
import com.doximity.realtimewatchlist_krishna_doximity.domain.usecase.ObserveWatchlistWithPricesUseCase
import com.doximity.realtimewatchlist_krishna_doximity.domain.usecase.RefreshWatchlistUseCase
import com.doximity.realtimewatchlist_krishna_doximity.domain.usecase.RemoveFromWatchlistUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WatchlistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun emptyWatchlist_exposesEmptyState() = runViewModelTest { viewModel, _ ->
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.totalItems)
        assertTrue(state.entries.isEmpty())
        assertFalse(state.showPagination)
    }

    @Test
    fun watchlistEntries_loadChartsForVisiblePage() = runViewModelTest(
        initialItems = listOf(item("AAPL"), item("MSFT")),
    ) { viewModel, _ ->
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.totalItems)
        assertEquals(2, state.entries.size)
        assertTrue(state.entries.all { it.chart is ChartUiState.Ready })
        assertFalse(state.showPagination)
    }

    @Test
    fun livePrice_updatesChartTip() = runViewModelTest(
        initialItems = listOf(item("AAPL")),
        historyBySymbol = mapOf(
            "AAPL" to listOf(100.0, 101.0, 102.0),
        ),
        quotePrice = 100.0,
    ) { viewModel, deps ->
        advanceUntilIdle()

        val before = viewModel.uiState.value.entries.single().chart as ChartUiState.Ready
        // Live quote is overlaid on the tip of the historical series.
        assertEquals(100f, before.prices.last())
        assertEquals(listOf(100f, 101f), before.prices.dropLast(1))

        deps.market.emitPrice(
            PriceUpdate(symbol = "AAPL", price = 110.0, timestampMs = 2L),
        )
        advanceUntilIdle()

        val after = viewModel.uiState.value.entries.single().chart as ChartUiState.Ready
        assertEquals(110f, after.prices.last())
        assertEquals(listOf(100f, 101f), after.prices.dropLast(1))
    }

    @Test
    fun pagination_showsOnlyCurrentPageAndNavigates() = runViewModelTest(
        initialItems = (0..WatchlistViewModel.PAGE_SIZE).map { index ->
            item(symbol = "SYM$index")
        },
    ) { viewModel, _ ->
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(WatchlistViewModel.PAGE_SIZE + 1, state.totalItems)
        assertEquals(2, state.totalPages)
        assertTrue(state.showPagination)
        assertEquals(WatchlistViewModel.PAGE_SIZE, state.entries.size)
        assertEquals("SYM0", state.entries.first().item.symbol)
        assertTrue(state.canGoToNextPage)
        assertFalse(state.canGoToPreviousPage)

        viewModel.nextPage()
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals(1, state.currentPage)
        assertEquals(1, state.entries.size)
        assertEquals("SYM${WatchlistViewModel.PAGE_SIZE}", state.entries.single().item.symbol)
        assertTrue(state.canGoToPreviousPage)
        assertFalse(state.canGoToNextPage)

        viewModel.previousPage()
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals(0, state.currentPage)
        assertEquals(WatchlistViewModel.PAGE_SIZE, state.entries.size)
    }

    @Test
    fun removingItems_clampsPageIndex() = runViewModelTest(
        initialItems = (0..WatchlistViewModel.PAGE_SIZE).map { index ->
            item(symbol = "SYM$index")
        },
    ) { viewModel, deps ->
        advanceUntilIdle()
        viewModel.nextPage()
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.currentPage)

        deps.watchlist.setItems(listOf(item("SYM0"), item("SYM1")))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.currentPage)
        assertEquals(2, state.totalItems)
        assertFalse(state.showPagination)
    }

    @Test
    fun quoteError_mapsToUiMessage() = runViewModelTest(
        initialItems = listOf(item("AAPL")),
        quoteError = IllegalStateException("boom"),
    ) { viewModel, _ ->
        advanceUntilIdle()

        val message = viewModel.uiState.value.errorMessage
        assertEquals(UiText.Dynamic("boom"), message)
    }

    private fun runViewModelTest(
        initialItems: List<WatchlistItem> = emptyList(),
        historyBySymbol: Map<String, List<Double>> = emptyMap(),
        quotePrice: Double = 100.0,
        quoteError: Throwable? = null,
        testBody: suspend TestScope.(WatchlistViewModel, TestDeps) -> Unit,
    ) = runTest(mainDispatcherRule.dispatcher) {
        val applicationScope = CoroutineScope(SupervisorJob() + mainDispatcherRule.dispatcher)
        val watchlistRepository = FakeWatchlistRepository(MutableStateFlow(initialItems))
        val marketRepository = FakeMarketDataRepository(
            historyBySymbol = historyBySymbol,
            quotePrice = quotePrice,
            quoteError = quoteError,
        )
        val interactor = WatchlistInteractor(
            watchlistRepository = watchlistRepository,
            marketDataRepository = marketRepository,
            applicationScope = applicationScope,
        )
        val viewModel = WatchlistViewModel(
            observeWatchlistWithPricesUseCase = ObserveWatchlistWithPricesUseCase(interactor),
            refreshWatchlistUseCase = RefreshWatchlistUseCase(interactor),
            removeFromWatchlistUseCase = RemoveFromWatchlistUseCase(interactor),
            getHistoricalPricesUseCase = GetHistoricalPricesUseCase(marketRepository),
        )
        try {
            testBody(
                viewModel,
                TestDeps(
                    watchlist = watchlistRepository,
                    market = marketRepository,
                ),
            )
        } finally {
            applicationScope.cancel()
        }
    }

    private data class TestDeps(
        val watchlist: FakeWatchlistRepository,
        val market: FakeMarketDataRepository,
    )

    private fun item(symbol: String) = WatchlistItem(
        symbol = symbol,
        displaySymbol = symbol,
        description = "$symbol Inc.",
        type = "Common Stock",
        addedAtEpochMs = 0L,
    )

    private class FakeWatchlistRepository(
        private val items: MutableStateFlow<List<WatchlistItem>>,
    ) : WatchlistRepository {
        override fun observeWatchlist(): Flow<List<WatchlistItem>> = items

        override suspend fun addInstrument(instrument: Instrument) = Unit

        override suspend fun removeInstrument(symbol: String) {
            items.value = items.value.filterNot { it.symbol == symbol }
        }

        override suspend fun isInWatchlist(symbol: String): Boolean =
            items.value.any { it.symbol == symbol }

        fun setItems(value: List<WatchlistItem>) {
            items.value = value
        }
    }

    private class FakeMarketDataRepository(
        private val historyBySymbol: Map<String, List<Double>> = emptyMap(),
        private val quotePrice: Double = 100.0,
        private val quoteError: Throwable? = null,
    ) : MarketDataRepository {
        private val _connectionState = MutableStateFlow(ConnectionState.Connected)
        override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

        private val _priceUpdates = MutableSharedFlow<PriceUpdate>(extraBufferCapacity = 8)
        override val priceUpdates: Flow<PriceUpdate> = _priceUpdates.asSharedFlow()

        override suspend fun searchInstruments(query: String) = Result.success(emptyList<Instrument>())

        override suspend fun getQuote(symbol: String): Result<Quote> {
            quoteError?.let { return Result.failure(it) }
            return Result.success(
                Quote(
                    currentPrice = quotePrice,
                    change = 1.0,
                    percentChange = 1.0,
                    previousClose = quotePrice - 1.0,
                    timestampSeconds = 1L,
                ),
            )
        }

        override suspend fun getHistoricalPrices(
            symbol: String,
            instrumentType: String,
            days: Int,
        ): Result<HistoricalPrices> {
            val prices = historyBySymbol[symbol] ?: listOf(10.0, 11.0, 12.0)
            return Result.success(
                HistoricalPrices(
                    symbol = symbol,
                    points = prices.mapIndexed { index, price ->
                        PricePoint(timestampMs = index.toLong(), price = price)
                    },
                ),
            )
        }

        override fun updateLiveSubscriptions(symbols: Set<String>) {
            _connectionState.value = if (symbols.isEmpty()) {
                ConnectionState.Disconnected
            } else {
                ConnectionState.Connected
            }
        }

        suspend fun emitPrice(update: PriceUpdate) {
            _priceUpdates.emit(update)
        }
    }
}
