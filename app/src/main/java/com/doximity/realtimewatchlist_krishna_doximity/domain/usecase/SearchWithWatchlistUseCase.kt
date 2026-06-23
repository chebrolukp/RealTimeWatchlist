package com.doximity.realtimewatchlist_krishna_doximity.domain.usecase

import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.SearchResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private val SEARCH_DEBOUNCE_MS = 300.milliseconds

data class SearchWithWatchlistState(
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val error: Throwable? = null,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchWithWatchlistUseCase @Inject constructor(
    private val searchInstrumentsUseCase: SearchInstrumentsUseCase,
    private val observeWatchlistSymbolsUseCase: ObserveWatchlistSymbolsUseCase,
) {
    operator fun invoke(queries: Flow<String>): Flow<SearchWithWatchlistState> =
        combine(
            queryStates(queries),
            observeWatchlistSymbolsUseCase(),
        ) { queryState, watchlistSymbols ->
            queryState.toSearchWithWatchlistState(watchlistSymbols)
        }

    private fun queryStates(queries: Flow<String>): Flow<SearchQueryState> =
        queries
            .debounce(SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                when {
                    query.isBlank() -> flowOf(SearchQueryState.Idle)
                    else -> flow<SearchQueryState> {
                        emit(SearchQueryState.Loading)
                        emit(
                            searchInstrumentsUseCase(query.trim()).fold(
                                onSuccess = { SearchQueryState.Loaded(it) },
                                onFailure = { SearchQueryState.Failed(it) },
                            ),
                        )
                    }
                }
            }

    private sealed interface SearchQueryState {
        data object Idle : SearchQueryState

        data object Loading : SearchQueryState

        data class Loaded(val instruments: List<Instrument>) : SearchQueryState

        data class Failed(val error: Throwable) : SearchQueryState
    }

    private fun SearchQueryState.toSearchWithWatchlistState(
        watchlistSymbols: Set<String>,
    ): SearchWithWatchlistState = when (this) {
        SearchQueryState.Idle -> SearchWithWatchlistState()
        SearchQueryState.Loading -> SearchWithWatchlistState(
            isSearching = true,
            hasSearched = true,
        )
        is SearchQueryState.Loaded -> SearchWithWatchlistState(
            isSearching = false,
            hasSearched = true,
            results = instruments.map { instrument ->
                SearchResult(
                    instrument = instrument,
                    isInWatchlist = watchlistSymbols.contains(instrument.symbol),
                )
            },
        )
        is SearchQueryState.Failed -> SearchWithWatchlistState(
            isSearching = false,
            hasSearched = true,
            error = error,
        )
    }
}
