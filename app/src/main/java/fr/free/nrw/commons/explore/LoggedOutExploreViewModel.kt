package fr.free.nrw.commons.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.free.nrw.commons.feature.contributions.data.remote.api.MediaWikiApi
import fr.free.nrw.commons.feature.contributions.domain.model.ContributionModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ViewModel for the logged-out Explore grid.
 * Feeds the Compose grid with Featured category media via the same MediaWikiApi used by
 * feature-contributions (iiurlwidth=640) so thumbnail URLs are always populated.
 */
@HiltViewModel
class LoggedOutExploreViewModel @Inject constructor(
    private val mediaWikiApi: MediaWikiApi
) : ViewModel() {

    private val categoryTitle = "Category:$FEATURED_CATEGORY"

    val contributionsPagingFlow: Flow<PagingData<ContributionModel>> = Pager(
        config = PagingConfig(pageSize = 30, prefetchDistance = 10, enablePlaceholders = false),
        pagingSourceFactory = { ExploreCategoryPagingSource(mediaWikiApi, categoryTitle) }
    ).flow.cachedIn(viewModelScope)

    companion object {
        const val FEATURED_CATEGORY = "Featured_pictures_on_Wikimedia_Commons"
    }
}
