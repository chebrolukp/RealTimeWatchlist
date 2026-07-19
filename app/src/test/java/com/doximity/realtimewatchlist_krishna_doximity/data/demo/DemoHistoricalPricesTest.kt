package com.doximity.realtimewatchlist_krishna_doximity.data.demo

import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Quote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoHistoricalPricesTest {

    @Test
    fun forSymbol_returnsRequestedDayCountInChronologicalOrder() {
        val quote = Quote(
            currentPrice = 100.0,
            change = 1.0,
            percentChange = 1.0,
            previousClose = 99.0,
            timestampSeconds = 1_700_000_000L,
        )

        val history = DemoHistoricalPrices.forSymbol(
            symbol = "AAPL",
            quote = quote,
            days = 10,
            nowMs = 1_700_000_000_000L,
        )

        assertEquals("AAPL", history.symbol)
        assertEquals(10, history.points.size)
        assertTrue(history.points.zipWithNext().all { (left, right) -> left.timestampMs < right.timestampMs })
        assertEquals(quote.currentPrice, history.points.last().price, 0.0001)
    }

    @Test
    fun forSymbol_isDeterministicForSameInputs() {
        val quote = Quote(
            currentPrice = 50.0,
            change = -0.5,
            percentChange = -1.0,
            previousClose = 50.5,
            timestampSeconds = 1L,
        )

        val first = DemoHistoricalPrices.forSymbol("MSFT", quote, days = 5, nowMs = 1000L)
        val second = DemoHistoricalPrices.forSymbol("MSFT", quote, days = 5, nowMs = 1000L)

        assertEquals(first, second)
    }
}
