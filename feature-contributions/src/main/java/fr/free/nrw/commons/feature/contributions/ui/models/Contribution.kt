package fr.free.nrw.commons.feature.contributions.ui.models

/**
 * Represents a user contribution for display in UI components.
 * This is a UI model used for rendering contributions in grids, lists, etc.
 */
data class Contribution(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val uploadDate: Long,
    val views: Int,
    val aspectRatio: Float = 1f, // width/height for smart grid sizing
    val width: Int = 0,
    val height: Int = 0
)

/**
 * Groups contributions by timeline (day, week, month, year)
 */
data class TimelineGroup(
    val label: String, // e.g., "Today", "1 year ago", "December 2024"
    val timestamp: Long,
    val contributions: List<Contribution>
)

