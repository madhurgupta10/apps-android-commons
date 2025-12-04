package fr.free.nrw.commons.feature.contributions.data.remote

import com.google.gson.Gson
import fr.free.nrw.commons.feature.contributions.data.remote.api.MediaWikiApi
import fr.free.nrw.commons.feature.contributions.data.remote.api.Page
import fr.free.nrw.commons.feature.contributions.domain.model.ContributionModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for fetching contributions from MediaWiki API
 * Uses core-network module for network operations
 * Handles multi-part continuation properly
 */
@Singleton
class ContributionRemoteDataSource @Inject constructor(
    private val mediaWikiApi: MediaWikiApi,
    private val gson: Gson
) {
    /**
     * Fetch contributions for a user from the remote API
     *
     * @param username Username to fetch contributions for
     * @param continuation Continuation token for pagination (null for first page)
     * @return Pair of contributions list and continuation token for next page
     */
    suspend fun fetchUserContributions(
        username: String,
        continuation: String? = null
    ): Result<Pair<List<ContributionModel>, String?>> {
        return try {
            Timber.d("=== FETCH START ===")
            Timber.d("Fetching contributions for user: $username, continuation: $continuation")

            // Build continuation map
            // IMPORTANT: Don't add 'continue' parameter! The API doesn't need it for simple gaicontinue
            val continuationMap = if (continuation != null) {
                mapOf("gaicontinue" to continuation)
            } else {
                // First request - NO continuation parameters
                emptyMap()
            }

            Timber.d("API params: username=$username, limit=10")
            Timber.d("Continuation map: $continuationMap")
            Timber.d("Will call: w/api.php?...&gaiuser=$username&gailimit=10${if (continuationMap.isNotEmpty()) "&" + continuationMap.entries.joinToString("&") { "${it.key}=${it.value}" } else ""}")

            // Fetch from API
            val response = mediaWikiApi.getUserContributions(
                username = username,
                itemLimit = 10,
                continuation = continuationMap
            )

            Timber.d("API Response: ${response.query?.pages?.size ?: 0} pages")
            Timber.d("Continue tokens: ${response.continueToken}")

            // Get next continuation token
            val nextContinuation = response.continueToken?.get("gaicontinue")
            Timber.d("Next gaicontinue: $nextContinuation")

            // Convert pages to contributions
            val contributions = response.query?.pages?.mapNotNull { page ->
                page.toContributionModel()
            } ?: emptyList()

            Timber.d("=== FETCH RESULT ===")
            Timber.d("Fetched ${contributions.size} contributions")
            if (nextContinuation != null) {
                Timber.d("✅ More pages available - gaicontinue: $nextContinuation")
            } else {
                Timber.d("✅ End of pagination")
            }

            Result.success(Pair(contributions, nextContinuation))
        } catch (e: Exception) {
            Timber.e(e, "=== FETCH ERROR ===")
            Result.failure(e)
        }
    }

    /**
     * Convert MediaWiki Page to ContributionModel
     */
    private fun Page.toContributionModel(): ContributionModel? {
        val imageInfo = imageInfo?.firstOrNull() ?: return null
        val pageId = pageId?.toString() ?: return null

        return ContributionModel(
            pageId = pageId,
            filename = title?.removePrefix("File:"),
            thumbUrl = imageInfo.thumbUrl,
            imageUrl = imageInfo.url,
            dateUploaded = parseTimestamp(imageInfo.timestamp),
            description = imageInfo.extMetadata?.imageDescription?.value,
            author = imageInfo.user,
            categories = emptyList(), // Not fetching categories to avoid multi-part continuation
            state = ContributionModel.STATE_COMPLETED
        )
    }

    /**
     * Parse MediaWiki timestamp to Date
     * Format: 2023-12-01T10:30:00Z
     */
    private fun parseTimestamp(timestamp: String?): Date? {
        if (timestamp == null) {
            Timber.w("parseTimestamp: timestamp is null")
            return null
        }
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            val date = format.parse(timestamp)
            Timber.d("parseTimestamp: '$timestamp' -> $date")
            date
        } catch (e: Exception) {
            Timber.e(e, "Error parsing timestamp: $timestamp")
            null
        }
    }
}
