package fr.free.nrw.commons.feature.profile.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import fr.free.nrw.commons.core.network.NetworkResult
import fr.free.nrw.commons.core.network.safeNetworkCall
import fr.free.nrw.commons.feature.profile.data.local.dao.AchievementDao
import fr.free.nrw.commons.feature.profile.data.local.dao.LeaderboardDao
import fr.free.nrw.commons.feature.profile.data.local.dao.UserProfileDao
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardCategory
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardDuration
import fr.free.nrw.commons.feature.profile.data.mapper.toDomainModel
import fr.free.nrw.commons.feature.profile.data.mapper.toEntity
import fr.free.nrw.commons.feature.profile.data.mapper.toAchievementsList
import fr.free.nrw.commons.feature.profile.data.mapper.toContribution
import fr.free.nrw.commons.feature.profile.data.remote.ProfileApiService
import fr.free.nrw.commons.feature.profile.domain.model.Achievement
import fr.free.nrw.commons.feature.profile.domain.model.LeaderboardUser
import fr.free.nrw.commons.feature.profile.domain.model.UserProfile
import fr.free.nrw.commons.feature.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ProfileRepository following the repository pattern.
 * Handles data operations with a single source of truth (local database).
 * Network data is fetched and cached locally.
 *
 * Now uses NetworkResult from core-network module for consistent error handling.
 */
@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiService: ProfileApiService,
    private val userProfileDao: UserProfileDao,
    private val achievementDao: AchievementDao,
    private val leaderboardDao: LeaderboardDao
) : ProfileRepository {

    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 5
        private const val CACHE_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
    }

    // User Profile Operations
    override fun getUserProfile(username: String): Flow<Result<UserProfile>> = flow {
        // Check if we need to refresh first
        val cachedProfile = userProfileDao.getUserProfileOnce(username)
        if (cachedProfile == null || isCacheStale(cachedProfile.lastUpdated)) {
            // Refresh from network first if cache is empty or stale
            val refreshResult = refreshUserProfile(username)
            if (refreshResult.isFailure && cachedProfile == null) {
                // If refresh failed and no cache exists, emit the error
                emit(Result.failure(refreshResult.exceptionOrNull() ?: Exception("Failed to load profile")))
                return@flow
            }
        }

        // Emit data from cache (now populated if it wasn't before)
        userProfileDao.getUserProfile(username)
            .map { entity ->
                entity?.let { Result.success(it.toDomainModel()) }
                    ?: Result.failure(Exception("Profile not found"))
            }
            .catch { emit(Result.failure(it)) }
            .collect { emit(it) }
    }.catch {
        emit(Result.failure(it))
    }

    override suspend fun refreshUserProfile(username: String): Result<Unit> {
        // Use safeNetworkCall from core-network module
        return when (val networkResult = safeNetworkCall { apiService.getUserProfile(username) }) {
            is NetworkResult.Success -> {
                try {
                    val profileEntity = networkResult.data.toEntity()
                    userProfileDao.insert(profileEntity)  // Use BaseDao method
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            is NetworkResult.Error -> {
                Result.failure(networkResult.throwable ?: Exception(networkResult.message))
            }
            is NetworkResult.Loading -> {
                Result.failure(Exception("Unexpected loading state"))
            }
        }
    }

    // Achievement Operations
    override fun getAchievements(username: String): Flow<Result<List<Achievement>>> = flow {
        // Check if cache is empty or stale - simplified approach for now
        // TODO: Use getAchievementsOnce when Room generates it after build

        // Always try to refresh first, then emit from cache
        val refreshResult = refreshAchievements(username)
        if (refreshResult.isFailure) {
            // If refresh failed, still try to emit cached data if available
        }

        // Emit data from cache
        achievementDao.getAchievements(username)
            .map { entities ->
                Result.success(entities.map { it.toDomainModel() })
            }
            .catch { emit(Result.failure(it)) }
            .collect { emit(it) }
    }.catch {
        emit(Result.failure(it))
    }

    override fun getAchievementsPaged(username: String): Flow<PagingData<Achievement>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { achievementDao.getAchievementsPaged(username) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }

    override suspend fun refreshAchievements(username: String): Result<Unit> {
        return try {
            val response = apiService.getAchievements(username)
            // Convert FeedbackResponse to achievements list
            val achievementDtos = response.toAchievementsList(username)
            val entities = achievementDtos.map { it.toEntity(username) }
            achievementDao.insertAll(entities)  // Use BaseDao method
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUnlockedAchievementsCount(username: String): Flow<Int> {
        return achievementDao.getUnlockedAchievementCount(username)
    }

    // Leaderboard Operations
    override fun getLeaderboard(
        category: LeaderboardCategory,
        duration: LeaderboardDuration,
        username: String?
    ): Flow<Result<List<LeaderboardUser>>> = flow {
        // Check if cache is empty first - simplified approach for now
        // TODO: Use getLeaderboardOnce when Room generates it after build

        // Always try to refresh first, then emit from cache
        val refreshResult = refreshLeaderboard(category, duration, username)
        if (refreshResult.isFailure) {
            // If refresh failed, still try to emit cached data if available
        }

        // Emit data from cache
        leaderboardDao.getLeaderboard(category, duration)
            .map { entities ->
                Result.success(entities.map { entity ->
                    entity.toDomainModel().copy(
                        isCurrentUser = username != null && entity.username == username
                    )
                })
            }
            .catch { emit(Result.failure(it)) }
            .collect { emit(it) }
    }.catch {
        emit(Result.failure(it))
    }

    override fun getLeaderboardPaged(
        category: LeaderboardCategory,
        duration: LeaderboardDuration,
        username: String?
    ): Flow<PagingData<LeaderboardUser>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { leaderboardDao.getLeaderboardPaged(category, duration) }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toDomainModel().copy(
                    isCurrentUser = username != null && entity.username == username
                )
            }
        }
    }

    override suspend fun refreshLeaderboard(
        category: LeaderboardCategory,
        duration: LeaderboardDuration,
        username: String?
    ): Result<Unit> {
        return try {
            val response = apiService.getLeaderboard(
                username = username,
                duration = duration.apiValue,
                category = category.apiValue,
                limit = "100",
                offset = "0"
            )

            val entities = response.leaderboardList.map { dto ->
                dto.toEntity(category, duration)
            }

            leaderboardDao.refreshLeaderboard(category, duration, entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserRank(
        username: String,
        category: LeaderboardCategory,
        duration: LeaderboardDuration
    ): Flow<Result<LeaderboardUser?>> = flow {
        val entity = leaderboardDao.getUserRank(username, category, duration)
        emit(Result.success(entity?.toDomainModel()?.copy(isCurrentUser = true)))
    }.catch {
        emit(Result.failure(it))
    }

    override suspend fun getUserContributions(
        username: String,
        limit: Int
    ): Result<List<fr.free.nrw.commons.feature.profile.presentation.Contribution>> {
        // TODO: Implement when MediaWiki API is properly configured
        // Temporarily return empty list - "Coming Soon" message shown in UI
        return Result.success(emptyList())
    }

    private fun isCacheStale(lastUpdated: Long): Boolean {
        return System.currentTimeMillis() - lastUpdated > CACHE_TIMEOUT_MS
    }
}

