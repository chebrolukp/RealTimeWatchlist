package com.doximity.realtimewatchlist_krishna_doximity.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SymbolSearchResponseDto(
    val count: Int = 0,
    val result: List<SymbolSearchResultDto> = emptyList(),
)

@Serializable
data class SymbolSearchResultDto(
    val description: String = "",
    @SerialName("displaySymbol") val displaySymbol: String = "",
    val symbol: String = "",
    val type: String = "",
)

@Serializable
data class QuoteResponseDto(
    @SerialName("c") val currentPrice: Double? = null,
    @SerialName("d") val change: Double? = null,
    @SerialName("dp") val percentChange: Double? = null,
    @SerialName("pc") val previousClose: Double? = null,
    @SerialName("t") val timestampSeconds: Long? = null,
)

@Serializable
data class WebSocketMessageDto(
    val type: String? = null,
    val data: List<TradeDto>? = null,
)

@Serializable
data class TradeDto(
    @SerialName("s") val symbol: String = "",
    @SerialName("p") val price: Double = 0.0,
    @SerialName("t") val timestampMs: Long = 0L,
    @SerialName("v") val volume: Double = 0.0,
)

@Serializable
data class WebSocketSubscriptionDto(
    val type: String,
    val symbol: String,
)
