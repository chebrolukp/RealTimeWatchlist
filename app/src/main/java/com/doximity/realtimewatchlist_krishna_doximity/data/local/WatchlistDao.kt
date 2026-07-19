package com.doximity.realtimewatchlist_krishna_doximity.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.doximity.realtimewatchlist_krishna_doximity.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAtEpochMs ASC")
    fun observeAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT symbol FROM watchlist")
    suspend fun getSymbols(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun delete(symbol: String)

    @Query(
        """
        UPDATE watchlist SET
            alertThreshold = :threshold,
            alertDirection = :direction,
            alertTriggered = :triggered
        WHERE symbol = :symbol
        """,
    )
    suspend fun updateAlert(
        symbol: String,
        threshold: Double?,
        direction: String?,
        triggered: Boolean,
    )

    @Query("UPDATE watchlist SET alertTriggered = 1 WHERE symbol = :symbol")
    suspend fun markAlertTriggered(symbol: String)
}
