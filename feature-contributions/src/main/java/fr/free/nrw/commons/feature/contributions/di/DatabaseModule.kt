package fr.free.nrw.commons.feature.contributions.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.free.nrw.commons.core.network.NetworkFactory
import fr.free.nrw.commons.feature.contributions.data.local.ContributionDao
import fr.free.nrw.commons.feature.contributions.data.local.ContributionDatabase
import fr.free.nrw.commons.feature.contributions.data.remote.api.MediaWikiApi
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Singleton

/**
 * Hilt module for providing database and network dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val COMMONS_BASE_URL = "https://commons.wikimedia.org/"
    private const val USER_AGENT = "Commons-Android-App/6.1.0 (https://github.com/commons-app/apps-android-commons)"

    @Provides
    @Singleton
    fun provideContributionDatabase(
        @ApplicationContext context: Context
    ): ContributionDatabase {
        return Room.databaseBuilder(
            context,
            ContributionDatabase::class.java,
            ContributionDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideContributionDao(
        database: ContributionDatabase
    ): ContributionDao {
        return database.contributionDao()
    }

    @Provides
    @Singleton
    fun provideMediaWikiApi(): MediaWikiApi {
        // Create User-Agent interceptor (required by MediaWiki API)
        val userAgentInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", USER_AGENT)
                .build()
            chain.proceed(request)
        }

        val retrofit = NetworkFactory.createRetrofit(
            baseUrl = COMMONS_BASE_URL,
            enableLogging = true,
            interceptors = listOf(userAgentInterceptor)
        )
        return retrofit.create(MediaWikiApi::class.java)
    }
}


