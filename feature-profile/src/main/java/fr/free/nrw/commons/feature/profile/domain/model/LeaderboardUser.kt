package fr.free.nrw.commons.feature.profile.domain.model

/**
 * Domain model for a leaderboard user entry.
 */
data class LeaderboardUser(
    val username: String,
    val avatarUrl: String?,
    val rank: Int,
    val score: Int,
    val isCurrentUser: Boolean = false
) {
    val displayRank: String
        get() = when (rank) {
            1 -> "🥇"
            2 -> "🥈"
            3 -> "🥉"
            else -> rank.toString()
        }
}

