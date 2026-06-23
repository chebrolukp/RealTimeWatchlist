package com.doximity.realtimewatchlist_krishna_doximity.data.demo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoMarketCatalogTest {

    @Test
    fun search_byTicker_returnsMatchingInstrument() {
        val results = DemoMarketCatalog.search("AAPL")

        assertEquals(1, results.size)
        assertEquals("AAPL", results.single().symbol)
    }

    @Test
    fun search_byCompanyName_returnsMatchingInstrument() {
        val results = DemoMarketCatalog.search("Microsoft")

        assertEquals(1, results.size)
        assertEquals("MSFT", results.single().symbol)
    }

    @Test
    fun search_byAssetType_returnsMultipleInstruments() {
        val results = DemoMarketCatalog.search("Crypto")

        assertEquals(1, results.size)
        assertEquals("BINANCE:BTCUSDT", results.single().symbol)
    }

    @Test
    fun search_unknownQuery_returnsEmptyList() {
        assertTrue(DemoMarketCatalog.search("ZZZZZ").isEmpty())
    }

    @Test
    fun search_blankQuery_returnsEmptyList() {
        assertTrue(DemoMarketCatalog.search("   ").isEmpty())
    }

    @Test
    fun quoteFor_knownSymbol_returnsPrice() {
        val quote = DemoMarketCatalog.quoteFor("AAPL")

        assertNotNull(quote)
        assertTrue(quote!!.hasPrice)
    }

    @Test
    fun quoteFor_unknownSymbol_returnsNull() {
        assertEquals(null, DemoMarketCatalog.quoteFor("UNKNOWN"))
    }

    @Test
    fun instruments_containsExpectedSymbols() {
        val symbols = DemoMarketCatalog.instruments.map { it.symbol }

        assertTrue(symbols.contains("AAPL"))
        assertTrue(symbols.contains("MSFT"))
        assertTrue(symbols.contains("BINANCE:BTCUSDT"))
        assertFalse(symbols.contains("UNKNOWN"))
    }
}
