package fr.free.nrw.commons.feature.contributions.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import fr.free.nrw.commons.feature.contributions.domain.model.ContributionModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val GRID_COLUMNS = 3


/**
 * Data class for timeline grouping
 */
internal data class TimelineGroup(
    val label: String,
    val timestamp: Long,
    val startIndex: Int,
    val endIndex: Int
)

/**
 * Calculates optimal spans for all items to ensure no gaps (Google Photos style).
 * Uses prime number-based algorithm to create visually pleasing patterns.
 */
private fun calculateSpans(totalSize: Int): List<SpanLayout> {
    if (totalSize == 0) return emptyList()

    val spans = mutableListOf<SpanLayout>()

    // Handle special cases for small counts
    when (totalSize) {
        1 -> {
            spans.add(SpanLayout(0, 3))
            return spans
        }
        2 -> {
            spans.add(SpanLayout(0, 3))
            spans.add(SpanLayout(1, 3))
            return spans
        }
        3 -> {
            spans.add(SpanLayout(0, 1))
            spans.add(SpanLayout(1, 1))
            spans.add(SpanLayout(2, 1))
            return spans
        }
        4 -> {
            spans.add(SpanLayout(0, 2))
            spans.add(SpanLayout(1, 1))
            spans.add(SpanLayout(2, 2))
            spans.add(SpanLayout(3, 1))
            return spans
        }
        5 -> {
            spans.add(SpanLayout(0, 2))
            spans.add(SpanLayout(1, 1))
            spans.add(SpanLayout(2, 1))
            spans.add(SpanLayout(3, 1))
            spans.add(SpanLayout(4, 1))
            return spans
        }
    }

    return generatePrimeBasedPattern(totalSize)
}

/**
 * Generates a span pattern based on prime number decomposition.
 */
private fun generatePrimeBasedPattern(totalSize: Int): List<SpanLayout> {
    val spans = mutableListOf<SpanLayout>()
    val remainder = totalSize % 3

    val completeRows = when (remainder) {
        0 -> totalSize / 3
        1 -> (totalSize - 4) / 3
        2 -> (totalSize - 5) / 3
        else -> 0
    }

    var currentIndex = 0

    for (rowIndex in 0 until completeRows) {
        when {
            rowIndex % 7 == 0 && currentIndex + 3 < totalSize -> {
                spans.add(SpanLayout(currentIndex++, 3))
            }
            rowIndex % 5 == 0 -> {
                spans.add(SpanLayout(currentIndex++, 2))
                spans.add(SpanLayout(currentIndex++, 1))
            }
            rowIndex % 3 == 0 -> {
                spans.add(SpanLayout(currentIndex++, 1))
                spans.add(SpanLayout(currentIndex++, 2))
            }
            else -> {
                spans.add(SpanLayout(currentIndex++, 1))
                spans.add(SpanLayout(currentIndex++, 1))
                spans.add(SpanLayout(currentIndex++, 1))
            }
        }
    }

    when (remainder) {
        0 -> {
            while (currentIndex < totalSize) {
                when ((currentIndex / 3) % 3) {
                    0 -> {
                        spans.add(SpanLayout(currentIndex++, 1))
                        spans.add(SpanLayout(currentIndex++, 1))
                        spans.add(SpanLayout(currentIndex++, 1))
                    }
                    1 -> {
                        spans.add(SpanLayout(currentIndex++, 2))
                        if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 1))
                        if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 1))
                        if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 1))
                    }
                    else -> {
                        spans.add(SpanLayout(currentIndex++, 1))
                        if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 2))
                        if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 1))
                        if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 1))
                    }
                }
            }
        }
        1 -> {
            if (currentIndex < totalSize) {
                spans.add(SpanLayout(currentIndex++, 2))
                if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 1))
                if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 3))
            }
        }
        2 -> {
            if (currentIndex < totalSize) {
                spans.add(SpanLayout(currentIndex++, 2))
                if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 1))
                if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 1))
                if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 1))
            }
        }
    }

    return spans
}

/**
 * Groups contributions by timeline periods for LazyPagingItems
 */
private fun groupByTimelinePaging(
    contributions: LazyPagingItems<ContributionModel>
): List<TimelineGroup> {
    if (contributions.itemCount == 0) return emptyList()

    val now = System.currentTimeMillis()
    val groups = mutableListOf<TimelineGroup>()

    var currentLabel = ""
    var currentStartIndex = 0
    var currentTimestamp = 0L

    for (index in 0 until contributions.itemCount) {
        val contribution = contributions[index] ?: continue
        val timestamp = contribution.dateUploaded?.time ?: System.currentTimeMillis()

        // Debug logging
        if (contribution.dateUploaded == null) {
            Timber.w("Contribution ${contribution.filename} has null dateUploaded, using current time")
        } else {
            Timber.d("Contribution ${contribution.filename} uploaded at: ${contribution.dateUploaded}")
        }

        val label = getTimelineLabel(timestamp, now)

        if (label != currentLabel) {
            if (currentLabel.isNotEmpty()) {
                groups.add(TimelineGroup(currentLabel, currentTimestamp, currentStartIndex, index - 1))
            }
            currentLabel = label
            currentStartIndex = index
            currentTimestamp = timestamp
        }
    }

    if (currentLabel.isNotEmpty() && currentStartIndex < contributions.itemCount) {
        groups.add(TimelineGroup(currentLabel, currentTimestamp, currentStartIndex, contributions.itemCount - 1))
    }

    return groups
}

