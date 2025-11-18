package fr.free.nrw.commons.feature.profile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a user profile stored locally.
 * Cached from the network to enable offline access.
 */
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val username: String,
    val avatarUrl: String?,
    val rank: Int,
    val uploadCount: Int,
    val thanksReceived: Int,
    val uniqueImagesUsed: Int,
    val articlesUsingImages: Int,
    val deletedUploads: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

