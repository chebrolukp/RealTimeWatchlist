package com.doximity.realtimewatchlist_krishna_doximity.domain.model

data class PriceUpdate(
    val symbol: String,
    val price: Double,
    val timestampMs: Long,
)
