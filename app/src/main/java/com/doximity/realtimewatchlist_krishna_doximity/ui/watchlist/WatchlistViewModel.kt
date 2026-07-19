package com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.ConnectionState
import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.PriceStatus
import com.doximity.realtimewatchlist_krishna_doximity.core.error.toUiText
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.model.UiText
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.WatchlistItem
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.WatchlistOverview
import com.doximity.realtimewatchlist_krishna_doximity.domain.usecase.GetHistoricalPricesUseCase
import com.doximity.realtimewatchlist_krishna_doximity.domain.usecase.ObserveWatchlistWithPricesUseCase
import com.doximity.realtimewatchlist_krishna_doximity.domain.usecase.RefreshWatchlistUseCase
import com.doximity.realtimewatchlist_krishna_doximity.domain.usecase.RemoveFromWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

sealed interface ChartUiState {
    data object Loading : ChartUiState
    data object Unavailable : ChartUiState
    data class Ready(
        val prices: List<Float>,
        val liveTip: Float? = null,
    ) : ChartUiState {
        val tipPrice: Float
            get() = liveTip ?: prices.last()
    }
}

data class WatchlistEntryUiModel(
    val item: WatchlistItem,
    val price: Double?,
    val change: Double?,
    val percentChange: Double?,
    val status: PriceStatus,
    val chart: ChartUiState = ChartUiState.Loading,
)

data class WatchlistScreenState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val entries: List<WatchlistEntryUiModel> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val errorMessage: UiText? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
) {
    val canGoToPreviousPage: Boolean get() = currentPage > 0
    val canGoToNextPage: Boolean get() = currentPage < totalPages - 1
    val showPagination: Boolean get() = totalItems > WatchlistViewModel.PAGE_SIZE
}

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    observeWatchlistWithPricesUseCase: ObserveWatchlistWithPricesUseCase,
    private val refreshWatchlistUseCase: RefreshWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    private val getHistoricalPricesUseCase: GetHistoricalPricesUseCase,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val chartStates = MutableStateFlow<Map<String, ChartUiState>>(emptyMap())
    private val pageIndex = MutableStateFlow(0)

    private val overviewFlow = observeWatchlistWithPricesUseCase()

    val uiState: StateFlow<WatchlistScreenState> = combine(
        overviewFlow,
        isRefreshing,
        chartStates,
        pageIndex,
    ) { overview, refreshing, charts, page ->
        overview.toUiState(
            refreshing = refreshing,
            charts = charts,
            requestedPage = page,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WatchlistScreenState(),
    )

    init {
        viewModelScope.launch {
            overviewFlow
                .map { overview -> overview.entries.map { it.item.symbol }.toSet() }
                .distinctUntilChanged()
                .collect { symbols ->
                    val maxPage = maxPageIndex(symbols.size)
                    pageIndex.update { current -> current.coerceAtMost(maxPage) }
                    chartStates.update { charts -> charts.filterKeys { it in symbols } }
                }
        }

        viewModelScope.launch {
            combine(
                overviewFlow.map { overview ->
                    overview.entries.map { it.item.symbol to it.item.type }
                },
                pageIndex,
            ) { symbolTypes, page ->
                val safePage = page.coerceAtMost(maxPageIndex(symbolTypes.size))
                pageSlice(symbolTypes, safePage).toMap()
            }
                .distinctUntilChanged()
                .collectLatest { visibleSymbolTypes ->
                    syncCharts(symbolTypes = visibleSymbolTypes, force = false)
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.update { true }
            try {
                refreshWatchlistUseCase()
                val visibleSymbolTypes = uiState.value.entries.associate { entry ->
                    entry.item.symbol to entry.item.type
                }
                syncCharts(symbolTypes = visibleSymbolTypes, force = true)
            } finally {
                isRefreshing.update { false }
            }
        }
    }

    fun removeSymbol(symbol: String) {
        removeFromWatchlistUseCase(symbol)
    }

    fun nextPage() {
        pageIndex.update { current ->
            val maxPage = (uiState.value.totalPages - 1).coerceAtLeast(0)
            (current + 1).coerceAtMost(maxPage)
        }
    }

    fun previousPage() {
        pageIndex.update { current -> (current - 1).coerceAtLeast(0) }
    }

    private suspend fun syncCharts(
        symbolTypes: Map<String, String>,
        force: Boolean,
    ) {
        if (symbolTypes.isEmpty()) return

        chartStates.update { current ->
            if (force) {
                current + symbolTypes.keys.associateWith { ChartUiState.Loading }
            } else {
                current + symbolTypes.keys
                    .filter { symbol -> current[symbol] !is ChartUiState.Ready }
                    .associateWith { ChartUiState.Loading }
            }
        }

        val symbolsToLoad = if (force) {
            symbolTypes.keys
        } else {
            symbolTypes.keys.filter { symbol ->
                chartStates.value[symbol] !is ChartUiState.Ready
            }
        }
        if (symbolsToLoad.isEmpty()) return

        val results = coroutineScope {
            symbolsToLoad.map { symbol ->
                async {
                    val instrumentType = symbolTypes[symbol].orEmpty()
                    val chartState = getHistoricalPricesUseCase(
                        symbol = symbol,
                        instrumentType = instrumentType,
                    ).fold(
                        onSuccess = { history ->
                            if (history.points.size >= 2) {
                                ChartUiState.Ready(history.points.map { it.price.toFloat() })
                            } else {
                                ChartUiState.Unavailable
                            }
                        },
                        onFailure = { ChartUiState.Unavailable },
                    )
                    symbol to chartState
                }
            }.awaitAll()
        }

        chartStates.update { states -> states + results.toMap() }
    }

    private fun WatchlistOverview.toUiState(
        refreshing: Boolean,
        charts: Map<String, ChartUiState>,
        requestedPage: Int,
    ): WatchlistScreenState {
        val totalItems = entries.size
        val totalPages = totalPagesFor(totalItems)
        val currentPage = requestedPage.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
        val pageEntries = pageSlice(entries, currentPage).map { entry ->
            WatchlistEntryUiModel(
                item = entry.item,
                price = entry.price,
                change = entry.change,
                percentChange = entry.percentChange,
                status = entry.status,
                chart = (charts[entry.item.symbol] ?: ChartUiState.Loading)
                    .withLivePrice(entry.price),
            )
        }

        return WatchlistScreenState(
            isLoading = false,
            isRefreshing = refreshing,
            entries = pageEntries,
            connectionState = connectionState,
            errorMessage = error?.toUiText(),
            currentPage = currentPage,
            totalPages = totalPages,
            totalItems = totalItems,
        )
    }

    companion object {
        const val PAGE_SIZE = 5
    }
}

private fun totalPagesFor(totalItems: Int): Int =
    if (totalItems <= 0) 1 else ceil(totalItems / WatchlistViewModel.PAGE_SIZE.toDouble()).toInt()

private fun maxPageIndex(totalItems: Int): Int =
    (totalPagesFor(totalItems) - 1).coerceAtLeast(0)

private fun <T> pageSlice(items: List<T>, page: Int): List<T> {
    val start = page * WatchlistViewModel.PAGE_SIZE
    if (start >= items.size) return emptyList()
    return items.subList(start, minOf(start + WatchlistViewModel.PAGE_SIZE, items.size))
}

/** Pins the live tip without copying the historical series. */
private fun ChartUiState.withLivePrice(livePrice: Double?): ChartUiState {
    if (this !is ChartUiState.Ready || livePrice == null || prices.isEmpty()) return this
    val tip = livePrice.toFloat()
    if (liveTip == tip) return this
    return copy(liveTip = tip)
}
