package com.doximity.realtimewatchlist_krishna_doximity.core.error

sealed class FinnhubException : Exception() {
    class Unauthorized : FinnhubException()
    class Forbidden : FinnhubException()
    class RateLimit : FinnhubException()
    class Api(
        val httpCode: Int? = null,
        val isEmptyBody: Boolean = false,
    ) : FinnhubException()
}
