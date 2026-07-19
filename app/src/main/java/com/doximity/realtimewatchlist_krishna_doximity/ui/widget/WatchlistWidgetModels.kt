package com.doximity.realtimewatchlist_krishna_doximity.ui.widget

data class WatchlistWidgetItem(
    val symbol: String,
    val displaySymbol: String,
    val price: Double?,
    val change: Double?,
    val percentChange: Double?,
)

data class WatchlistWidgetState(
    val items: List<WatchlistWidgetItem>,
    val isDemoMode: Boolean,
)
