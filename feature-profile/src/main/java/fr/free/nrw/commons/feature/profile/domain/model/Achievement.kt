package fr.free.nrw.commons.feature.profile.domain.model

import fr.free.nrw.commons.feature.profile.data.local.entity.AchievementType

/**
 * Domain model for an achievement.
 */
data class Achievement(
    val id: Int,
    val type: AchievementType,
    val title: String,
    val description: String,
    val iconUrl: String?,
    val currentValue: Int,
    val targetValue: Int,
    val progress: Float, // 0.0 to 1.0
    val isUnlocked: Boolean,
    val unlockedAt: Long?,
    val level: Int
) {
    val progressPercentage: Int
        get() = (progress * 100).toInt()

    val isInProgress: Boolean
        get() = !isUnlocked && currentValue > 0
}

