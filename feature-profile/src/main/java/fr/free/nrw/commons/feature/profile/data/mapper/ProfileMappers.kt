package fr.free.nrw.commons.feature.profile.data.mapper

import fr.free.nrw.commons.feature.profile.data.local.entity.AchievementEntity
import fr.free.nrw.commons.feature.profile.data.local.entity.AchievementType
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardCategory
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardDuration
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardEntity
import fr.free.nrw.commons.feature.profile.data.local.entity.UserProfileEntity
import fr.free.nrw.commons.feature.profile.data.remote.dto.AchievementDto
import fr.free.nrw.commons.feature.profile.data.remote.dto.AchievementsResponse
import fr.free.nrw.commons.feature.profile.data.remote.dto.LeaderboardDto
import fr.free.nrw.commons.feature.profile.data.remote.dto.LogEventDto
import fr.free.nrw.commons.feature.profile.data.remote.dto.UserProfileDto
import fr.free.nrw.commons.feature.profile.domain.model.Achievement
import fr.free.nrw.commons.feature.profile.domain.model.LeaderboardUser
import fr.free.nrw.commons.feature.profile.domain.model.UserProfile
import fr.free.nrw.commons.feature.contributions.ui.models.Contribution
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Extension functions to map between data layer objects and domain models.
 * Follows clean architecture principles by separating concerns.
 */

// DTO to Entity mappings
fun UserProfileDto.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        username = username,
        avatarUrl = avatar,
        rank = rank,
        uploadCount = uploadCount,
        thanksReceived = thanksReceived,
        uniqueImagesUsed = uniqueImagesUsed,
        articlesUsingImages = articlesUsingImages,
        deletedUploads = deletedUploads,
        lastUpdated = System.currentTimeMillis()
    )
}

fun AchievementDto.toEntity(username: String): AchievementEntity {
    val achievementType = try {
        AchievementType.valueOf(type.uppercase())
    } catch (e: IllegalArgumentException) {
        AchievementType.UPLOADS // Default fallback
    }

    return AchievementEntity(
        username = username,
        achievementType = achievementType,
        title = title,
        description = description,
        iconUrl = iconUrl,
        currentValue = currentValue,
        targetValue = targetValue,
        isUnlocked = isUnlocked,
        unlockedAt = unlockedAt,
        level = level,
        lastUpdated = System.currentTimeMillis()
    )
}

fun LeaderboardDto.toEntity(
    category: LeaderboardCategory,
    duration: LeaderboardDuration
): LeaderboardEntity {
    return LeaderboardEntity(
        compositeKey = "${category.apiValue}_${duration.apiValue}_${username ?: "unknown"}",
        username = username ?: "Unknown",
        avatarUrl = avatar,
        rank = rank ?: 0,
        score = categoryCount ?: 0,
        category = category,
        duration = duration,
        lastUpdated = System.currentTimeMillis()
    )
}

// Entity to Domain Model mappings
fun UserProfileEntity.toDomainModel(): UserProfile {
    return UserProfile(
        username = username,
        avatarUrl = avatarUrl,
        rank = rank,
        uploadCount = uploadCount,
        thanksReceived = thanksReceived,
        uniqueImagesUsed = uniqueImagesUsed,
        articlesUsingImages = articlesUsingImages,
        deletedUploads = deletedUploads,
        qualityScore = calculateQualityScore()
    )
}

fun AchievementEntity.toDomainModel(): Achievement {
    return Achievement(
        id = id,
        type = achievementType,
        title = title,
        description = description,
        iconUrl = iconUrl,
        currentValue = currentValue,
        targetValue = targetValue,
        progress = if (targetValue > 0) (currentValue.toFloat() / targetValue) else 0f,
        isUnlocked = isUnlocked,
        unlockedAt = unlockedAt,
        level = level
    )
}

fun LeaderboardEntity.toDomainModel(): LeaderboardUser {
    return LeaderboardUser(
        username = username,
        avatarUrl = avatarUrl,
        rank = rank,
        score = score,
        isCurrentUser = false // Will be set by the repository
    )
}

// Helper function to calculate quality score
private fun UserProfileEntity.calculateQualityScore(): Float {
    if (uploadCount == 0) return 100f

    val nonDeletedPercentage = ((uploadCount - deletedUploads).toFloat() / uploadCount) * 100
    val usageBonus = (uniqueImagesUsed.toFloat() / uploadCount.coerceAtLeast(1)) * 20
    val thanksBonus = (thanksReceived.toFloat() / uploadCount.coerceAtLeast(1)) * 10

    return (nonDeletedPercentage + usageBonus + thanksBonus).coerceIn(0f, 100f)
}

/**
 * Convert AchievementsResponse (which is actually FeedbackResponse) into mock achievements.
 * Since the API doesn't return structured achievements, we create them from user stats.
 */