/**
 * Generates timeline label like "Today", "1 week ago", "December 2024"
 */
private fun getTimelineLabel(timestamp: Long, now: Long): String {
    // Create calendar instances for proper date comparison
    val nowCal = Calendar.getInstance().apply { timeInMillis = now }
    val timestampCal = Calendar.getInstance().apply { timeInMillis = timestamp }

    // Calculate the difference in calendar days
    val nowDay = nowCal.get(Calendar.DAY_OF_YEAR)
    val nowYear = nowCal.get(Calendar.YEAR)
    val timestampDay = timestampCal.get(Calendar.DAY_OF_YEAR)
    val timestampYear = timestampCal.get(Calendar.YEAR)

    // Calculate actual day difference
    val daysDiff = if (nowYear == timestampYear) {
        nowDay - timestampDay
    } else {
        // Calculate across year boundaries
        val daysInTimestampYear = if (timestampYear % 4 == 0 && (timestampYear % 100 != 0 || timestampYear % 400 == 0)) 366 else 365
        val daysRemainingInTimestampYear = daysInTimestampYear - timestampDay
        var totalDays = daysRemainingInTimestampYear + nowDay

        // Add days for years in between
        for (year in (timestampYear + 1) until nowYear) {
            totalDays += if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 366 else 365
        }
        totalDays
    }

    val label = when {
        daysDiff == 0 -> "Today"
        daysDiff == 1 -> "Yesterday"
        daysDiff < 7 -> "$daysDiff days ago"
        daysDiff < 14 -> "1 week ago"
        daysDiff < 30 -> "${daysDiff / 7} weeks ago"
        daysDiff < 60 -> "1 month ago"
        daysDiff < 365 -> "${daysDiff / 30} months ago"
        daysDiff < 730 -> "1 year ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }

    Timber.d("Timeline: timestamp=$timestamp (${Date(timestamp)}), now=$now, nowYear=$nowYear, nowDay=$nowDay, timestampYear=$timestampYear, timestampDay=$timestampDay, daysDiff=$daysDiff, label=$label")

    return label
}

/**
 * Contributions grid that works with Paging3 LazyPagingItems with dynamic Google Photos-style layout
 * Automatically loads more data as user scrolls
 */
@Composable
fun ContributionsPagingGrid(
    contributions: LazyPagingItems<ContributionModel>,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyGridState()

    // Group contributions by timeline - recalculate when items change
    val timelineGroups by remember {
        derivedStateOf {
            groupByTimelinePaging(contributions)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Handle loading state at the start
        when (contributions.loadState.refresh) {
            is LoadState.Loading -> {
                item(span = { GridItemSpan(GRID_COLUMNS) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is LoadState.Error -> {
                val error = (contributions.loadState.refresh as LoadState.Error).error
                item(span = { GridItemSpan(GRID_COLUMNS) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${error.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            else -> {
                // Display contributions with timeline groups and dynamic spans
                timelineGroups.forEach { group ->
                    val groupSize = group.endIndex - group.startIndex + 1
                    val spanLayouts = calculateSpans(groupSize)

                    // Timeline header
                    item(span = { GridItemSpan(GRID_COLUMNS) }) {
                        TimelineHeader(label = group.label)
                    }

                    // Items in this group
                    items(
                        count = groupSize,
                        key = { localIndex ->
                            val globalIndex = group.startIndex + localIndex
                            contributions.itemKey { it.pageId }(globalIndex)
                        },
                        span = { localIndex ->
                            val span = spanLayouts.getOrNull(localIndex)?.span ?: 1
                            GridItemSpan(span.coerceIn(1, GRID_COLUMNS))
                        }
                    ) { localIndex ->
                        val globalIndex = group.startIndex + localIndex
                        val contribution = contributions[globalIndex]
                        val span = spanLayouts.getOrNull(localIndex)?.span ?: 1

                        if (contribution != null) {
                            ContributionPagingCard(
                                contribution = contribution,
                                span = span,
                                imageLoader = imageLoader
                            )
                        }
                    }
                }
            }
        }

        // Handle loading more at the end
        when (contributions.loadState.append) {
            is LoadState.Loading -> {
                item(span = { GridItemSpan(GRID_COLUMNS) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is LoadState.Error -> {
                val error = (contributions.loadState.append as LoadState.Error).error
                item(span = { GridItemSpan(GRID_COLUMNS) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading more: ${error.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            else -> Unit
        }
    }
}

/**
 * Timeline header for grouping contributions (like Google Photos)
 */
@Composable
private fun TimelineHeader(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
    }
}

/**
 * Individual contribution card for paging grid with dynamic span support
 */
@Composable
private fun ContributionPagingCard(
    contribution: ContributionModel,
    span: Int,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    // Adjust aspect ratio based on the span
    val aspectRatio = when (span) {
        3 -> 1.77f // Landscape for full-width items
        2 -> 2f    // 2:1 ratio for 2-column items
        else -> 1f // Square for standard 1-column items
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clickable { /* TODO: Open detail view */ },
        shape = RoundedCornerShape(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current

            // Use thumb URL if available, otherwise use image URL
            val imageUrl = contribution.thumbUrl ?: contribution.imageUrl

            LaunchedEffect(imageUrl) {
                Timber.d("ContributionsPagingGrid: Loading image URL: $imageUrl")
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .build(),
                contentDescription = contribution.filename,
                imageLoader = imageLoader,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

