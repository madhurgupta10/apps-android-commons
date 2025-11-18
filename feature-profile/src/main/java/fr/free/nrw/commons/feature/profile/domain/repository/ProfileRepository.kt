package fr.free.nrw.commons.feature.profile.domain.repository

import androidx.paging.PagingData
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardCategory
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardDuration
import fr.free.nrw.commons.feature.profile.domain.model.Achievement
import fr.free.nrw.commons.feature.profile.domain.model.LeaderboardUser
import fr.free.nrw.commons.feature.profile.domain.model.UserProfile
import fr.free.nrw.commons.feature.profile.presentation.Contribution
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for profile data operations.
 * Provides a clean API for the domain layer.
 */
interface ProfileRepository {

    // User Profile Operations
    fun getUserProfile(username: String): Flow<Result<UserProfile>>
    suspend fun refreshUserProfile(username: String): Result<Unit>

    // Achievement Operations
    fun getAchievements(username: String): Flow<Result<List<Achievement>>>
    fun getAchievementsPaged(username: String): Flow<PagingData<Achievement>>
    suspend fun refreshAchievements(username: String): Result<Unit>
    fun getUnlockedAchievementsCount(username: String): Flow<Int>

    // Leaderboard Operations
    fun getLeaderboard(
        category: LeaderboardCategory,
        duration: LeaderboardDuration,
        username: String?
    ): Flow<Result<List<LeaderboardUser>>>

    fun getLeaderboardPaged(
        category: LeaderboardCategory,
        duration: LeaderboardDuration,
        username: String?
    ): Flow<PagingData<LeaderboardUser>>

    suspend fun refreshLeaderboard(
        category: LeaderboardCategory,
        duration: LeaderboardDuration,
        username: String?
    ): Result<Unit>

    fun getUserRank(
        username: String,
        category: LeaderboardCategory,
        duration: LeaderboardDuration
    ): Flow<Result<LeaderboardUser?>>

    // Contribution Operations
    suspend fun getUserContributions(username: String, limit: Int = 50): Result<List<Contribution>>
}

