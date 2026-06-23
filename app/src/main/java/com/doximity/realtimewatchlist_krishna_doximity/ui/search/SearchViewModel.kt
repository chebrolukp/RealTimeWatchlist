package com.doximity.realtimewatchlist_krishna_doximity.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.model.UiText
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.util.toUiText
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.SearchResult
import com.doximity.realtimewatchlist_krishna_doximity.domain.usecase.AddToWatchlistUseCase
import com.doximity.realtimewatchlist_krishna_doximity.domain.usecase.SearchWithWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val errorMessage: UiText? = null,
    val hasSearched: Boolean = false,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    searchWithWatchlistUseCase: SearchWithWatchlistUseCase,
    private val addToWatchlistUseCase: AddToWatchlistUseCase,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = combine(
        searchQuery,
        searchWithWatchlistUseCase(searchQuery),
    ) { query, searchState ->
        SearchUiState(
            query = query,
            isSearching = searchState.isSearching,
            results = searchState.results,
            errorMessage = searchState.error?.toUiText(),
            hasSearched = searchState.hasSearched,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState(),
    )

    fun onQueryChange(query: String) {
        searchQuery.value = query
    }

    fun addToWatchlist(instrument: Instrument) {
        viewModelScope.launch {
            addToWatchlistUseCase(instrument)
        }
    }
}
