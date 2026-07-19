package com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.doximity.realtimewatchlist_krishna_doximity.CompactPhoneTestContent
import com.doximity.realtimewatchlist_krishna_doximity.ui.preview.PreviewSampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchlistScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun watchlistEmpty_showsEmptyState() {
        composeRule.setContent {
            CompactPhoneTestContent {
                WatchlistContent(
                    uiState = PreviewSampleData.watchlistEmpty,
                    onRemove = {},
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithText("Watchlist is empty").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Search for stocks, crypto, or forex and add symbols to track live prices.",
        ).assertIsDisplayed()
    }

    @Test
    fun watchlistWithEntries_displaysSymbols() {
        composeRule.setContent {
            CompactPhoneTestContent {
                WatchlistContent(
                    uiState = PreviewSampleData.watchlistWithEntries,
                    onRemove = {},
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithText("AAPL").assertIsDisplayed()
        composeRule.onNodeWithText("MSFT").assertIsDisplayed()
        composeRule.onNodeWithText("BTC/USDT").assertIsDisplayed()
    }

    @Test
    fun watchlistWithEntries_showsConnectionBanner() {
        composeRule.setContent {
            CompactPhoneTestContent {
                WatchlistContent(
                    uiState = PreviewSampleData.watchlistWithEntries,
                    onRemove = {},
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithText("Stream: Live", substring = true).assertIsDisplayed()
    }

    @Test
    fun watchlistWithEntries_showsHistoricalChartInDetail() {
        composeRule.setContent {
            CompactPhoneTestContent {
                WatchlistContent(
                    uiState = PreviewSampleData.watchlistWithEntries,
                    onRemove = {},
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithText("AAPL").performClick()
        composeRule.onNodeWithText("30-day history").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("30-day chart for AAPL trending up")
            .assertIsDisplayed()
    }

    @Test
    fun watchlistWithEntries_removeButton_invokesCallback() {
        var removedSymbol: String? = null

        composeRule.setContent {
            CompactPhoneTestContent {
                WatchlistContent(
                    uiState = PreviewSampleData.watchlistWithEntries,
                    onRemove = { removedSymbol = it },
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Remove AAPL from watchlist")
            .performClick()

        assertEquals("AAPL", removedSymbol)
    }

    @Test
    fun watchlistLoading_showsLoadingIndicator() {
        composeRule.setContent {
            CompactPhoneTestContent {
                WatchlistContent(
                    uiState = PreviewSampleData.watchlistLoading,
                    onRemove = {},
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Loading watchlist").assertIsDisplayed()
    }

    @Test
    fun watchlistPaginated_showsPageControls() {
        composeRule.setContent {
            CompactPhoneTestContent {
                WatchlistContent(
                    uiState = PreviewSampleData.watchlistPaginatedPage1,
                    onRemove = {},
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithText("Page 1 of 2").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Previous page").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Next page").assertIsEnabled()
        composeRule.onNodeWithText("SYM0").assertIsDisplayed()
        composeRule.onNodeWithText("SYM5").assertDoesNotExist()
    }

    @Test
    fun watchlistPaginated_nextPage_invokesCallback() {
        var nextClicked = false

        composeRule.setContent {
            CompactPhoneTestContent {
                WatchlistContent(
                    uiState = PreviewSampleData.watchlistPaginatedPage1,
                    onRemove = {},
                    onRefresh = {},
                    onNextPage = { nextClicked = true },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Next page").performClick()

        assertTrue(nextClicked)
    }

    @Test
    fun watchlistPaginatedPage2_previousPage_invokesCallback() {
        var previousClicked = false

        composeRule.setContent {
            CompactPhoneTestContent {
                WatchlistContent(
                    uiState = PreviewSampleData.watchlistPaginatedPage2,
                    onRemove = {},
                    onRefresh = {},
                    onPreviousPage = { previousClicked = true },
                )
            }
        }

        composeRule.onNodeWithText("Page 2 of 2").assertIsDisplayed()
        composeRule.onNodeWithText("SYM5").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Next page").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Previous page").performClick()

        assertTrue(previousClicked)
    }

    @Test
    fun watchlistShortList_hidesPaginationControls() {
        composeRule.setContent {
            CompactPhoneTestContent {
                WatchlistContent(
                    uiState = PreviewSampleData.watchlistWithEntries,
                    onRemove = {},
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithText("Page 1 of 1").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Next page").assertDoesNotExist()
    }
}
