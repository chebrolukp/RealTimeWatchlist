package com.doximity.realtimewatchlist_krishna_doximity.data.repository

import com.doximity.realtimewatchlist_krishna_doximity.BuildConfig
import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.ConnectionState
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.FinnhubApi
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.dto.toDomain
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.requireFinnhubBody
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceUpdate
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Quote
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinnhubMarketDataRepository @Inject constructor(
    private val finnhubApi: FinnhubApi,
) : MarketDataRepository {

    override val isDemoMode: Boolean = BuildConfig.DEMO_MODE

    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    override val priceUpdates: Flow<PriceUpdate> = emptyFlow()

    override suspend fun searchInstruments(query: String): Result<List<Instrument>> =
        runCatching {
            if (query.isBlank()) return@runCatching emptyList()
            if (isDemoMode) {
                return@runCatching demoInstruments(query)
            }
            finnhubApi.searchSymbols(query).requireFinnhubBody().result.map { it.toDomain() }
        }

    override suspend fun getQuote(symbol: String): Result<Quote> =
        runCatching {
            if (isDemoMode) {
                return@runCatching demoQuote(symbol)
            }
            finnhubApi.getQuote(symbol).requireFinnhubBody().toDomain()
        }

    override fun updateLiveSubscriptions(symbols: Set<String>) {
        // Not implemented for now
    }

    private fun demoInstruments(query: String): List<Instrument> {
        val normalizedQuery = query.trim().lowercase()
        return DEMO_INSTRUMENTS.filter { instrument ->
            instrument.symbol.lowercase().contains(normalizedQuery) ||
                instrument.displaySymbol.lowercase().contains(normalizedQuery) ||
                instrument.description.lowercase().contains(normalizedQuery)
        }
    }

    private fun demoQuote(symbol: String): Quote {
        val instrument = DEMO_INSTRUMENTS.firstOrNull {
            it.symbol.equals(symbol, ignoreCase = true)
        }
        val price = when (instrument?.symbol?.uppercase()) {
            "AAPL" -> 190.0
            "BTC" -> 65000.0
            "EUR/USD" -> 1.08
            else -> 100.0
        }
        return Quote(
            currentPrice = price,
            change = 1.25,
            percentChange = 0.66,
            previousClose = price - 1.25,
            timestampSeconds = System.currentTimeMillis() / 1000,
        )
    }

    private companion object {
        val DEMO_INSTRUMENTS = listOf(
            Instrument("AAPL", "AAPL", "Apple Inc", "Common Stock"),
            Instrument("BTC", "BTC", "Bitcoin", "Crypto"),
            Instrument("EUR/USD", "EUR/USD", "Euro / US Dollar", "Forex"),
        )
    }
}
