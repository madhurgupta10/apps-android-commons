package fr.free.nrw.commons.feature.profile.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.free.nrw.commons.feature.profile.data.local.ProfileDatabase
import fr.free.nrw.commons.feature.profile.data.local.dao.AchievementDao
import fr.free.nrw.commons.feature.profile.data.local.dao.LeaderboardDao
import fr.free.nrw.commons.feature.profile.data.local.dao.UserProfileDao
import javax.inject.Singleton

/**
 * Hilt module providing database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideProfileDatabase(
        @ApplicationContext context: Context
    ): ProfileDatabase {
        return Room.databaseBuilder(
            context,
            ProfileDatabase::class.java,
            ProfileDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // TODO: Add proper migrations in production
            .build()
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(database: ProfileDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(database: ProfileDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Provides
    @Singleton
    fun provideLeaderboardDao(database: ProfileDatabase): LeaderboardDao {
        return database.leaderboardDao()
    }
}

