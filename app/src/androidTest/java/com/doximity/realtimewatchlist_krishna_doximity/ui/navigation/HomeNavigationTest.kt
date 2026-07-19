package com.doximity.realtimewatchlist_krishna_doximity.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.doximity.realtimewatchlist_krishna_doximity.CompactPhoneTestContent
import com.doximity.realtimewatchlist_krishna_doximity.ui.preview.PreviewSampleData
import com.doximity.realtimewatchlist_krishna_doximity.ui.search.SearchContent
import com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist.WatchlistContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun navigationSuite_showsWatchlistAndSearchTabs() {
        composeRule.setContent {
            CompactPhoneTestContent {
                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        homeNavigationItems(
                            selectedRoute = AppDestination.Watchlist.route,
                            onDestinationSelected = {},
                        )
                    },
                ) {}
            }
        }

        composeRule.onNodeWithText("Watchlist").assertIsDisplayed()
        composeRule.onNodeWithText("Search").assertIsDisplayed()
    }

    @Test
    fun navigationSuite_switchesSelectedDestination() {
        var selectedRoute = AppDestination.Watchlist.route

        composeRule.setContent {
            CompactPhoneTestContent {
                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        homeNavigationItems(
                            selectedRoute = selectedRoute,
                            onDestinationSelected = { selectedRoute = it.route },
                        )
                    },
                ) {}
            }
        }

        composeRule.onNodeWithText("Search").performClick()

        composeRule.runOnIdle {
            assertEquals(AppDestination.Search.route, selectedRoute)
        }
    }

    @Test
    fun homeShell_watchlistTab_showsWatchlistContent() {
        composeRule.setContent {
            CompactPhoneTestContent {
                HomeTestShell(selectedRoute = AppDestination.Watchlist.route)
            }
        }

        composeRule.onNodeWithText("AAPL").assertIsDisplayed()
        composeRule.onNodeWithText("Watchlist").assertIsSelected()
    }

    @Test
    fun homeShell_searchTab_showsSearchContent() {
        composeRule.setContent {
            CompactPhoneTestContent {
                HomeTestShell(selectedRoute = AppDestination.Search.route)
            }
        }

        composeRule.onNodeWithText("Search instruments").assertIsDisplayed()
        composeRule.onNodeWithText("MSFT").assertIsDisplayed()
        composeRule.onNodeWithText("Search").assertIsSelected()
    }

    @Test
    fun homeShell_tappingSearchTab_updatesContent() {
        composeRule.setContent {
            CompactPhoneTestContent {
                var selectedRoute by remember { mutableStateOf(AppDestination.Watchlist.route) }
                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        homeNavigationItems(
                            selectedRoute = selectedRoute,
                            onDestinationSelected = { selectedRoute = it.route },
                        )
                    },
                ) {
                    when (selectedRoute) {
                        AppDestination.Watchlist.route -> WatchlistContent(
                            uiState = PreviewSampleData.watchlistWithEntries,
                            onRemove = {},
                            onRefresh = {},
                            modifier = Modifier.fillMaxSize(),
                        )
                        AppDestination.Search.route -> SearchContent(
                            uiState = PreviewSampleData.searchResults,
                            onQueryChange = {},
                            onAdd = {},
                            modifier = Modifier.fillMaxSize(),
                        )
                        else -> Unit
                    }
                }
            }
        }

        composeRule.onNodeWithText("AAPL").assertIsDisplayed()
        composeRule.onNodeWithText("Search").performClick()
        composeRule.onNodeWithText("Search instruments").assertIsDisplayed()
        composeRule.onNodeWithText("BTC/USDT").assertIsDisplayed()
    }
}

@Composable
private fun HomeTestShell(selectedRoute: String) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            homeNavigationItems(
                selectedRoute = selectedRoute,
                onDestinationSelected = {},
            )
        },
    ) {
        when (selectedRoute) {
            AppDestination.Watchlist.route -> WatchlistContent(
                uiState = PreviewSampleData.watchlistWithEntries,
                onRemove = {},
                onRefresh = {},
                modifier = Modifier.fillMaxSize(),
            )
            AppDestination.Search.route -> SearchContent(
                uiState = PreviewSampleData.searchResults,
                onQueryChange = {},
                onAdd = {},
                modifier = Modifier.fillMaxSize(),
            )
            else -> Unit
        }
    }
}
