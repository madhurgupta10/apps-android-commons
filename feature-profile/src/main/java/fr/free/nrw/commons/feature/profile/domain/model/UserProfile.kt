package fr.free.nrw.commons.feature.profile.domain.model

import fr.free.nrw.commons.feature.profile.data.local.entity.AchievementType

/**
 * Domain model for user profile.
 * This is the model used throughout the presentation layer.
 */
data class UserProfile(
    val username: String,
    val avatarUrl: String?,
    val rank: Int,
    val uploadCount: Int,
    val thanksReceived: Int,
    val uniqueImagesUsed: Int,
    val articlesUsingImages: Int,
    val deletedUploads: Int,
    val qualityScore: Float
) {
    val uploadSuccessRate: Float
        get() = if (uploadCount > 0) {
            ((uploadCount - deletedUploads).toFloat() / uploadCount) * 100
        } else {
            100f
        }
}

