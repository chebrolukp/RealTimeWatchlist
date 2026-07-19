package com.doximity.realtimewatchlist_krishna_doximity.core.error

import com.doximity.realtimewatchlist_krishna_doximity.R
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.model.UiText
import org.junit.Assert.assertEquals
import org.junit.Test

class ThrowableToUiTextTest {

    @Test
    fun unauthorized_mapsToUnauthorizedMessage() {
        assertEquals(
            UiText.Resource(R.string.error_finnhub_unauthorized),
            FinnhubException.Unauthorized().toUiText(),
        )
    }

    @Test
    fun forbidden_mapsToForbiddenMessage() {
        assertEquals(
            UiText.Resource(R.string.error_finnhub_forbidden),
            FinnhubException.Forbidden().toUiText(),
        )
    }

    @Test
    fun rateLimit_mapsToRateLimitMessage() {
        assertEquals(
            UiText.Resource(R.string.error_finnhub_rate_limit),
            FinnhubException.RateLimit().toUiText(),
        )
    }

    @Test
    fun emptyBodyApi_mapsToEmptyResponseMessage() {
        assertEquals(
            UiText.Resource(R.string.error_finnhub_empty_response),
            FinnhubException.Api(isEmptyBody = true).toUiText(),
        )
    }

    @Test
    fun httpApi_mapsToRequestFailedMessage() {
        assertEquals(
            UiText.Resource(R.string.error_finnhub_request_failed, listOf(500)),
            FinnhubException.Api(httpCode = 500).toUiText(),
        )
    }

    @Test
    fun blankThrowable_mapsToGenericMessage() {
        assertEquals(
            UiText.Resource(R.string.error_generic),
            RuntimeException("   ").toUiText(),
        )
    }

    @Test
    fun messageThrowable_mapsToDynamicText() {
        assertEquals(
            UiText.Dynamic("Something custom"),
            RuntimeException("Something custom").toUiText(),
        )
    }
}
