package com.doximity.realtimewatchlist_krishna_doximity.ui.search

import androidx.compose.runtime.Composable
import com.doximity.realtimewatchlist_krishna_doximity.ui.preview.PreviewSampleData
import com.doximity.realtimewatchlist_krishna_doximity.ui.preview.ScreenPreview
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.RealtimeWatchListKrishnaDoximityTheme

@ScreenPreview
@Composable
private fun SearchScreenIdlePreview() {
    RealtimeWatchListKrishnaDoximityTheme {
        SearchContent(
            uiState = PreviewSampleData.searchIdle,
            onQueryChange = {},
            onAdd = {},
        )
    }
}

@ScreenPreview
@Composable
private fun SearchScreenLoadingPreview() {
    RealtimeWatchListKrishnaDoximityTheme {
        SearchContent(
            uiState = PreviewSampleData.searchLoading,
            onQueryChange = {},
            onAdd = {},
        )
    }
}

@ScreenPreview
@Composable
private fun SearchScreenResultsPreview() {
    RealtimeWatchListKrishnaDoximityTheme {
        SearchContent(
            uiState = PreviewSampleData.searchResults,
            onQueryChange = {},
            onAdd = {},
        )
    }
}

@ScreenPreview
@Composable
private fun SearchScreenEmptyPreview() {
    RealtimeWatchListKrishnaDoximityTheme {
        SearchContent(
            uiState = PreviewSampleData.searchEmpty,
            onQueryChange = {},
            onAdd = {},
        )
    }
}

@ScreenPreview
@Composable
private fun SearchScreenErrorPreview() {
    RealtimeWatchListKrishnaDoximityTheme {
        SearchContent(
            uiState = PreviewSampleData.searchError,
            onQueryChange = {},
            onAdd = {},
        )
    }
}
