package fr.free.nrw.commons.contributions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.free.nrw.commons.feature.contributions.domain.model.ContributionModel
import fr.free.nrw.commons.feature.contributions.domain.usecase.GetUserContributionsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Contributions Grid using Jetpack Compose.
 * Uses Paging 3 for efficient pagination with the new grid UI.
 */
@HiltViewModel
class ContributionsGridViewModel @Inject constructor(
    private val getUserContributions: GetUserContributionsUseCase
) : ViewModel() {

    private val usernameFlow = MutableSharedFlow<String>(replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val contributionsPagingFlow: Flow<PagingData<ContributionModel>> = usernameFlow
        .flatMapLatest { username ->
            Timber.d("Loading contributions for user: $username")
            getUserContributions(username)
        }
        .cachedIn(viewModelScope)

    private var currentUsername: String? = null

    /**
     * Load contributions for a specific user
     * @param username The username to load contributions for
     */
    fun loadContributions(username: String) {
        if (username == currentUsername) return // Avoid reloading for same user

        currentUsername = username
        Timber.d("Setting up contributions for user: $username")
        usernameFlow.tryEmit(username)
    }

    /**
     * Refresh contributions by reloading for current user
     */
    fun refresh() {
        currentUsername?.let { username ->
            Timber.d("Refreshing contributions for user: $username")
            usernameFlow.tryEmit(username)
        }
    }
}
