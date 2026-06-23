package com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.doximity.realtimewatchlist_krishna_doximity.CompactPhoneTestContent
import com.doximity.realtimewatchlist_krishna_doximity.ui.preview.PreviewSampleData
import org.junit.Assert.assertEquals
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
}
