package com.doximity.realtimewatchlist_krishna_doximity.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class CandleMarketTest {

    @Test
    fun resolveCandleMarket_detectsCryptoFromType() {
        assertEquals(
            CandleMarket.Crypto,
            resolveCandleMarket(instrumentType = "Crypto", symbol = "BTC"),
        )
    }

    @Test
    fun resolveCandleMarket_detectsForexFromSymbolPrefix() {
        assertEquals(
            CandleMarket.Forex,
            resolveCandleMarket(instrumentType = "", symbol = "OANDA:EUR_USD"),
        )
    }

    @Test
    fun resolveCandleMarket_defaultsToStock() {
        assertEquals(
            CandleMarket.Stock,
            resolveCandleMarket(instrumentType = "Common Stock", symbol = "AAPL"),
        )
    }
}
