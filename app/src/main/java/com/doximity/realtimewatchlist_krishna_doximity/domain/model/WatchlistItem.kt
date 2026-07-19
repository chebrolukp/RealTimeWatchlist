package com.doximity.realtimewatchlist_krishna_doximity.domain.model

data class WatchlistItem(
    val symbol: String,
    val displaySymbol: String,
    val description: String,
    val type: String,
    val addedAtEpochMs: Long,
    val priceAlert: PriceAlert? = null,
)
