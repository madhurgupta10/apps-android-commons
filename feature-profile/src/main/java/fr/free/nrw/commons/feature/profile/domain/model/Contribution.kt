package fr.free.nrw.commons.feature.profile.domain.model

import java.util.Date

/**
 * Domain model representing a user's contribution to Wikimedia Commons.
 */
data class Contribution(
    val id: String,
    val filename: String,
    val thumbnailUrl: String?,
    val imageUrl: String,
    val uploadDate: Date,
    val categories: List<String>,
    val depictions: List<String>,
    val description: String?
)

