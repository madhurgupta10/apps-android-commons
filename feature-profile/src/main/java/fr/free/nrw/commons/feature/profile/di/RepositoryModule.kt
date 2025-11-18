package fr.free.nrw.commons.feature.profile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.free.nrw.commons.feature.profile.data.repository.ProfileRepositoryImpl
import fr.free.nrw.commons.feature.profile.domain.repository.ProfileRepository
import javax.inject.Singleton

/**
 * Hilt module binding repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository
}

