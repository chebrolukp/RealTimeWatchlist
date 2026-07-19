package com.doximity.realtimewatchlist_krishna_doximity.domain.alert

import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlert
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlertDirection

/**
 * Decides whether a live price should fire a one-shot alert.
 *
 * Rules:
 * - Already-triggered alerts never fire again.
 * - Fire only when price **crosses into** the alert zone (previous on the safe side,
 *   current on the trigger side). That blocks chatter like 59.99 ↔ 60.01 around 60
 *   after the first crossing has already fired and latched.
 */
object PriceAlertEvaluator {

    fun isInTriggerZone(alert: PriceAlert, price: Double): Boolean = when (alert.direction) {
        PriceAlertDirection.Above -> price >= alert.threshold
        PriceAlertDirection.Below -> price <= alert.threshold
    }

    fun shouldFire(
        alert: PriceAlert,
        previousPrice: Double?,
        currentPrice: Double,
    ): Boolean {
        if (alert.triggered) return false
        if (previousPrice == null) return false
        if (!isInTriggerZone(alert, currentPrice)) return false
        // Require a real cross from the safe side into the trigger zone.
        return !isInTriggerZone(alert, previousPrice)
    }
}
