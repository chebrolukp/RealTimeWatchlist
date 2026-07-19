package com.doximity.realtimewatchlist_krishna_doximity.domain.alert

import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlert
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlertDirection

interface PriceAlertNotifier {
    fun notifyAlert(
        symbol: String,
        displaySymbol: String,
        alert: PriceAlert,
        currentPrice: Double,
    )
}

fun PriceAlertDirection.label(): String = when (this) {
    PriceAlertDirection.Above -> "above"
    PriceAlertDirection.Below -> "below"
}
