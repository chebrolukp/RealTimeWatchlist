package com.doximity.realtimewatchlist_krishna_doximity.data.repository

import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.ConnectionState
import com.doximity.realtimewatchlist_krishna_doximity.core.error.FinnhubException
import com.doximity.realtimewatchlist_krishna_doximity.data.demo.DemoHistoricalPrices
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.CandleMarket
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.FinnhubApi
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.dto.toDomain
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.dto.toHistoricalPrices
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.requireFinnhubBody
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.resolveCandleMarket
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.websocket.FinnhubWebSocketClient
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.HistoricalPrices
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceUpdate
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Quote
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinnhubMarketDataRepository @Inject constructor(
    private val finnhubApi: FinnhubApi,
    private val webSocketClient: FinnhubWebSocketClient,
) : MarketDataRepository {

    override val connectionState: StateFlow<ConnectionState> =
        webSocketClient.connectionState

    override val priceUpdates: Flow<PriceUpdate> =
        webSocketClient.priceUpdates

    override suspend fun searchInstruments(query: String): Result<List<Instrument>> =
        runCatching {
            if (query.isBlank()) return@runCatching emptyList()
            finnhubApi.searchSymbols(query).requireFinnhubBody().result.map { it.toDomain() }
        }

    override suspend fun getQuote(symbol: String): Result<Quote> =
        runCatching { finnhubApi.getQuote(symbol).requireFinnhubBody().toDomain() }

    override suspend fun getHistoricalPrices(
        symbol: String,
        instrumentType: String,
        days: Int,
    ): Result<HistoricalPrices> = runCatching {
        val toUnixSeconds = System.currentTimeMillis() / 1_000L
        val fromUnixSeconds = toUnixSeconds - (days.coerceAtLeast(1) * SECONDS_PER_DAY)
        val market = resolveCandleMarket(instrumentType, symbol)

        try {
            val candles = when (market) {
                CandleMarket.Stock -> finnhubApi.getStockCandles(
                    symbol = symbol,
                    resolution = RESOLUTION_DAILY,
                    fromUnixSeconds = fromUnixSeconds,
                    toUnixSeconds = toUnixSeconds,
                )
                CandleMarket.Crypto -> finnhubApi.getCryptoCandles(
                    symbol = symbol,
                    resolution = RESOLUTION_DAILY,
                    fromUnixSeconds = fromUnixSeconds,
                    toUnixSeconds = toUnixSeconds,
                )
                CandleMarket.Forex -> finnhubApi.getForexCandles(
                    symbol = symbol,
                    resolution = RESOLUTION_DAILY,
                    fromUnixSeconds = fromUnixSeconds,
                    toUnixSeconds = toUnixSeconds,
                )
            }.requireFinnhubBody().toHistoricalPrices(symbol)

            if (candles.points.isNotEmpty()) {
                candles
            } else {
                // Free Finnhub plans often omit stock candles; fall back so charts still render.
                syntheticHistoryFromQuote(symbol, days)
            }
        } catch (_: FinnhubException.Forbidden) {
            syntheticHistoryFromQuote(symbol, days)
        }
    }

    override fun updateLiveSubscriptions(symbols: Set<String>) {
        if (symbols.isEmpty()) {
            webSocketClient.stop()
        } else {
            webSocketClient.updateSubscriptions(symbols)
        }
    }

    private suspend fun syntheticHistoryFromQuote(symbol: String, days: Int): HistoricalPrices {
        val quote = finnhubApi.getQuote(symbol).requireFinnhubBody().toDomain()
        return DemoHistoricalPrices.forSymbol(symbol = symbol, quote = quote, days = days)
    }

    private companion object {
        const val RESOLUTION_DAILY = "D"
        const val SECONDS_PER_DAY = 24L * 60L * 60L
    }
}
