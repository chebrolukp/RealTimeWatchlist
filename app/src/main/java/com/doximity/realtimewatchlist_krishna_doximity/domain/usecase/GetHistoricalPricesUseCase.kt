package com.doximity.realtimewatchlist_krishna_doximity.domain.usecase

import com.doximity.realtimewatchlist_krishna_doximity.domain.model.HistoricalPrices
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import javax.inject.Inject

class GetHistoricalPricesUseCase @Inject constructor(
    private val marketDataRepository: MarketDataRepository,
) {
    suspend operator fun invoke(
        symbol: String,
        instrumentType: String,
        days: Int = DEFAULT_DAYS,
    ): Result<HistoricalPrices> =
        marketDataRepository.getHistoricalPrices(
            symbol = symbol,
            instrumentType = instrumentType,
            days = days,
        )

    companion object {
        const val DEFAULT_DAYS = 30
    }
}
