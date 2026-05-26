package fr.free.nrw.commons.feature.profile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an achievement.
 * Stores achievement badges, milestones, and progress.
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val achievementType: AchievementType,
    val title: String,
    val description: String,
    val iconUrl: String?,
    val currentValue: Int,
    val targetValue: Int,
    val isUnlocked: Boolean,
    val unlockedAt: Long?,
    val level: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Types of achievements available in the app.
 */
enum class AchievementType {
    UPLOADS,
    QUALITY_IMAGES,
    FEATURED_IMAGES,
    THANKS_RECEIVED,
    IMAGES_USED,
    ARTICLES_ENHANCED,
    UPLOAD_STREAK,
    CATEGORIES_ADDED,
    DESCRIPTIONS_ADDED
}

