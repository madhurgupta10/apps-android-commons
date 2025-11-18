package fr.free.nrw.commons.feature.contributions.domain.usecase

import fr.free.nrw.commons.feature.contributions.domain.model.ContributionModel
import fr.free.nrw.commons.feature.contributions.domain.repository.ContributionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing user contributions as a simple Flow (non-paginated)
 * Useful for observing real-time changes to contributions
 */
class ObserveContributionsUseCase @Inject constructor(
    private val repository: ContributionRepository
) {
    /**
     * Observe contributions for a user
     *
     * @param username Username to observe contributions for
     * @return Flow of list of contributions
     */
    operator fun invoke(username: String): Flow<List<ContributionModel>> {
        return repository.getUserContributionsFlow(username)
    }
}

