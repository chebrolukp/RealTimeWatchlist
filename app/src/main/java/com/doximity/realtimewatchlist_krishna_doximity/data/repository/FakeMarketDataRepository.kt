package com.doximity.realtimewatchlist_krishna_doximity.data.repository

import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.ConnectionState
import com.doximity.realtimewatchlist_krishna_doximity.data.demo.DemoMarketCatalog
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceUpdate
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Quote
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FakeMarketDataRepository @Inject constructor(
    @Named("applicationScope") private val applicationScope: CoroutineScope,
) : MarketDataRepository {

    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _priceUpdates = MutableSharedFlow<PriceUpdate>(extraBufferCapacity = 128)
    override val priceUpdates: Flow<PriceUpdate> = _priceUpdates.asSharedFlow()

    private val subscribedSymbols = linkedSetOf<String>()
    private val livePrices = mutableMapOf<String, Double>()
    private var tickerJob: Job? = null

    override suspend fun searchInstruments(query: String): Result<List<Instrument>> =
        runCatching {
            delay(SEARCH_DELAY_MS)
            DemoMarketCatalog.search(query)
        }

    override suspend fun getQuote(symbol: String): Result<Quote> =
        runCatching {
            delay(QUOTE_DELAY_MS)
            DemoMarketCatalog.quoteFor(symbol)
                ?: throw DemoQuoteUnavailableException(symbol)
        }

    override fun updateLiveSubscriptions(symbols: Set<String>) {
        subscribedSymbols.clear()
        subscribedSymbols.addAll(symbols)

        if (symbols.isEmpty()) {
            stopTicker()
            _connectionState.value = ConnectionState.Disconnected
            livePrices.clear()
            return
        }

        _connectionState.value = ConnectionState.Connected
        symbols.forEach { symbol ->
            DemoMarketCatalog.quoteFor(symbol)?.currentPrice?.let { price ->
                livePrices[symbol] = price
            }
        }
        startTicker()
    }

    suspend fun emitPriceUpdate(update: PriceUpdate) {
        _priceUpdates.emit(update)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = applicationScope.launch {
            while (isActive && subscribedSymbols.isNotEmpty()) {
                delay(TICK_INTERVAL_MS)
                emitSimulatedUpdates()
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private suspend fun emitSimulatedUpdates() {
        subscribedSymbols.forEach { symbol ->
            val basePrice = livePrices[symbol] ?: DemoMarketCatalog.quoteFor(symbol)?.currentPrice
                ?: return@forEach
            val jitter = basePrice * Random.nextDouble(-JITTER_PERCENT, JITTER_PERCENT)
            val nextPrice = (basePrice + jitter).coerceAtLeast(MIN_PRICE)
            livePrices[symbol] = nextPrice
            _priceUpdates.emit(
                PriceUpdate(
                    symbol = symbol,
                    price = nextPrice,
                    timestampMs = System.currentTimeMillis(),
                ),
            )
        }
    }

    private class DemoQuoteUnavailableException(symbol: String) :
        Exception("Demo quote unavailable for $symbol")

    private companion object {
        const val SEARCH_DELAY_MS = 150L
        const val QUOTE_DELAY_MS = 100L
        const val TICK_INTERVAL_MS = 2_000L
        const val JITTER_PERCENT = 0.005
        const val MIN_PRICE = 0.01
    }
}
