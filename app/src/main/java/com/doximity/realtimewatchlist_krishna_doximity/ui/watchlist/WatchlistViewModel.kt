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

sealed interface ChartUiState {
    data object Loading : ChartUiState
    data object Unavailable : ChartUiState
    data class Ready(val prices: List<Float>) : ChartUiState
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
)

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    observeWatchlistWithPricesUseCase: ObserveWatchlistWithPricesUseCase,
    private val refreshWatchlistUseCase: RefreshWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    private val getHistoricalPricesUseCase: GetHistoricalPricesUseCase,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val chartStates = MutableStateFlow<Map<String, ChartUiState>>(emptyMap())

    private val overviewFlow = observeWatchlistWithPricesUseCase()

    val uiState: StateFlow<WatchlistScreenState> = combine(
        overviewFlow,
        isRefreshing,
        chartStates,
    ) { overview, refreshing, charts ->
        overview.toUiState(refreshing = refreshing, charts = charts)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WatchlistScreenState(),
    )

    init {
        viewModelScope.launch {
            overviewFlow
                .map { overview ->
                    overview.entries.associate { entry ->
                        entry.item.symbol to entry.item.type
                    }
                }
                .distinctUntilChanged()
                .collectLatest { symbolTypes ->
                    syncCharts(symbolTypes = symbolTypes, force = false)
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.update { true }
            try {
                refreshWatchlistUseCase()
                val symbolTypes = uiState.value.entries.associate { entry ->
                    entry.item.symbol to entry.item.type
                }
                syncCharts(symbolTypes = symbolTypes, force = true)
            } finally {
                isRefreshing.update { false }
            }
        }
    }

    fun removeSymbol(symbol: String) {
        removeFromWatchlistUseCase(symbol)
    }

    private suspend fun syncCharts(
        symbolTypes: Map<String, String>,
        force: Boolean,
    ) {
        chartStates.update { current ->
            val retained = current.filterKeys { it in symbolTypes.keys }
            val next = if (force) {
                symbolTypes.keys.associateWith { ChartUiState.Loading }
            } else {
                retained + symbolTypes.keys
                    .filter { it !in retained }
                    .associateWith { ChartUiState.Loading }
            }
            next
        }

        val symbolsToLoad = if (force) {
            symbolTypes.keys
        } else {
            symbolTypes.keys.filter { symbol ->
                chartStates.value[symbol] !is ChartUiState.Ready
            }
        }
        if (symbolsToLoad.isEmpty()) return

        coroutineScope {
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
        }.forEach { (symbol, chartState) ->
            chartStates.update { states -> states + (symbol to chartState) }
        }
    }

    private fun WatchlistOverview.toUiState(
        refreshing: Boolean,
        charts: Map<String, ChartUiState>,
    ): WatchlistScreenState {
        return WatchlistScreenState(
            isLoading = false,
            isRefreshing = refreshing,
            entries = entries.map { entry ->
                WatchlistEntryUiModel(
                    item = entry.item,
                    price = entry.price,
                    change = entry.change,
                    percentChange = entry.percentChange,
                    status = entry.status,
                    chart = (charts[entry.item.symbol] ?: ChartUiState.Loading)
                        .withLivePrice(entry.price),
                )
            },
            connectionState = connectionState,
            errorMessage = error?.toUiText(),
        )
    }
}

/** Keeps the historical series intact and pins the tip to the latest live quote. */
private fun ChartUiState.withLivePrice(livePrice: Double?): ChartUiState {
    if (this !is ChartUiState.Ready || livePrice == null || prices.isEmpty()) return this
    if (prices.last() == livePrice.toFloat()) return this
    return ChartUiState.Ready(prices = prices.dropLast(1) + livePrice.toFloat())
}
