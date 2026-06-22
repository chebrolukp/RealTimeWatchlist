package com.doximity.realtimewatchlist_krishna_doximity.domain.usecase

import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.MarketDataRepository
import javax.inject.Inject

class SearchInstrumentsUseCase @Inject constructor(
    private val marketDataRepository: MarketDataRepository
) {
    suspend operator fun invoke(query: String): Result<List<Instrument>> {
        return marketDataRepository.searchInstruments(query)
    }
}
