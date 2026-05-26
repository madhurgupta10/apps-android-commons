package fr.free.nrw.commons.feature.contributions.domain.repository

import androidx.paging.PagingData
import fr.free.nrw.commons.feature.contributions.domain.model.ContributionModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for contributions
 * Follows clean architecture principles
 */
interface ContributionRepository {
    /**
     * Get paginated contributions for a user
     * Data is fetched from DB as single source of truth
     * Network data automatically updates the DB via RemoteMediator
     *
     * @param username Username to fetch contributions for
     * @return Flow of PagingData containing contributions
     */
    fun getUserContributions(username: String): Flow<PagingData<ContributionModel>>

    /**
     * Get contributions for a user as a simple Flow (non-paginated)
     * Useful for observing changes
     *
     * @param username Username to fetch contributions for
     * @return Flow of list of contributions
     */
    fun getUserContributionsFlow(username: String): Flow<List<ContributionModel>>

    /**
     * Refresh contributions for a user
     * Clears local cache and fetches fresh data from network
     *
     * @param username Username to refresh contributions for
     */
    suspend fun refreshContributions(username: String): Result<Unit>

    /**
     * Get contribution count for a user
     *
     * @param username Username to get count for
     * @return Number of contributions
     */
    suspend fun getContributionCount(username: String): Int

    /**
     * Clear contributions for a user from local cache
     *
     * @param username Username to clear contributions for
     */
    suspend fun clearUserContributions(username: String)
}

