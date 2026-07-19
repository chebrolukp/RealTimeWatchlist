package com.doximity.realtimewatchlist_krishna_doximity.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val symbol: String,
    val displaySymbol: String,
    val description: String,
    val type: String,
    val addedAtEpochMs: Long,
    val alertThreshold: Double? = null,
    /** "ABOVE" | "BELOW"; null when no alert is configured. */
    val alertDirection: String? = null,
    val alertTriggered: Boolean = false,
)
