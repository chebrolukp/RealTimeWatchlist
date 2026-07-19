package com.doximity.realtimewatchlist_krishna_doximity.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.doximity.realtimewatchlist_krishna_doximity.ui.preview.PreviewSampleData
import com.doximity.realtimewatchlist_krishna_doximity.ui.preview.ScreenPreview
import com.doximity.realtimewatchlist_krishna_doximity.ui.search.SearchContent
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.RealtimeWatchListKrishnaDoximityTheme
import com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist.WatchlistContent

@ScreenPreview
@Composable
private fun HomeScreenWatchlistTabPreview() {
    RealtimeWatchListKrishnaDoximityTheme {
        HomeScreenPreviewShell(selectedRoute = AppDestination.Watchlist.route) {
            WatchlistContent(
                uiState = PreviewSampleData.watchlistWithEntries,
                onRemove = {},
                onRefresh = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun HomeScreenSearchTabPreview() {
    RealtimeWatchListKrishnaDoximityTheme {
        HomeScreenPreviewShell(selectedRoute = AppDestination.Search.route) {
            SearchContent(
                uiState = PreviewSampleData.searchResults,
                onQueryChange = {},
                onAdd = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun HomeScreenPreviewShell(
    selectedRoute: String,
    content: @Composable () -> Unit,
) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            homeNavigationItems(
                selectedRoute = selectedRoute,
                onDestinationSelected = {},
            )
        },
    ) {
        content()
    }
}
