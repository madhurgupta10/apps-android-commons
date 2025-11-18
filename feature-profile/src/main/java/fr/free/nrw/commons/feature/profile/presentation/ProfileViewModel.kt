package fr.free.nrw.commons.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.free.nrw.commons.feature.contributions.domain.usecase.ObserveContributionsUseCase
import fr.free.nrw.commons.feature.contributions.domain.usecase.RefreshContributionsUseCase
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
    private val observeContributions: ObserveContributionsUseCase,
    private val refreshContributions: RefreshContributionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var profileJob: Job? = null
    private var achievementsJob: Job? = null
    private var leaderboardJob: Job? = null
    private var contributionsJob: Job? = null

    // Default username - hardcoded for development
    private var currentUsername: String = "Syced"

    init {
        setupPeriodicSync()
        loadInitialData()
        loadContributions() // Load real contributions
    }

    fun setUsername(username: String) {
        currentUsername = username
        loadInitialData()
        loadContributions() // Reload contributions for new user
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
     * Load user contributions from feature-contributions module
     */
    private fun loadContributions() {
        contributionsJob?.cancel()
        contributionsJob = viewModelScope.launch {
            _uiState.update { it.copy(contributionsError = null) }

            try {
                Timber.d("Loading contributions for $currentUsername")

                // First, trigger a refresh to fetch from network
                refreshContributions(currentUsername).fold(
                    onSuccess = {
                        Timber.d("Contributions refreshed successfully")
                    },
                    onFailure = { error ->
                        Timber.e(error, "Failed to refresh contributions")
                    }
                )

                // Then observe from database
                observeContributions(currentUsername).collect { contributions ->
                    Timber.d("Received ${contributions.size} contributions from DB")
                    _uiState.update {
                        it.copy(
                            contributions = contributions.map { contribution ->
                                // Convert ContributionModel to Contribution for UI
                                Contribution(
                                    id = contribution.pageId,
                                    title = contribution.filename ?: "Untitled",
                                    thumbnailUrl = contribution.thumbUrl ?: contribution.imageUrl,
                                    uploadDate = contribution.dateUploaded?.time ?: 0L,
                                    views = 0, // Views not available in ContributionModel yet
                                    aspectRatio = 1f, // Default square aspect ratio
                                    width = 640, // Default thumbnail width
                                    height = 640 // Default thumbnail height
                                )
                            },
                            contributionsError = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        contributions = emptyList(),
                        contributionsError = e.message ?: "Failed to load contributions"
                    )
                }
                Timber.e(e, "Exception loading contributions")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        profileJob?.cancel()
        achievementsJob?.cancel()
        leaderboardJob?.cancel()
    }
}

