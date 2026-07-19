package com.doximity.realtimewatchlist_krishna_doximity.domain.model

data class PricePoint(
    val timestampMs: Long,
    val price: Double,
)

data class HistoricalPrices(
    val symbol: String,
    val points: List<PricePoint>,
)
