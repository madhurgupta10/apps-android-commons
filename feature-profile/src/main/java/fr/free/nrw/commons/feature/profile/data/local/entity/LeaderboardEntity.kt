package fr.free.nrw.commons.feature.profile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a leaderboard entry.
 * Cached from the network for offline viewing and pagination.
 */
@Entity(tableName = "leaderboard")
data class LeaderboardEntity(
    @PrimaryKey
    val compositeKey: String, // Format: "category_duration_username"
    val username: String,
    val avatarUrl: String?,
    val rank: Int,
    val score: Int,
    val category: LeaderboardCategory,
    val duration: LeaderboardDuration,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Leaderboard categories matching the API.
 */
enum class LeaderboardCategory(val apiValue: String) {
    UPLOAD("upload"),
    USED("used"),
    NEARBY("nearby"),
    FEATURED("featured"),
    QUALITY("quality"),
    THANKS("thanks");

    companion object {
        fun fromApiValue(value: String): LeaderboardCategory =
            values().firstOrNull { it.apiValue == value } ?: UPLOAD
    }
}

/**
 * Duration filters for leaderboard.
 */
enum class LeaderboardDuration(val apiValue: String, val displayName: String) {
    WEEKLY("weekly", "Weekly"),
    MONTHLY("monthly", "Monthly"),
    YEARLY("yearly", "Yearly"),
    ALL_TIME("all-time", "All Time");

    companion object {
        fun fromApiValue(value: String): LeaderboardDuration =
            values().firstOrNull { it.apiValue == value } ?: WEEKLY
    }
}

