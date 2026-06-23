package com.doximity.realtimewatchlist_krishna_doximity.data.remote

class FinnhubRateLimitException :
    Exception("Finnhub rate limit exceeded (HTTP 429). Try again shortly.")
