package com.doximity.realtimewatchlist_krishna_doximity.core.error

import com.doximity.realtimewatchlist_krishna_doximity.R
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.model.UiText

fun Throwable.toUiText(): UiText = when (this) {
    is FinnhubException.Unauthorized -> UiText.Resource(R.string.error_finnhub_unauthorized)
    is FinnhubException.Forbidden -> UiText.Resource(R.string.error_finnhub_forbidden)
    is FinnhubException.RateLimit -> UiText.Resource(R.string.error_finnhub_rate_limit)
    is FinnhubException.Api -> when {
        isEmptyBody -> UiText.Resource(R.string.error_finnhub_empty_response)
        httpCode != null -> UiText.Resource(R.string.error_finnhub_request_failed, listOf(httpCode))
        else -> UiText.Resource(R.string.error_generic)
    }
    else -> message?.takeIf { it.isNotBlank() }?.let(UiText::Dynamic)
        ?: UiText.Resource(R.string.error_generic)
}
