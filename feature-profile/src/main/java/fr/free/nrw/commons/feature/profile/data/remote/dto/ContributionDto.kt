package fr.free.nrw.commons.feature.profile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response from MediaWiki API for user contributions.
 */
data class UserContributionsResponse(
    @SerializedName("query")
    val query: ContributionsQuery? = null
)

data class ContributionsQuery(
    @SerializedName("logevents")
    val logEvents: List<LogEventDto>? = null
)

/**
 * DTO for a single upload log event from MediaWiki API.
 */
data class LogEventDto(
    @SerializedName("logid")
    val logId: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("params")
    val params: LogEventParams? = null
)

data class LogEventParams(
    @SerializedName("img_sha1")
    val imageSha1: String? = null,

    @SerializedName("img_timestamp")
    val imageTimestamp: String? = null,

    @SerializedName("img_width")
    val imageWidth: Int? = null,

    @SerializedName("img_height")
    val imageHeight: Int? = null
)

