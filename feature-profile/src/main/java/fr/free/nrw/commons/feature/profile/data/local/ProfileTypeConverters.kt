package fr.free.nrw.commons.feature.profile.data.local

import androidx.room.TypeConverter
import fr.free.nrw.commons.feature.profile.data.local.entity.AchievementType
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardCategory
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardDuration

/**
 * Type converters for Room database.
 * Converts complex types to primitives that Room can store.
 */
class ProfileTypeConverters {

    @TypeConverter
    fun fromAchievementType(value: AchievementType): String {
        return value.name
    }

    @TypeConverter
    fun toAchievementType(value: String): AchievementType {
        return AchievementType.valueOf(value)
    }

    @TypeConverter
    fun fromLeaderboardCategory(value: LeaderboardCategory): String {
        return value.name
    }

    @TypeConverter
    fun toLeaderboardCategory(value: String): LeaderboardCategory {
        return LeaderboardCategory.valueOf(value)
    }

    @TypeConverter
    fun fromLeaderboardDuration(value: LeaderboardDuration): String {
        return value.name
    }

    @TypeConverter
    fun toLeaderboardDuration(value: String): LeaderboardDuration {
        return LeaderboardDuration.valueOf(value)
    }
}

