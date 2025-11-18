package fr.free.nrw.commons.feature.profile.data.local.dao

import androidx.room.*
import fr.free.nrw.commons.core.database.BaseDao
import fr.free.nrw.commons.feature.profile.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for user profile operations.
 * Implements BaseDao for consistent CRUD operations.
 * Provides Flow-based reactive queries for observing data changes.
 */
@Dao
interface UserProfileDao : BaseDao<UserProfileEntity> {

    // BaseDao implementations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: UserProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertAll(entities: List<UserProfileEntity>)

    @Update
    override suspend fun update(entity: UserProfileEntity): Int

    @Delete
    override suspend fun delete(entity: UserProfileEntity): Int

    @Query("DELETE FROM user_profiles")
    override suspend fun deleteAll(): Int

    // Custom profile-specific queries
    @Query("SELECT * FROM user_profiles WHERE username = :username")
    fun getUserProfile(username: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE username = :username")
    suspend fun getUserProfileOnce(username: String): UserProfileEntity?


    @Query("DELETE FROM user_profiles WHERE username = :username")
    suspend fun deleteUserProfileByUsername(username: String)

    @Query("DELETE FROM user_profiles WHERE lastUpdated < :timestamp")
    suspend fun deleteOldProfiles(timestamp: Long)

    @Query("SELECT * FROM user_profiles ORDER BY lastUpdated DESC LIMIT 1")
    fun getMostRecentProfile(): Flow<UserProfileEntity?>
}

