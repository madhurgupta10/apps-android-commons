package fr.free.nrw.commons.feature.contributions.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ContributionEntity
 */
@Dao
interface ContributionDao {

    /**
     * Get contributions for a specific user with pagination support
     * @param username The username to fetch contributions for
     * @return PagingSource for paginated contributions
     */
    @Query("SELECT * FROM user_contributions WHERE username = :username ORDER BY dateUploaded DESC")
    fun getContributionsByUser(username: String): PagingSource<Int, ContributionEntity>

    /**
     * Get contributions for a specific user as a Flow
     * @param username The username to fetch contributions for
     * @return Flow of list of contributions
     */
    @Query("SELECT * FROM user_contributions WHERE username = :username ORDER BY dateUploaded DESC")
    fun getContributionsByUserFlow(username: String): Flow<List<ContributionEntity>>

    /**
     * Get contributions for a specific user with specific states
     * @param username The username to fetch contributions for
     * @param states List of states to filter by
     * @return PagingSource for paginated contributions
     */
    @Query("SELECT * FROM user_contributions WHERE username = :username AND state IN (:states) ORDER BY dateUploaded DESC")
    fun getContributionsByUserAndStates(
        username: String,
        states: List<Int>
    ): PagingSource<Int, ContributionEntity>

    /**
     * Insert or replace contributions
     * @param contributions List of contributions to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContributions(contributions: List<ContributionEntity>)

    /**
     * Insert or replace a single contribution
     * @param contribution Contribution to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: ContributionEntity)

    /**
     * Get a single contribution by pageId
     * @param pageId The page ID of the contribution
     * @return ContributionEntity or null
     */
    @Query("SELECT * FROM user_contributions WHERE pageId = :pageId LIMIT 1")
    suspend fun getContributionById(pageId: String): ContributionEntity?

    /**
     * Delete all contributions for a specific user
     * @param username The username whose contributions to delete
     */
    @Query("DELETE FROM user_contributions WHERE username = :username")
    suspend fun deleteContributionsByUser(username: String)

    /**
     * Delete old contributions (older than the given timestamp)
     * @param timestamp Timestamp in milliseconds
     */
    @Query("DELETE FROM user_contributions WHERE lastUpdated < :timestamp")
    suspend fun deleteOldContributions(timestamp: Long)

    /**
     * Get the count of contributions for a user
     * @param username The username to count contributions for
     * @return Count of contributions
     */
    @Query("SELECT COUNT(*) FROM user_contributions WHERE username = :username")
    suspend fun getContributionCount(username: String): Int

    /**
     * Get the timestamp of the last updated contribution for a user
     * @param username The username to check
     * @return Timestamp or null
     */
    @Query("SELECT MAX(lastUpdated) FROM user_contributions WHERE username = :username")
    suspend fun getLastUpdatedTimestamp(username: String): Long?
}

