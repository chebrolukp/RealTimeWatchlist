package com.doximity.realtimewatchlist_krishna_doximity.data.remote

enum class CandleMarket {
    Stock,
    Crypto,
    Forex,
}

fun resolveCandleMarket(instrumentType: String, symbol: String): CandleMarket {
    val type = instrumentType.lowercase()
    val normalizedSymbol = symbol.uppercase()
    return when {
        type.contains("crypto") ||
            normalizedSymbol.contains("BINANCE:") ||
            normalizedSymbol.contains("COINBASE:") ||
            normalizedSymbol.contains("KRAKEN:") -> CandleMarket.Crypto

        type.contains("forex") ||
            normalizedSymbol.contains("OANDA:") ||
            normalizedSymbol.contains("FX:") -> CandleMarket.Forex

        else -> CandleMarket.Stock
    }
}
