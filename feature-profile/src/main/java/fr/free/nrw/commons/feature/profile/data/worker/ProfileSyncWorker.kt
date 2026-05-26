package fr.free.nrw.commons.feature.profile.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardCategory
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardDuration
import fr.free.nrw.commons.feature.profile.domain.repository.ProfileRepository
import timber.log.Timber

/**
 * Background worker to sync profile data periodically.
 * Runs every 6 hours to keep data fresh.
 */
@HiltWorker
class ProfileSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ProfileRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "profile_sync_work"
        const val KEY_USERNAME = "username"
    }

    override suspend fun doWork(): Result {
        return try {
            val username = inputData.getString(KEY_USERNAME) ?: return Result.failure()

            // Sync user profile
            val profileResult = repository.refreshUserProfile(username)
            if (profileResult.isFailure) {
                Timber.e(profileResult.exceptionOrNull(), "Failed to sync user profile")
            }

            // Sync achievements
            val achievementsResult = repository.refreshAchievements(username)
            if (achievementsResult.isFailure) {
                Timber.e(achievementsResult.exceptionOrNull(), "Failed to sync achievements")
            }

            // Sync leaderboard for different categories
            syncLeaderboards(username)

            if (profileResult.isSuccess || achievementsResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "Profile sync worker failed")
            Result.retry()
        }
    }

    private suspend fun syncLeaderboards(username: String) {
        val categories = listOf(
            LeaderboardCategory.UPLOAD,
            LeaderboardCategory.USED,
            LeaderboardCategory.THANKS
        )

        val durations = listOf(
            LeaderboardDuration.WEEKLY,
            LeaderboardDuration.MONTHLY
        )

        categories.forEach { category ->
            durations.forEach { duration ->
                try {
                    repository.refreshLeaderboard(category, duration, username)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync leaderboard: $category, $duration")
                }
            }
        }
    }
}

