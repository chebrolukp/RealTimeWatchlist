package com.doximity.realtimewatchlist_krishna_doximity.domain.model

data class Quote(
    val currentPrice: Double,
    val change: Double,
    val percentChange: Double,
    val previousClose: Double,
    val timestampSeconds: Long,
) {
    val hasPrice: Boolean get() = currentPrice > 0.0
}
