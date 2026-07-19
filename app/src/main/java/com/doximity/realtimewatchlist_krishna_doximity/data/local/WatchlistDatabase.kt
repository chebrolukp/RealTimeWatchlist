package com.doximity.realtimewatchlist_krishna_doximity.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.doximity.realtimewatchlist_krishna_doximity.data.local.entity.WatchlistEntity

@Database(
    entities = [WatchlistEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class WatchlistDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE watchlist ADD COLUMN alertThreshold REAL")
                db.execSQL("ALTER TABLE watchlist ADD COLUMN alertDirection TEXT")
                db.execSQL(
                    "ALTER TABLE watchlist ADD COLUMN alertTriggered INTEGER NOT NULL DEFAULT 0",
                )
            }
        }
    }
}
