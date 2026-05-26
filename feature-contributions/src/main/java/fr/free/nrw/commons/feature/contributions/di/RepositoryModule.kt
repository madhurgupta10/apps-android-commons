package fr.free.nrw.commons.feature.contributions.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.free.nrw.commons.feature.contributions.data.local.ContributionDao
import fr.free.nrw.commons.feature.contributions.data.local.ContributionDatabase
import fr.free.nrw.commons.feature.contributions.data.remote.ContributionRemoteDataSource
import fr.free.nrw.commons.feature.contributions.data.repository.ContributionRepositoryImpl
import fr.free.nrw.commons.feature.contributions.domain.repository.ContributionRepository
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideContributionRepository(
        contributionDao: ContributionDao,
        database: ContributionDatabase,
        remoteDataSource: ContributionRemoteDataSource
    ): ContributionRepository {
        return ContributionRepositoryImpl(
            contributionDao = contributionDao,
            database = database,
            remoteDataSource = remoteDataSource
        )
    }
}

