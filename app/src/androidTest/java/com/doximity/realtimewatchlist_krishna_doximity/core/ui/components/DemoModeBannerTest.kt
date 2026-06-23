package com.doximity.realtimewatchlist_krishna_doximity.core.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.doximity.realtimewatchlist_krishna_doximity.CompactPhoneTestContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DemoModeBannerTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun demoModeBanner_showsMessage() {
        composeRule.setContent {
            CompactPhoneTestContent {
                DemoModeBanner()
            }
        }

        composeRule.onNodeWithText(
            "Demo mode — sample data only. Add FINNHUB_API_KEY to local.properties for live Finnhub data.",
        ).assertIsDisplayed()
    }
}
