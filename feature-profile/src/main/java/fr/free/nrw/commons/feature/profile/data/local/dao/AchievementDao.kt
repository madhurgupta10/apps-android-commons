package fr.free.nrw.commons.feature.profile.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import fr.free.nrw.commons.core.database.BaseDao
import fr.free.nrw.commons.feature.profile.data.local.entity.AchievementEntity
import fr.free.nrw.commons.feature.profile.data.local.entity.AchievementType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for achievement operations.
 * Implements BaseDao for consistent CRUD operations.
 * Supports pagination and reactive queries.
 */
@Dao
interface AchievementDao : BaseDao<AchievementEntity> {

    // BaseDao implementations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: AchievementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(entities: List<AchievementEntity>)

    @Update
    override suspend fun update(entity: AchievementEntity): Int

    @Delete
    override suspend fun delete(entity: AchievementEntity): Int

    @Query("DELETE FROM achievements")
    override suspend fun deleteAll(): Int

    // Custom achievement-specific queries
    @Query("SELECT * FROM achievements WHERE username = :username ORDER BY isUnlocked DESC, level DESC, currentValue DESC")
    fun getAchievements(username: String): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE username = :username ORDER BY isUnlocked DESC, level DESC, currentValue DESC")
    suspend fun getAchievementsOnce(username: String): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE username = :username ORDER BY isUnlocked DESC, level DESC, currentValue DESC")
    fun getAchievementsPaged(username: String): PagingSource<Int, AchievementEntity>

    @Query("SELECT * FROM achievements WHERE username = :username AND isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(username: String): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE username = :username AND isUnlocked = 0 ORDER BY currentValue DESC")
    fun getLockedAchievements(username: String): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE username = :username AND achievementType = :type")
    fun getAchievementsByType(username: String, type: AchievementType): Flow<List<AchievementEntity>>

    @Query("SELECT COUNT(*) FROM achievements WHERE username = :username AND isUnlocked = 1")
    fun getUnlockedAchievementCount(username: String): Flow<Int>


    @Query("DELETE FROM achievements WHERE username = :username")
    suspend fun deleteAllAchievements(username: String)

    @Query("DELETE FROM achievements WHERE lastUpdated < :timestamp")
    suspend fun deleteOldAchievements(timestamp: Long)
}

