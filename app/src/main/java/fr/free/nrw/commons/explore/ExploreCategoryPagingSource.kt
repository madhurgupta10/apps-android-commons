package fr.free.nrw.commons.explore

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.free.nrw.commons.feature.contributions.data.remote.api.MediaWikiApi
import fr.free.nrw.commons.feature.contributions.data.remote.api.Page
import fr.free.nrw.commons.feature.contributions.domain.model.ContributionModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * PagingSource that surfaces media from a Commons category for the logged-out Explore grid.
 * Uses the same MediaWikiApi as feature-contributions (iiurlwidth=640) so thumbUrls are always
 * populated — no entity lookup required.
 */
class ExploreCategoryPagingSource(
    private val mediaWikiApi: MediaWikiApi,
    private val categoryTitle: String // e.g. "Category:Featured_pictures_on_Wikimedia_Commons"
) : PagingSource<String, ContributionModel>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, ContributionModel> {
        return try {
            val continuation = params.key // null on first load
            val continuationMap = if (continuation != null) {
                mapOf("gcmcontinue" to continuation)
            } else {
                emptyMap()
            }

            Timber.d("ExploreCategoryPagingSource: loading $categoryTitle, continuation=$continuation")

            val response = mediaWikiApi.getCategoryMedia(
                categoryTitle = categoryTitle,
                itemLimit = 30,
                continuation = continuationMap
            )

            val items = (response.query?.pages?.mapNotNull { it.toContributionModel() } ?: emptyList())
                .sortedByDescending { it.dateUploaded?.time ?: 0L }
            val nextKey = response.continueToken?.get("gcmcontinue")

            Timber.d("ExploreCategoryPagingSource: got ${items.size} items, nextKey=$nextKey")

            LoadResult.Page(
                data = items,
                prevKey = null, // only forward pagination
                nextKey = nextKey
            )
        } catch (e: Exception) {
            Timber.e(e, "ExploreCategoryPagingSource: error loading $categoryTitle")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, ContributionModel>): String? = null
}

private val timestampFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun Page.toContributionModel(): ContributionModel? {
    val info = imageInfo?.firstOrNull() ?: return null
    val id = pageId?.toString() ?: return null
    val thumb = info.thumbUrl?.takeIf { it.isNotBlank() }
    val original = info.url?.takeIf { it.isNotBlank() }
    val displayUrl = thumb ?: original ?: return null

    return ContributionModel(
        pageId = id,
        filename = title?.removePrefix("File:"),
        thumbUrl = displayUrl,
        imageUrl = original ?: displayUrl,
        dateUploaded = info.timestamp?.let {
            try { timestampFormat.parse(it) } catch (e: Exception) { null }
        },
        description = info.extMetadata?.imageDescription?.value,
        author = info.user,
        categories = emptyList(),
        state = ContributionModel.STATE_COMPLETED
    )
}
