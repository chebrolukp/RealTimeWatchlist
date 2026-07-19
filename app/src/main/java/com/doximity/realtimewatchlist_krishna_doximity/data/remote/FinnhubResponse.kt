package com.doximity.realtimewatchlist_krishna_doximity.data.remote

import com.doximity.realtimewatchlist_krishna_doximity.core.error.FinnhubException
import retrofit2.Response

internal fun <T> Response<T>.requireFinnhubBody(): T {
    if (isSuccessful) {
        val body = body()
        if (body != null) return body
        throw FinnhubException.Api(isEmptyBody = true)
    }
    throw when (code()) {
        401 -> FinnhubException.Unauthorized()
        403 -> FinnhubException.Forbidden()
        429 -> FinnhubException.RateLimit()
        else -> FinnhubException.Api(httpCode = code())
    }
}
