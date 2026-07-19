package com.doximity.realtimewatchlist_krishna_doximity.domain.model

enum class PriceAlertDirection {
    Above,
    Below,
}

/**
 * One-shot price alert for a watchlist item.
 *
 * Once [triggered] is true the alert stays silent until the user clears or resets it,
 * so oscillation around the threshold cannot spam notifications.
 */
data class PriceAlert(
    val threshold: Double,
    val direction: PriceAlertDirection,
    val triggered: Boolean = false,
)
