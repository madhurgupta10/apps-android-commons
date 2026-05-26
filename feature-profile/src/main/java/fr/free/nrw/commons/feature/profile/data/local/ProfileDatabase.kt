package fr.free.nrw.commons.feature.profile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.free.nrw.commons.feature.profile.data.local.dao.AchievementDao
import fr.free.nrw.commons.feature.profile.data.local.dao.LeaderboardDao
import fr.free.nrw.commons.feature.profile.data.local.dao.UserProfileDao
import fr.free.nrw.commons.feature.profile.data.local.entity.AchievementEntity
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardEntity
import fr.free.nrw.commons.feature.profile.data.local.entity.UserProfileEntity

/**
 * Room database for the Profile feature module.
 * Stores user profiles, achievements, and leaderboard data locally.
 */
@Database(
    entities = [
        UserProfileEntity::class,
        AchievementEntity::class,
        LeaderboardEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(ProfileTypeConverters::class)
abstract class ProfileDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun achievementDao(): AchievementDao
    abstract fun leaderboardDao(): LeaderboardDao

    companion object {
        const val DATABASE_NAME = "profile_database"
    }
}

