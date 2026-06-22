package com.doximity.realtimewatchlist_krishna_doximity.data.remote.dto

import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceUpdate
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Quote

fun SymbolSearchResultDto.toDomain(): Instrument = Instrument(
    symbol = symbol,
    displaySymbol = displaySymbol.ifBlank { symbol },
    description = description,
    type = type,
)

fun QuoteResponseDto.toDomain(): Quote = Quote(
    currentPrice = currentPrice ?: 0.0,
    change = change ?: 0.0,
    percentChange = percentChange ?: 0.0,
    previousClose = previousClose ?: 0.0,
    timestampSeconds = timestampSeconds ?: 0L,
)

fun TradeDto.toDomain(): PriceUpdate = PriceUpdate(
    symbol = symbol,
    price = price,
    timestampMs = timestampMs,
)
