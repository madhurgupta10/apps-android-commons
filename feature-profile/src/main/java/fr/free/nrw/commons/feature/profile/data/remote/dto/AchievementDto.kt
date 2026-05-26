package fr.free.nrw.commons.feature.profile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for achievement API response.
 * This is a derived structure from FeedbackResponse to create achievements.
 */
data class AchievementDto(
    @SerializedName("type")
    val type: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("icon_url")
    val iconUrl: String?,

    @SerializedName("current_value")
    val currentValue: Int,

    @SerializedName("target_value")
    val targetValue: Int,

    @SerializedName("is_unlocked")
    val isUnlocked: Boolean,

    @SerializedName("unlocked_at")
    val unlockedAt: Long?,

    @SerializedName("level")
    val level: Int
)

/**
 * Response from /feedback.py API - represents actual user feedback data.
 * We'll transform this into achievements.
 */
data class AchievementsResponse(
    @SerializedName("uniqueUsedImages")
    val uniqueUsedImages: Int = 0,

    @SerializedName("articlesUsingImages")
    val articlesUsingImages: Int = 0,

    @SerializedName("deletedUploads")
    val deletedUploads: Int = 0,

    @SerializedName("featuredImages")
    val featuredImages: FeaturedImagesDto = FeaturedImagesDto(),

    @SerializedName("thanksReceived")
    val thanksReceived: Int = 0,

    @SerializedName("user")
    val user: String = ""
)


