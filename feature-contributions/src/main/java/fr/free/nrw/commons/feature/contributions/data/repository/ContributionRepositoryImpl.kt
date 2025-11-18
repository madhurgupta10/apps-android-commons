package fr.free.nrw.commons.feature.contributions.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import fr.free.nrw.commons.feature.contributions.data.local.ContributionDao
import fr.free.nrw.commons.feature.contributions.data.local.ContributionDatabase
import fr.free.nrw.commons.feature.contributions.data.remote.ContributionRemoteDataSource
import fr.free.nrw.commons.feature.contributions.data.remote.ContributionRemoteMediator
import fr.free.nrw.commons.feature.contributions.domain.model.ContributionModel
import fr.free.nrw.commons.feature.contributions.domain.repository.ContributionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of ContributionRepository
 * Implements single source of truth pattern with DB as the source
 */
class ContributionRepositoryImpl @Inject constructor(
    private val contributionDao: ContributionDao,
    private val database: ContributionDatabase,
    private val remoteDataSource: ContributionRemoteDataSource
) : ContributionRepository {

    companion object {
        private const val PAGE_SIZE = 10
        private const val PREFETCH_DISTANCE = 5
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getUserContributions(username: String): Flow<PagingData<ContributionModel>> {
        Timber.d("Getting contributions for user: $username")

        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            remoteMediator = ContributionRemoteMediator(
                username = username,
                database = database,
                remoteDataSource = remoteDataSource
            ),
            pagingSourceFactory = {
                contributionDao.getContributionsByUser(username)
            }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toModel()
            }
        }
    }

    override fun getUserContributionsFlow(username: String): Flow<List<ContributionModel>> {
        return contributionDao.getContributionsByUserFlow(username)
            .map { entities ->
                entities.map { it.toModel() }
            }
    }

    override suspend fun refreshContributions(username: String): Result<Unit> {
        return try {
            Timber.d("Refreshing contributions for user: $username")
            // Clear existing data
            contributionDao.deleteContributionsByUser(username)

            // Fetch fresh data from network
            val result = remoteDataSource.fetchUserContributions(username, null)

            result.fold(
                onSuccess = { (contributions, _) ->
                    // Save to database
                    val entities = contributions.map { model ->
                        fr.free.nrw.commons.feature.contributions.data.local.ContributionEntity
                            .fromModel(model, username)
                    }
                    contributionDao.insertContributions(entities)
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to refresh contributions")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Exception while refreshing contributions")
            Result.failure(e)
        }
    }

    override suspend fun getContributionCount(username: String): Int {
        return contributionDao.getContributionCount(username)
    }

    override suspend fun clearUserContributions(username: String) {
        contributionDao.deleteContributionsByUser(username)
    }
}