fun AchievementsResponse.toAchievementsList(username: String): List<AchievementDto> {
    val achievements = mutableListOf<AchievementDto>()

    // Image Usage Achievement
    achievements.add(AchievementDto(
        type = "USAGE",
        title = "Image Usage",
        description = "Images used in articles",
        iconUrl = null,
        currentValue = uniqueUsedImages,
        targetValue = when {
            uniqueUsedImages >= 100 -> uniqueUsedImages + 50
            uniqueUsedImages >= 50 -> 100
            uniqueUsedImages >= 10 -> 50
            else -> 10
        },
        isUnlocked = uniqueUsedImages >= 10,
        unlockedAt = if (uniqueUsedImages >= 10) System.currentTimeMillis() else null,
        level = when {
            uniqueUsedImages >= 100 -> 3
            uniqueUsedImages >= 50 -> 2
            uniqueUsedImages >= 10 -> 1
            else -> 0
        }
    ))

    // Article Impact Achievement
    achievements.add(AchievementDto(
        type = "ARTICLES",
        title = "Article Impact",
        description = "Articles using your images",
        iconUrl = null,
        currentValue = articlesUsingImages,
        targetValue = when {
            articlesUsingImages >= 50 -> articlesUsingImages + 25
            articlesUsingImages >= 25 -> 50
            articlesUsingImages >= 5 -> 25
            else -> 5
        },
        isUnlocked = articlesUsingImages >= 5,
        unlockedAt = if (articlesUsingImages >= 5) System.currentTimeMillis() else null,
        level = when {
            articlesUsingImages >= 50 -> 3
            articlesUsingImages >= 25 -> 2
            articlesUsingImages >= 5 -> 1
            else -> 0
        }
    ))

    // Thanks Achievement
    achievements.add(AchievementDto(
        type = "THANKS",
        title = "Community Appreciation",
        description = "Thanks received from other users",
        iconUrl = null,
        currentValue = thanksReceived,
        targetValue = when {
            thanksReceived >= 100 -> thanksReceived + 50
            thanksReceived >= 50 -> 100
            thanksReceived >= 10 -> 50
            else -> 10
        },
        isUnlocked = thanksReceived >= 10,
        unlockedAt = if (thanksReceived >= 10) System.currentTimeMillis() else null,
        level = when {
            thanksReceived >= 100 -> 3
            thanksReceived >= 50 -> 2
            thanksReceived >= 10 -> 1
            else -> 0
        }
    ))

    // Quality Achievement
    achievements.add(AchievementDto(
        type = "QUALITY",
        title = "Quality Contributor",
        description = "High-quality images with minimal deletions",
        iconUrl = null,
        currentValue = maxOf(0, uniqueUsedImages - deletedUploads),
        targetValue = when {
            uniqueUsedImages >= 50 -> 50
            uniqueUsedImages >= 25 -> 25
            uniqueUsedImages >= 10 -> 10
            else -> 5
        },
        isUnlocked = (uniqueUsedImages > 0 && deletedUploads <= uniqueUsedImages / 4),
        unlockedAt = if (uniqueUsedImages > 0 && deletedUploads <= uniqueUsedImages / 4) System.currentTimeMillis() else null,
        level = when {
            uniqueUsedImages >= 50 && deletedUploads <= uniqueUsedImages / 10 -> 3
            uniqueUsedImages >= 25 && deletedUploads <= uniqueUsedImages / 8 -> 2
            uniqueUsedImages >= 10 && deletedUploads <= uniqueUsedImages / 4 -> 1
            else -> 0
        }
    ))

    // Featured Images Achievement
    val totalFeatured = featuredImages.qualityImages + featuredImages.featuredPicturesOnWikimediaCommons
    achievements.add(AchievementDto(
        type = "FEATURED",
        title = "Featured Content",
        description = "Quality and featured images",
        iconUrl = null,
        currentValue = totalFeatured,
        targetValue = when {
            totalFeatured >= 10 -> totalFeatured + 5
            totalFeatured >= 5 -> 10
            totalFeatured >= 1 -> 5
            else -> 1
        },
        isUnlocked = totalFeatured >= 1,
        unlockedAt = if (totalFeatured >= 1) System.currentTimeMillis() else null,
        level = when {
            totalFeatured >= 10 -> 3
            totalFeatured >= 5 -> 2
            totalFeatured >= 1 -> 1
            else -> 0
        }
    ))

    return achievements
}

/**
 * Convert MediaWiki LogEventDto to Contribution presentation model.
 * Generates thumbnail URLs and formats timestamps.
 */
fun LogEventDto.toContribution(): Contribution {
    // Extract filename from title (e.g., "File:Example.jpg" -> "Example.jpg")
    val filename = title.removePrefix("File:")

    // URL-encode the filename for the thumbnail URL
    val encodedFilename = URLEncoder.encode(filename, "UTF-8")

    // Generate thumbnail URL (Special:FilePath provides direct access)
    val thumbnailUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/$encodedFilename?width=800"

    // Parse timestamp to Long
    val uploadDate = parseTimestampToMillis(timestamp)

    // Extract image dimensions from params if available
    val width = params?.imageWidth ?: 0
    val height = params?.imageHeight ?: 0
    val aspectRatio = if (width > 0 && height > 0) {
        width.toFloat() / height.toFloat()
    } else {
        1f // Default to square
    }

    return Contribution(
        id = logId.toString(),
        title = filename,
        thumbnailUrl = thumbnailUrl,
        uploadDate = uploadDate,
        views = 0, // TODO: Fetch from usage API
        aspectRatio = aspectRatio,
        width = width,
        height = height
    )
}

/**
 * Parse ISO 8601 timestamp to milliseconds since epoch.
 */
private fun parseTimestampToMillis(timestamp: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp)
        date?.time ?: System.currentTimeMillis()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

/**
 * Format ISO 8601 timestamp to relative time string.
 */
@Suppress("unused") // May be used in future for UI display
private fun formatTimestamp(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp)

        if (date != null) {
            val now = System.currentTimeMillis()
            val diff = now - date.time

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7
            val months = days / 30
            val years = days / 365

            when {
                years > 0 -> "$years year${if (years > 1) "s" else ""} ago"
                months > 0 -> "$months month${if (months > 1) "s" else ""} ago"
                weeks > 0 -> "$weeks week${if (weeks > 1) "s" else ""} ago"
                days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
                hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
                else -> "Just now"
            }
        } else {
            timestamp
        }
    } catch (_: Exception) {
        timestamp
    }
}
