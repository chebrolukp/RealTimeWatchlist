package com.doximity.realtimewatchlist_krishna_doximity.data.remote

import com.doximity.realtimewatchlist_krishna_doximity.data.remote.dto.CandleResponseDto
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.dto.QuoteResponseDto
import com.doximity.realtimewatchlist_krishna_doximity.data.remote.dto.SymbolSearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubApi {

    @GET("search")
    suspend fun searchSymbols(
        @Query("q") query: String,
    ): Response<SymbolSearchResponseDto>

    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
    ): Response<QuoteResponseDto>

    @GET("stock/candle")
    suspend fun getStockCandles(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") fromUnixSeconds: Long,
        @Query("to") toUnixSeconds: Long,
    ): Response<CandleResponseDto>

    @GET("crypto/candle")
    suspend fun getCryptoCandles(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") fromUnixSeconds: Long,
        @Query("to") toUnixSeconds: Long,
    ): Response<CandleResponseDto>

    @GET("forex/candle")
    suspend fun getForexCandles(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") fromUnixSeconds: Long,
        @Query("to") toUnixSeconds: Long,
    ): Response<CandleResponseDto>

    companion object {
        const val BASE_URL = "https://finnhub.io/api/v1/"
    }
}
