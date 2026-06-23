@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.doximity.realtimewatchlist_krishna_doximity.data.repository

import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.ConnectionState
import com.doximity.realtimewatchlist_krishna_doximity.data.demo.DemoMarketCatalog
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeMarketDataRepositoryTest {

    private val dispatcher = StandardTestDispatcher()

    private fun runRepositoryTest(
        testBody: suspend TestScope.(FakeMarketDataRepository) -> Unit,
    ) = runTest(dispatcher) {
        val applicationScope = CoroutineScope(SupervisorJob() + dispatcher)
        val repository = FakeMarketDataRepository(applicationScope)
        try {
            testBody(repository)
        } finally {
            repository.updateLiveSubscriptions(emptySet())
            applicationScope.cancel()
        }
    }

    @Test
    fun searchInstruments_returnsCatalogMatches() = runRepositoryTest { repository ->
        val result = repository.searchInstruments("apple")
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals("AAPL", result.getOrThrow().single().symbol)
    }

    @Test
    fun getQuote_knownSymbol_returnsQuote() = runRepositoryTest { repository ->
        val result = repository.getQuote("MSFT")
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(DemoMarketCatalog.quotes["MSFT"]?.currentPrice, result.getOrThrow().currentPrice)
    }

    @Test
    fun getQuote_unknownSymbol_returnsFailure() = runRepositoryTest { repository ->
        val result = repository.getQuote("UNKNOWN")
        advanceUntilIdle()

        assertTrue(result.isFailure)
    }

    @Test
    fun updateLiveSubscriptions_withSymbols_setsConnected() = runRepositoryTest { repository ->
        repository.updateLiveSubscriptions(setOf("AAPL"))

        assertEquals(ConnectionState.Connected, repository.connectionState.value)
    }

    @Test
    fun updateLiveSubscriptions_empty_setsDisconnected() = runRepositoryTest { repository ->
        repository.updateLiveSubscriptions(setOf("AAPL"))
        runCurrent()

        repository.updateLiveSubscriptions(emptySet())

        assertEquals(ConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun emitPriceUpdate_forwardsToPriceUpdatesFlow() = runRepositoryTest { repository ->
        var received: PriceUpdate? = null
        val collectJob = backgroundScope.launch {
            received = repository.priceUpdates.first()
        }
        runCurrent()

        repository.emitPriceUpdate(
            PriceUpdate(
                symbol = "AAPL",
                price = 195.0,
                timestampMs = 1L,
            ),
        )
        runCurrent()

        assertEquals(195.0, received!!.price, 0.0)
        collectJob.cancel()
    }
}
