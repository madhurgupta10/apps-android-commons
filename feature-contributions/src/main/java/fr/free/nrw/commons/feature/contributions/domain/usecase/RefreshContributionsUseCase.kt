package fr.free.nrw.commons.feature.contributions.domain.usecase

import fr.free.nrw.commons.feature.contributions.domain.repository.ContributionRepository
import javax.inject.Inject

/**
 * Use case for refreshing user contributions
 * Clears local cache and fetches fresh data from network
 */
class RefreshContributionsUseCase @Inject constructor(
    private val repository: ContributionRepository
) {
    /**
     * Refresh contributions for a user
     *
     * @param username Username to refresh contributions for
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(username: String): Result<Unit> {
        return repository.refreshContributions(username)
    }
}

