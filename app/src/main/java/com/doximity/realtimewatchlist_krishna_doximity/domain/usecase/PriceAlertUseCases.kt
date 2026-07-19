package com.doximity.realtimewatchlist_krishna_doximity.domain.usecase

import com.doximity.realtimewatchlist_krishna_doximity.domain.alert.PriceAlertEvaluator
import com.doximity.realtimewatchlist_krishna_doximity.domain.alert.PriceAlertNotifier
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlert
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlertDirection
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.WatchlistRepository
import javax.inject.Inject

class SetPriceAlertUseCase @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val priceAlertNotifier: PriceAlertNotifier,
) {
    /**
     * @param currentPrice if already in the trigger zone, fires once immediately and latches
     * so subsequent oscillation cannot re-alert.
     */
    suspend operator fun invoke(
        symbol: String,
        displaySymbol: String,
        threshold: Double,
        direction: PriceAlertDirection,
        currentPrice: Double? = null,
    ) {
        require(threshold > 0.0) { "Alert threshold must be positive" }
        val alert = PriceAlert(
            threshold = threshold,
            direction = direction,
            triggered = false,
        )
        if (currentPrice != null && PriceAlertEvaluator.isInTriggerZone(alert, currentPrice)) {
            watchlistRepository.setPriceAlert(symbol, alert.copy(triggered = true))
            priceAlertNotifier.notifyAlert(
                symbol = symbol,
                displaySymbol = displaySymbol,
                alert = alert,
                currentPrice = currentPrice,
            )
        } else {
            watchlistRepository.setPriceAlert(symbol, alert)
        }
    }
}

class ClearPriceAlertUseCase @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
) {
    suspend operator fun invoke(symbol: String) {
        watchlistRepository.clearPriceAlert(symbol)
    }
}
