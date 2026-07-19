package com.doximity.realtimewatchlist_krishna_doximity.data.demo

import com.doximity.realtimewatchlist_krishna_doximity.domain.model.HistoricalPrices
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PricePoint
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Quote
import kotlin.math.abs
import kotlin.random.Random

object DemoHistoricalPrices {

    fun forSymbol(
        symbol: String,
        quote: Quote,
        days: Int = 30,
        nowMs: Long = System.currentTimeMillis(),
    ): HistoricalPrices {
        val dayMs = 24L * 60L * 60L * 1_000L
        val random = Random(symbol.hashCode())
        val driftPerDay = quote.percentChange / 100.0 / days.coerceAtLeast(1)
        val volatility = (abs(quote.percentChange) / 100.0).coerceIn(0.002, 0.03)

        var price = quote.currentPrice
        val pointsReversed = ArrayList<PricePoint>(days)
        for (dayOffset in 0 until days) {
            pointsReversed += PricePoint(
                timestampMs = nowMs - (dayOffset * dayMs),
                price = price.coerceAtLeast(0.01),
            )
            val step = price * (driftPerDay + random.nextDouble(-volatility, volatility))
            price = (price - step).coerceAtLeast(0.01)
        }

        return HistoricalPrices(
            symbol = symbol,
            points = pointsReversed.asReversed(),
        )
    }
}
