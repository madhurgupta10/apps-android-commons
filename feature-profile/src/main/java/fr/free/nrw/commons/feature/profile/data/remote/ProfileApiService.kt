package fr.free.nrw.commons.feature.profile.data.remote

import fr.free.nrw.commons.feature.profile.data.remote.dto.AchievementsResponse
import fr.free.nrw.commons.feature.profile.data.remote.dto.LeaderboardResponse
import fr.free.nrw.commons.feature.profile.data.remote.dto.UserProfileDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API service for profile-related network requests.
 * Based on the existing Commons API structure.
 */
interface ProfileApiService {

    /**
     * Fetch user achievements.
     * Endpoint: /feedback.py
     *
     * @param username The username to fetch achievements for
     * @return Achievements response with user data
     */
    @GET("feedback.py")
    suspend fun getAchievements(
        @Query("user") username: String
    ): AchievementsResponse

    /**
     * Fetch leaderboard data with pagination.
     * Endpoint: /leaderboard.py
     *
     * @param username Current user's username to get their rank
     * @param duration Time period (weekly, monthly, yearly, all-time)
     * @param category Category type (upload, used, nearby, etc.)
     * @param limit Number of entries to fetch
     * @param offset Pagination offset
     * @return Leaderboard response with entries and user's rank
     */
    @GET("leaderboard.py")
    suspend fun getLeaderboard(
        @Query("user") username: String?,
        @Query("duration") duration: String?,
        @Query("category") category: String?,
        @Query("limit") limit: String?,
        @Query("offset") offset: String?
    ): LeaderboardResponse

    /**
     * Fetch user profile information.
     * Note: Uses the feedback.py endpoint which contains user data
     *
     * @param username The username to fetch profile for
     * @return User profile data
     */
    @GET("feedback.py")
    suspend fun getUserProfile(
        @Query("user") username: String
    ): UserProfileDto
}

