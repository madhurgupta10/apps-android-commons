package fr.free.nrw.commons.feature.contributions.data.remote

import fr.free.nrw.commons.feature.contributions.data.remote.api.MediaWikiApi
import fr.free.nrw.commons.feature.contributions.data.remote.api.Page
import fr.free.nrw.commons.feature.contributions.domain.model.ContributionModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for fetching contributions from MediaWiki API
 * Uses core-network module for network operations
 */
@Singleton
class ContributionRemoteDataSource @Inject constructor(
    private val mediaWikiApi: MediaWikiApi
) {
    /**
     * Fetch contributions for a user from the remote API
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
            val continuationMap = if (continuation != null) {
                mapOf("gaicontinue" to continuation, "continue" to "-||")
            } else {
                emptyMap()
            }

            Timber.d("Calling MediaWiki API...")

            // Fetch from API
            val response = mediaWikiApi.getUserContributions(
                username = username,
                itemLimit = 10,
                continuation = continuationMap
            )

            Timber.d("API Response received, pages: ${response.query?.pages?.size ?: 0}")

            // Convert to domain models
            val contributions = response.query?.pages?.mapNotNull { page ->
                Timber.d("Processing page: ${page.title}")
                page.toContributionModel()
            } ?: emptyList()

            // Get next continuation token
            val nextContinuation = response.continueToken?.gaiContinue

            Timber.d("=== FETCH RESULT ===")
            Timber.d("Fetched ${contributions.size} contributions, next continuation: $nextContinuation")
            contributions.forEach { c ->
                Timber.d("  - ${c.filename}")
            }

            Result.success(Pair(contributions, nextContinuation))
        } catch (e: Exception) {
            Timber.e(e, "=== FETCH ERROR ===")
            Timber.e(e, "Error details: ${e.message}")
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
            categories = categories?.mapNotNull { it.title?.removePrefix("Category:") } ?: emptyList(),
            state = ContributionModel.STATE_COMPLETED
        )
    }

    /**
     * Parse MediaWiki timestamp to Date
     * Format: 2023-12-01T10:30:00Z
     */
    private fun parseTimestamp(timestamp: String?): Date? {
        if (timestamp == null) return null
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            format.parse(timestamp)
        } catch (e: Exception) {
            Timber.e(e, "Error parsing timestamp: $timestamp")
            null
        }
    }
}
