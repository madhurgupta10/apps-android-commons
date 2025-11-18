package fr.free.nrw.commons.feature.profile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for leaderboard API response.
 * Based on the actual API response structure from the working app.
 */
data class LeaderboardDto(
    @SerializedName("username")
    val username: String?,

    @SerializedName("avatar")
    val avatar: String?,

    @SerializedName("rank")
    val rank: Int?,

    @SerializedName("category_count")
    val categoryCount: Int?
)

data class LeaderboardResponse(
    @SerializedName("status")
    val status: Int? = null,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("rank")
    val rank: Int? = null,

    @SerializedName("category_count")
    val categoryCount: Int? = null,

    @SerializedName("avatar")
    val avatar: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("duration")
    val duration: String? = null,

    @SerializedName("limit")
    val limit: Int = 0,

    @SerializedName("offset")
    val offset: Int = 0,

    @SerializedName("leaderboard_list")
    val leaderboardList: List<LeaderboardDto> = emptyList()
)

