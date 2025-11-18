package fr.free.nrw.commons.feature.profile.presentation

import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardCategory
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardDuration
import fr.free.nrw.commons.feature.profile.domain.model.Achievement
import fr.free.nrw.commons.feature.profile.domain.model.LeaderboardUser
import fr.free.nrw.commons.feature.profile.domain.model.UserProfile

/**
 * UI state for the Profile screen.
 * Represents all possible states of the screen.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val selectedTab: ProfileTab = ProfileTab.LEADERBOARD,

    // Achievements tab
    val achievements: List<Achievement> = emptyList(),
    val unlockedAchievementsCount: Int = 0,
    val achievementsError: String? = null,

    // Leaderboard tab
    val leaderboard: List<LeaderboardUser> = emptyList(),
    val selectedCategory: LeaderboardCategory = LeaderboardCategory.UPLOAD,
    val selectedDuration: LeaderboardDuration = LeaderboardDuration.WEEKLY,
    val userRank: LeaderboardUser? = null,
    val leaderboardError: String? = null,

    // Contributions tab
    val contributions: List<Contribution> = emptyList(),
    val contributionsError: String? = null,

    // General error
    val error: String? = null,

    // Refresh state
    val isRefreshing: Boolean = false
)

/**
 * Available tabs in the profile screen.
 */
enum class ProfileTab {
    ACHIEVEMENTS,
    LEADERBOARD,
    CONTRIBUTIONS
}

/**
 * Represents a user contribution (placeholder for future implementation).
 */
data class Contribution(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val uploadDate: Long,
    val views: Int,
    val aspectRatio: Float = 1f, // width/height for smart grid sizing
    val width: Int = 0,
    val height: Int = 0
)

/**
 * Groups contributions by timeline (day, week, month, year)
 */
data class TimelineGroup(
    val label: String, // e.g., "Today", "1 year ago", "December 2024"
    val timestamp: Long,
    val contributions: List<Contribution>
)

