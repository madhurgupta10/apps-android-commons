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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the logged-in Explore grid tabs (Featured Images, Mobile Uploads).
 * Each tab gets its own instance keyed by category name via ViewModelProvider(activity)[key, ...].
 */
@HiltViewModel
class ExploreGridViewModel @Inject constructor(
    private val mediaWikiApi: MediaWikiApi
) : ViewModel() {

    private val categoryFlow = MutableSharedFlow<String>(replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val contributionsPagingFlow: Flow<PagingData<ContributionModel>> = categoryFlow
        .flatMapLatest { categoryTitle ->
            Timber.d("ExploreGridViewModel: loading $categoryTitle")
            Pager(
                config = PagingConfig(pageSize = 30, prefetchDistance = 10, enablePlaceholders = false),
                pagingSourceFactory = { ExploreCategoryPagingSource(mediaWikiApi, categoryTitle) }
            ).flow
        }
        .cachedIn(viewModelScope)

    /** In-memory snapshot updated from Compose [SideEffect]; consumed by [getMediaAtPosition]. */
    var snapshot: List<ContributionModel> = emptyList()
        private set

    private var loadedCategory: String? = null

    /** Trigger data loading for [categoryName] (without "Category:" prefix). */
    fun loadCategory(categoryName: String) {
        val categoryTitle = "Category:$categoryName"
        if (loadedCategory == categoryTitle) return
        loadedCategory = categoryTitle
        categoryFlow.tryEmit(categoryTitle)
    }

    fun updateSnapshot(items: List<ContributionModel>) {
        snapshot = items
    }
}

