package com.doximity.realtimewatchlist_krishna_doximity.data.repository

import com.doximity.realtimewatchlist_krishna_doximity.data.local.WatchlistDao
import com.doximity.realtimewatchlist_krishna_doximity.data.local.toDomain
import com.doximity.realtimewatchlist_krishna_doximity.data.local.toEntity
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.Instrument
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.WatchlistItem
import com.doximity.realtimewatchlist_krishna_doximity.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomWatchlistRepository @Inject constructor(
    private val watchlistDao: WatchlistDao,
) : WatchlistRepository {

    override fun observeWatchlist(): Flow<List<WatchlistItem>> =
        watchlistDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addInstrument(instrument: Instrument) {
        watchlistDao.insert(
            instrument.toEntity(addedAtEpochMs = System.currentTimeMillis()),
        )
    }

    override suspend fun removeInstrument(symbol: String) {
        watchlistDao.delete(symbol)
    }

    override suspend fun isInWatchlist(symbol: String): Boolean =
        watchlistDao.getSymbols().contains(symbol)
}
