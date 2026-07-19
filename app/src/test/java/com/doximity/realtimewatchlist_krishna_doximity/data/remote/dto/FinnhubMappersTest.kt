package com.doximity.realtimewatchlist_krishna_doximity.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinnhubMappersTest {

    @Test
    fun candleResponse_okStatus_mapsClosePrices() {
        val dto = CandleResponseDto(
            closePrices = listOf(10.0, 11.0, 12.5),
            timestampsSeconds = listOf(100L, 200L, 300L),
            status = "ok",
        )

        val history = dto.toHistoricalPrices("AAPL")

        assertEquals("AAPL", history.symbol)
        assertEquals(3, history.points.size)
        assertEquals(12.5, history.points.last().price, 0.0)
        assertEquals(300_000L, history.points.last().timestampMs)
    }

    @Test
    fun candleResponse_noDataStatus_returnsEmptyPoints() {
        val dto = CandleResponseDto(
            closePrices = listOf(10.0),
            timestampsSeconds = listOf(100L),
            status = "no_data",
        )

        val history = dto.toHistoricalPrices("AAPL")

        assertTrue(history.points.isEmpty())
    }
}
