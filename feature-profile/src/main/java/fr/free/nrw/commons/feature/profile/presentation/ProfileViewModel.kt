package fr.free.nrw.commons.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.free.nrw.commons.feature.contributions.domain.usecase.GetUserContributionsUseCase
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardCategory
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardDuration
import fr.free.nrw.commons.feature.profile.data.worker.ProfileSyncWorker
import fr.free.nrw.commons.feature.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ViewModel for the Profile screen.
 * Manages UI state and business logic using Kotlin Flows and Coroutines.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val workManager: WorkManager,
    private val getUserContributions: GetUserContributionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var profileJob: Job? = null
    private var achievementsJob: Job? = null
    private var leaderboardJob: Job? = null

    // Track if contributions have been loaded to enable lazy loading
    private var contributionsInitialized = false

    // Default username - use a username that has actual contributions
    private var currentUsername: String = "Madhurgupta10"

    init {
        setupPeriodicSync()
        loadInitialData()
        // Don't load contributions here - will be loaded lazily when contributions tab is accessed
    }

    fun setUsername(username: String) {
        currentUsername = username
        loadInitialData()
        // Contributions will be reloaded automatically when paging flow is re-collected
    }

    private fun loadInitialData() {
        loadUserProfile()
        loadAchievements()
        loadLeaderboard()
    }

    private fun loadUserProfile() {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getUserProfile(currentUsername)
                .collect { result ->
                    result.onSuccess { profile ->
                        _uiState.update {
                            it.copy(
                                userProfile = profile,
                                isLoading = false,
                                error = null
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load profile"
                            )
                        }
                        Timber.e(error, "Failed to load user profile")
                    }
                }
        }
    }

    private fun loadAchievements() {
        achievementsJob?.cancel()
        achievementsJob = viewModelScope.launch {
            combine(
                repository.getAchievements(currentUsername),
                repository.getUnlockedAchievementsCount(currentUsername)
            ) { achievementsResult, unlockedCount ->
                Pair(achievementsResult, unlockedCount)
            }.collect { (result, unlockedCount) ->
                result.onSuccess { achievements ->
                    _uiState.update {
                        it.copy(
                            achievements = achievements,
                            unlockedAchievementsCount = unlockedCount,
                            achievementsError = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            achievementsError = error.message ?: "Failed to load achievements"
                        )
                    }
                    Timber.e(error, "Failed to load achievements")
                }
            }
        }
    }

    private fun loadLeaderboard() {
        leaderboardJob?.cancel()
        leaderboardJob = viewModelScope.launch {
            val category = _uiState.value.selectedCategory
            val duration = _uiState.value.selectedDuration

            combine(
                repository.getLeaderboard(category, duration, currentUsername),
                repository.getUserRank(currentUsername, category, duration)
            ) { leaderboardResult, userRankResult ->
                Pair(leaderboardResult, userRankResult)
            }.collect { (leaderboardResult, userRankResult) ->
                leaderboardResult.onSuccess { leaderboard ->
                    userRankResult.onSuccess { userRank ->
                        _uiState.update {
                            it.copy(
                                leaderboard = leaderboard,
                                userRank = userRank,
                                leaderboardError = null
                            )
                        }
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            leaderboardError = error.message ?: "Failed to load leaderboard"
                        )
                    }
                    Timber.e(error, "Failed to load leaderboard")
                }
            }
        }
    }

    fun onTabSelected(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }

        // Lazily initialize contributions paging flow when Contributions tab is first accessed
        if (tab == ProfileTab.CONTRIBUTIONS && !contributionsInitialized) {
            loadContributions()
            contributionsInitialized = true
        }
    }

    fun onCategorySelected(category: LeaderboardCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadLeaderboard()
    }

    fun onDurationSelected(duration: LeaderboardDuration) {
        _uiState.update { it.copy(selectedDuration = duration) }
        loadLeaderboard()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            // Refresh all data
            launch {
                repository.refreshUserProfile(currentUsername)
                    .onFailure { Timber.e(it, "Failed to refresh profile") }
            }
            launch {
                repository.refreshAchievements(currentUsername)
                    .onFailure { Timber.e(it, "Failed to refresh achievements") }
            }
            launch {
                val category = _uiState.value.selectedCategory
                val duration = _uiState.value.selectedDuration
                repository.refreshLeaderboard(category, duration, currentUsername)
                    .onFailure { Timber.e(it, "Failed to refresh leaderboard") }
            }

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun setupPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<ProfileSyncWorker>(
            repeatInterval = 6,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInputData(
                workDataOf(ProfileSyncWorker.KEY_USERNAME to currentUsername)
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            ProfileSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    /**
     * Load user contributions with pagination support
     * Uses RemoteMediator to automatically fetch from network when needed
     */
    private fun loadContributions() {
        try {
            Timber.d("Setting up paginated contributions for $currentUsername")

            // Get the paginated flow from repository
            val contributionsPagingFlow = getUserContributions(currentUsername)

            // Update UI state with the paging flow
            _uiState.update {
                it.copy(
                    contributionsPagingFlow = contributionsPagingFlow,
                    contributionsError = null
                )
            }

            Timber.d("Paginated contributions flow set up successfully")
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    contributionsError = e.message ?: "Failed to load contributions"
                )
            }
            Timber.e(e, "Exception loading contributions")
        }
    }

    override fun onCleared() {
        super.onCleared()
        profileJob?.cancel()
        achievementsJob?.cancel()
        leaderboardJob?.cancel()
    }
}

