package fr.free.nrw.commons.feature.contributions.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database for contributions feature
 *
 * Version history:
 * - v1: Initial schema
 * - v2: Fixed timestamp parsing to use UTC timezone (forces cache refresh)
 */
@Database(
    entities = [ContributionEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(ContributionConverters::class)
abstract class ContributionDatabase : RoomDatabase() {
    abstract fun contributionDao(): ContributionDao

    companion object {
        const val DATABASE_NAME = "contributions_database"
    }
}

