package com.doximity.realtimewatchlist_krishna_doximity.ui.preview

import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.ConnectionState
import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.PriceStatus
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.model.UiText
import com.doximity.realtimewatchlist_krishna_doximity.data.demo.DemoMarketCatalog
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.SearchResult
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.WatchlistItem
import com.doximity.realtimewatchlist_krishna_doximity.ui.search.SearchUiState
import com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist.ChartUiState
import com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist.WatchlistEntryUiModel
import com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist.WatchlistScreenState
import com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist.WatchlistViewModel

object PreviewSampleData {
    val apple: Instrument = DemoMarketCatalog.apple
    val microsoft: Instrument = DemoMarketCatalog.microsoft
    val bitcoin: Instrument = DemoMarketCatalog.bitcoin

    val searchIdle = SearchUiState(
        query = "",
    )

    val searchLoading = SearchUiState(
        query = "AAPL",
        isSearching = true,
        hasSearched = true,
    )

    val searchResults = SearchUiState(
        query = "A",
        results = listOf(
            SearchResult(apple, isInWatchlist = true),
            SearchResult(microsoft, isInWatchlist = false),
            SearchResult(bitcoin, isInWatchlist = false),
        ),
        hasSearched = true,
    )

    val searchEmpty = SearchUiState(
        query = "ZZZZZ",
        results = emptyList(),
        hasSearched = true,
    )

    val searchError = SearchUiState(
        query = "AAPL",
        errorMessage = UiText.Dynamic("Rate limit exceeded. Try again shortly."),
        hasSearched = true,
    )

    val watchlistEmpty = WatchlistScreenState(
        isLoading = false,
        entries = emptyList(),
        totalItems = 0,
        totalPages = 1,
        currentPage = 0,
    )

    val watchlistLoading = WatchlistScreenState(
        isLoading = true,
    )

    private val sampleEntries = listOf(
        WatchlistEntryUiModel(
            item = WatchlistItem(
                symbol = "AAPL",
                displaySymbol = "AAPL",
                description = "Apple Inc.",
                type = "Common Stock",
                addedAtEpochMs = 0L,
            ),
            price = 190.42,
            change = 2.15,
            percentChange = 1.14,
            status = PriceStatus.Live,
            chart = ChartUiState.Ready(
                prices = listOf(180f, 182f, 179f, 185f, 188f, 186f, 190f),
            ),
        ),
        WatchlistEntryUiModel(
            item = WatchlistItem(
                symbol = "MSFT",
                displaySymbol = "MSFT",
                description = "Microsoft Corp.",
                type = "Common Stock",
                addedAtEpochMs = 0L,
            ),
            price = 420.10,
            change = -1.80,
            percentChange = -0.43,
            status = PriceStatus.Live,
            chart = ChartUiState.Ready(
                prices = listOf(430f, 428f, 426f, 424f, 422f, 421f, 420f),
            ),
        ),
        WatchlistEntryUiModel(
            item = WatchlistItem(
                symbol = "BINANCE:BTCUSDT",
                displaySymbol = "BTC/USDT",
                description = "Bitcoin / Tether",
                type = "Crypto",
                addedAtEpochMs = 0L,
            ),
            price = null,
            change = null,
            percentChange = null,
            status = PriceStatus.Unavailable,
            chart = ChartUiState.Unavailable,
        ),
    )

    val watchlistWithEntries = WatchlistScreenState(
        isLoading = false,
        connectionState = ConnectionState.Connected,
        entries = sampleEntries,
        totalItems = sampleEntries.size,
        totalPages = 1,
        currentPage = 0,
    )

    val watchlistReconnecting = watchlistWithEntries.copy(
        connectionState = ConnectionState.Reconnecting,
        entries = watchlistWithEntries.entries.map { entry ->
            if (entry.item.symbol == "AAPL") {
                entry.copy(status = PriceStatus.Stale)
            } else {
                entry
            }
        },
    )

    private val paginatedAllEntries = List(WatchlistViewModel.PAGE_SIZE + 1) { index ->
        val symbol = "SYM$index"
        WatchlistEntryUiModel(
            item = WatchlistItem(
                symbol = symbol,
                displaySymbol = symbol,
                description = "Instrument $index",
                type = "Common Stock",
                addedAtEpochMs = index.toLong(),
            ),
            price = 100.0 + index,
            change = 1.0,
            percentChange = 1.0,
            status = PriceStatus.Live,
            chart = ChartUiState.Ready(
                prices = listOf(90f, 95f, 100f + index),
            ),
        )
    }

    val watchlistPaginatedPage1 = WatchlistScreenState(
        isLoading = false,
        connectionState = ConnectionState.Connected,
        entries = paginatedAllEntries.take(WatchlistViewModel.PAGE_SIZE),
        totalItems = paginatedAllEntries.size,
        totalPages = 2,
        currentPage = 0,
    )

    val watchlistPaginatedPage2 = watchlistPaginatedPage1.copy(
        entries = paginatedAllEntries.drop(WatchlistViewModel.PAGE_SIZE),
        currentPage = 1,
    )
}
