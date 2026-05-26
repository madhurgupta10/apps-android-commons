package fr.free.nrw.commons.feature.profile.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import fr.free.nrw.commons.core.database.BaseDao
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardCategory
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardDuration
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for leaderboard operations.
 * Implements BaseDao for consistent CRUD operations.
 * Supports pagination for efficient large list handling.
 */
@Dao
interface LeaderboardDao : BaseDao<LeaderboardEntity> {

    // BaseDao implementations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: LeaderboardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(entities: List<LeaderboardEntity>)

    @Update
    override suspend fun update(entity: LeaderboardEntity): Int

    @Delete
    override suspend fun delete(entity: LeaderboardEntity): Int

    @Query("DELETE FROM leaderboard")
    override suspend fun deleteAll(): Int

    // Custom leaderboard-specific queries
    @Query("""
        SELECT * FROM leaderboard 
        WHERE category = :category AND duration = :duration 
        ORDER BY rank ASC
    """)
    fun getLeaderboard(
        category: LeaderboardCategory,
        duration: LeaderboardDuration
    ): Flow<List<LeaderboardEntity>>

    @Query("""
        SELECT * FROM leaderboard 
        WHERE category = :category AND duration = :duration 
        ORDER BY rank ASC
    """)
    suspend fun getLeaderboardOnce(
        category: LeaderboardCategory,
        duration: LeaderboardDuration
    ): List<LeaderboardEntity>

    @Query("""
        SELECT * FROM leaderboard 
        WHERE category = :category AND duration = :duration 
        ORDER BY rank ASC
    """)
    fun getLeaderboardPaged(
        category: LeaderboardCategory,
        duration: LeaderboardDuration
    ): PagingSource<Int, LeaderboardEntity>

    @Query("""
        SELECT * FROM leaderboard 
        WHERE category = :category AND duration = :duration AND username = :username
    """)
    suspend fun getUserRank(
        username: String,
        category: LeaderboardCategory,
        duration: LeaderboardDuration
    ): LeaderboardEntity?

    @Query("""
        SELECT * FROM leaderboard 
        WHERE category = :category AND duration = :duration 
        ORDER BY rank ASC 
        LIMIT :limit
    """)
    fun getTopLeaders(
        category: LeaderboardCategory,
        duration: LeaderboardDuration,
        limit: Int = 10
    ): Flow<List<LeaderboardEntity>>

    @Query("DELETE FROM leaderboard WHERE category = :category AND duration = :duration")
    suspend fun clearLeaderboard(category: LeaderboardCategory, duration: LeaderboardDuration)

    @Query("DELETE FROM leaderboard WHERE lastUpdated < :timestamp")
    suspend fun deleteOldEntries(timestamp: Long)

    @Transaction
    suspend fun refreshLeaderboard(
        category: LeaderboardCategory,
        duration: LeaderboardDuration,
        entries: List<LeaderboardEntity>
    ) {
        clearLeaderboard(category, duration)
        insertAll(entries)  // Use BaseDao's insertAll
    }
}

