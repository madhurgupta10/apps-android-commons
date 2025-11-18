package fr.free.nrw.commons.feature.contributions.domain.model

import java.util.Date

/**
 * Domain model for Contribution
 * This is a simplified model for the feature module that focuses on displaying contributions
 */
data class ContributionModel(
    val pageId: String,
    val filename: String?,
    val thumbUrl: String?,
    val imageUrl: String?,
    val dateUploaded: Date?,
    val description: String?,
    val author: String?,
    val categories: List<String>,
    val state: Int
) {
    companion object {
        const val STATE_COMPLETED = -1
        const val STATE_FAILED = 1
        const val STATE_QUEUED = 2
        const val STATE_IN_PROGRESS = 3
        const val STATE_PAUSED = 4
    }
}

