package fr.free.nrw.commons.feature.contributions.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import fr.free.nrw.commons.feature.contributions.data.local.ContributionDatabase
import fr.free.nrw.commons.feature.contributions.data.local.ContributionEntity
import timber.log.Timber
import javax.inject.Inject

/**
 * RemoteMediator for handling pagination and caching of contributions
 * Implements the single source of truth pattern where DB is always the source
 */
@OptIn(ExperimentalPagingApi::class)
class ContributionRemoteMediator @Inject constructor(
    private val username: String,
    private val database: ContributionDatabase,
    private val remoteDataSource: ContributionRemoteDataSource
) : RemoteMediator<Int, ContributionEntity>() {

    private var continuationToken: String? = null
    private var hasMorePages = true

    override suspend fun initialize(): InitializeAction = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ContributionEntity>
    ): MediatorResult {
        return try {
            // Don't load if we've reached the end
            if (loadType == LoadType.APPEND && !hasMorePages) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            // Clear data on refresh - do it before fetching so stale data with null thumbUrls
            // doesn't linger if the network call takes time
            if (loadType == LoadType.REFRESH) {
                continuationToken = null
                hasMorePages = true
                database.withTransaction {
                    database.contributionDao().deleteContributionsByUser(username)
                }
            }

            // Skip if we're prepending (we don't support backward pagination)
            if (loadType == LoadType.PREPEND) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            Timber.d("Loading contributions for user: $username, loadType: $loadType, continuation: $continuationToken")

            // Fetch from network
            val result = remoteDataSource.fetchUserContributions(
                username = username,
                continuation = continuationToken
            )

            result.fold(
                onSuccess = { (contributions, nextContinuation) ->
                    Timber.d("Fetched ${contributions.size} contributions, nextContinuation: $nextContinuation")

                    // Update state
                    hasMorePages = nextContinuation != null
                    continuationToken = nextContinuation

                    // Save to database
                    database.withTransaction {

                        // Convert to entities and insert
                        val entities = contributions.map {
                            ContributionEntity.fromModel(it, username)
                        }
                        database.contributionDao().insertContributions(entities)
                    }

                    MediatorResult.Success(endOfPaginationReached = !hasMorePages)
                },
                onFailure = { error ->
                    Timber.e(error, "Error loading contributions")
                    MediatorResult.Error(error)
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Exception while loading contributions")
            MediatorResult.Error(e)
        }
    }
}

