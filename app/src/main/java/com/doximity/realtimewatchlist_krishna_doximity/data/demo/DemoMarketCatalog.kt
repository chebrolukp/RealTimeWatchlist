package com.doximity.realtimewatchlist_krishna_doximity.data.demo

import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Quote

object DemoMarketCatalog {
    val apple = Instrument("AAPL", "AAPL", "Apple Inc.", "Common Stock")
    val microsoft = Instrument("MSFT", "MSFT", "Microsoft Corp.", "Common Stock")
    val bitcoin = Instrument("BINANCE:BTCUSDT", "BTC/USDT", "Bitcoin / Tether", "Crypto")
    val euroUsd = Instrument("OANDA:EUR_USD", "EUR/USD", "Euro / US Dollar", "Forex")

    val instruments: List<Instrument> = listOf(apple, microsoft, bitcoin, euroUsd)

    val quotes: Map<String, Quote> = mapOf(
        apple.symbol to quote(
            currentPrice = 190.42,
            change = 2.15,
            percentChange = 1.14,
            previousClose = 188.27,
        ),
        microsoft.symbol to quote(
            currentPrice = 420.10,
            change = -1.80,
            percentChange = -0.43,
            previousClose = 421.90,
        ),
        bitcoin.symbol to quote(
            currentPrice = 67_250.0,
            change = 850.0,
            percentChange = 1.28,
            previousClose = 66_400.0,
        ),
        euroUsd.symbol to quote(
            currentPrice = 1.0842,
            change = 0.0015,
            percentChange = 0.14,
            previousClose = 1.0827,
        ),
    )

    fun search(query: String): List<Instrument> {
        val normalized = query.trim()
        if (normalized.isBlank()) return emptyList()
        return instruments.filter { instrument ->
            instrument.symbol.contains(normalized, ignoreCase = true) ||
                instrument.displaySymbol.contains(normalized, ignoreCase = true) ||
                instrument.description.contains(normalized, ignoreCase = true) ||
                instrument.type.contains(normalized, ignoreCase = true)
        }
    }

    fun quoteFor(symbol: String): Quote? = quotes[symbol]

    private fun quote(
        currentPrice: Double,
        change: Double,
        percentChange: Double,
        previousClose: Double,
    ): Quote = Quote(
        currentPrice = currentPrice,
        change = change,
        percentChange = percentChange,
        previousClose = previousClose,
        timestampSeconds = System.currentTimeMillis() / 1_000,
    )
}
