package fr.free.nrw.commons.feature.contributions.domain.usecase

import androidx.paging.PagingData
import fr.free.nrw.commons.feature.contributions.domain.model.ContributionModel
import fr.free.nrw.commons.feature.contributions.domain.repository.ContributionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching user contributions with pagination
 * This follows clean architecture principles by separating business logic
 */
class GetUserContributionsUseCase @Inject constructor(
    private val repository: ContributionRepository
) {
    /**
     * Get paginated contributions for a user
     * Data is always fetched from DB (single source of truth)
     * Network data automatically updates DB via RemoteMediator
     *
     * @param username Username to fetch contributions for
     * @return Flow of PagingData containing contributions
     */
    operator fun invoke(username: String): Flow<PagingData<ContributionModel>> {
        return repository.getUserContributions(username)
    }
}


