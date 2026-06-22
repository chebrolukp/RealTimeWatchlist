package com.doximity.realtimewatchlist_krishna_doximity.data.remote

import retrofit2.Response

internal fun <T> Response<T>.requireFinnhubBody(): T {
    if (isSuccessful) {
        val body = body()
        if (body != null) return body
        throw FinnhubApiException("Finnhub returned an empty response.")
    }
    throw when (code()) {
        401 -> FinnhubUnauthorizedException()
        403 -> FinnhubForbiddenException()
        429 -> FinnhubRateLimitException()
        else -> FinnhubApiException("Finnhub request failed (HTTP ${code()}).")
    }
}

class FinnhubApiException(message: String) : Exception(message)

class FinnhubUnauthorizedException : Exception("Finnhub request unauthorized (401).")
class FinnhubForbiddenException : Exception("Finnhub request forbidden (403).")
class FinnhubRateLimitException : Exception("Finnhub rate limit exceeded (429).")
