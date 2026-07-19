package com.doximity.realtimewatchlist_krishna_doximity.domain.alert

import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlert
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlertDirection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceAlertEvaluatorTest {

    @Test
    fun below_firesOnceOnCrossIntoZone() {
        val alert = PriceAlert(threshold = 60.0, direction = PriceAlertDirection.Below)
        assertTrue(PriceAlertEvaluator.shouldFire(alert, previousPrice = 60.01, currentPrice = 59.99))
    }

    @Test
    fun below_doesNotFireWhileAlreadyInZone() {
        val alert = PriceAlert(threshold = 60.0, direction = PriceAlertDirection.Below)
        assertFalse(PriceAlertEvaluator.shouldFire(alert, previousPrice = 59.99, currentPrice = 59.50))
    }

    @Test
    fun below_doesNotFireOnOscillationAfterLatch() {
        val alert = PriceAlert(
            threshold = 60.0,
            direction = PriceAlertDirection.Below,
            triggered = true,
        )
        assertFalse(PriceAlertEvaluator.shouldFire(alert, previousPrice = 60.01, currentPrice = 59.99))
        assertFalse(PriceAlertEvaluator.shouldFire(alert, previousPrice = 59.99, currentPrice = 60.01))
        assertFalse(PriceAlertEvaluator.shouldFire(alert, previousPrice = 60.01, currentPrice = 59.99))
    }

    @Test
    fun above_firesOnCrossIntoZone() {
        val alert = PriceAlert(threshold = 60.0, direction = PriceAlertDirection.Above)
        assertTrue(PriceAlertEvaluator.shouldFire(alert, previousPrice = 59.99, currentPrice = 60.01))
    }

    @Test
    fun above_ignoresFlappingAroundThresholdWithoutCrossFromSafeSide() {
        val alert = PriceAlert(threshold = 60.0, direction = PriceAlertDirection.Above)
        // Staying above threshold after already being above is not a fresh cross.
        assertFalse(PriceAlertEvaluator.shouldFire(alert, previousPrice = 60.01, currentPrice = 60.50))
        assertFalse(PriceAlertEvaluator.shouldFire(alert, previousPrice = 60.50, currentPrice = 60.01))
    }

    @Test
    fun requiresPreviousPrice() {
        val alert = PriceAlert(threshold = 60.0, direction = PriceAlertDirection.Below)
        assertFalse(PriceAlertEvaluator.shouldFire(alert, previousPrice = null, currentPrice = 59.0))
    }
}
