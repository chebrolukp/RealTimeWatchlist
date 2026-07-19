package com.doximity.realtimewatchlist_krishna_doximity.data.local

import com.doximity.realtimewatchlist_krishna_doximity.data.local.entity.WatchlistEntity
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlert
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlertDirection
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.WatchlistItem

fun WatchlistEntity.toDomain(): WatchlistItem = WatchlistItem(
    symbol = symbol,
    displaySymbol = displaySymbol,
    description = description,
    type = type,
    addedAtEpochMs = addedAtEpochMs,
    priceAlert = toPriceAlert(),
)

fun Instrument.toEntity(addedAtEpochMs: Long): WatchlistEntity = WatchlistEntity(
    symbol = symbol,
    displaySymbol = displaySymbol,
    description = description,
    type = type,
    addedAtEpochMs = addedAtEpochMs,
)

private fun WatchlistEntity.toPriceAlert(): PriceAlert? {
    val threshold = alertThreshold ?: return null
    val direction = when (alertDirection) {
        "ABOVE" -> PriceAlertDirection.Above
        "BELOW" -> PriceAlertDirection.Below
        else -> return null
    }
    return PriceAlert(
        threshold = threshold,
        direction = direction,
        triggered = alertTriggered,
    )
}

fun PriceAlertDirection.toStorage(): String = when (this) {
    PriceAlertDirection.Above -> "ABOVE"
    PriceAlertDirection.Below -> "BELOW"
}
