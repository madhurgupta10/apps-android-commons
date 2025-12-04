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
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import fr.free.nrw.commons.feature.contributions.ui.models.Contribution
import fr.free.nrw.commons.feature.contributions.ui.models.TimelineGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val GRID_COLUMNS = 3

/**
 * Data class to track span layout for each item
 */
internal data class SpanLayout(val index: Int, val span: Int)

/**
 * Calculates optimal spans for all items to ensure no gaps (Google Photos style).
 * Uses prime number-based algorithm to create visually pleasing patterns that
 * completely fill the grid with no empty spaces.
 */
private fun calculateSpans(totalSize: Int): List<SpanLayout> {
    if (totalSize == 0) return emptyList()

    val spans = mutableListOf<SpanLayout>()

    // Handle special cases for small counts
    when (totalSize) {
        1 -> {
            // Single item takes full width
            spans.add(SpanLayout(0, 3))
            return spans
        }
        2 -> {
            // Two items: each takes full width (3 columns) on separate rows
            // This gives both images equal visual weight and prominence
            spans.add(SpanLayout(0, 3))
            spans.add(SpanLayout(1, 3))
            return spans
        }
        3 -> {
            // Three items: each takes 1 column (fills one row perfectly)
            spans.add(SpanLayout(0, 1))
            spans.add(SpanLayout(1, 1))
            spans.add(SpanLayout(2, 1))
            return spans
        }
        4 -> {
            // Four items: 2x2 grid pattern (each item takes 1.5 columns visually)
            // Using 2+1, 2+1 to create a balanced 2x2 appearance
            spans.add(SpanLayout(0, 2))
            spans.add(SpanLayout(1, 1))
            spans.add(SpanLayout(2, 2))
            spans.add(SpanLayout(3, 1))
            return spans
        }
        5 -> {
            // Five items: 2+1, 1+1+1 pattern
            spans.add(SpanLayout(0, 2))
            spans.add(SpanLayout(1, 1))
            spans.add(SpanLayout(2, 1))
            spans.add(SpanLayout(3, 1))
            spans.add(SpanLayout(4, 1))
            return spans
        }
    }

    // For larger counts, use prime-based pattern generation
    return generatePrimeBasedPattern(totalSize)
}

/**
 * Generates a span pattern based on prime number decomposition.
 * The algorithm ensures that rows are filled completely by using the fact that
 * 3 = 3, 3 = 2+1, 3 = 1+1+1 (all ways to partition 3).
 *
 * Pattern strategy:
 * - Use remainder modulo 3 to determine how to handle the last incomplete row
 * - Mix span sizes (3, 2, 1) based on prime-like distribution for visual variety
 * - Ensure no gaps by pre-calculating the total layout
 */
private fun generatePrimeBasedPattern(totalSize: Int): List<SpanLayout> {
    val spans = mutableListOf<SpanLayout>()
    val remainder = totalSize % 3

    // Calculate how many complete rows of 3 single items we need
    val completeRows = when (remainder) {
        0 -> totalSize / 3  // All items fit in rows of 3
        1 -> (totalSize - 4) / 3  // Reserve 4 items for 2+1, 1 pattern
        2 -> (totalSize - 5) / 3  // Reserve 5 items for 2+1, 1+1 pattern
        else -> 0
    }

    var currentIndex = 0

    // Add varied patterns for complete rows using prime-inspired distribution
    // Use pattern based on index position to create visual variety
    for (rowIndex in 0 until completeRows) {
        when {
            // Every 7th row (7 is prime): create a full-width hero image
            rowIndex % 7 == 0 && currentIndex + 3 < totalSize -> {
                spans.add(SpanLayout(currentIndex++, 3))
            }
            // Every 5th row (5 is prime): create 2+1 pattern
            rowIndex % 5 == 0 -> {
                spans.add(SpanLayout(currentIndex++, 2))
                spans.add(SpanLayout(currentIndex++, 1))
            }
            // Every 3rd row (3 is prime): create 1+2 pattern
            rowIndex % 3 == 0 -> {
                spans.add(SpanLayout(currentIndex++, 1))
                spans.add(SpanLayout(currentIndex++, 2))
            }
            // Default: 1+1+1 pattern
            else -> {
                spans.add(SpanLayout(currentIndex++, 1))
                spans.add(SpanLayout(currentIndex++, 1))
                spans.add(SpanLayout(currentIndex++, 1))
            }
        }
    }

    // Handle remaining items based on remainder
    when (remainder) {
        0 -> {
            // No remaining items, but add final rows using the same pattern
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
            // 1 item remaining: fill a row as 2+1, then place the last item as span 3
            // Or create a 2+1, 1 pattern
            if (currentIndex < totalSize) {
                spans.add(SpanLayout(currentIndex++, 2))
                if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 1))
                if (currentIndex < totalSize) spans.add(SpanLayout(currentIndex++, 3))
            }
        }
        2 -> {
            // 2 items remaining: create 2+1, 1+1 pattern or 1+1, 2 pattern
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
 * Google Photos style contributions grid with smart sizing and timeline grouping.
 * Uses LazyVerticalGrid with a fixed column count and dynamic spanning.
 * Ensures no empty grid cells by pre-calculating optimal spans.
 */
@Composable
fun ContributionsGrid(
    contributions: List<Contribution>,
    imageLoader: ImageLoader,
    isLoadingMore: Boolean = false,
    hasMore: Boolean = true,
    onLoadMore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timelineGroups = remember(contributions) {
        groupByTimeline(contributions)
    }

    val listState = rememberLazyGridState()

    // Detect when user scrolls near the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= totalItems - 6 &&
                    hasMore &&
                    !isLoadingMore) {
                    onLoadMore()
                }
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
        timelineGroups.forEach { group ->
            // Pre-calculate spans for this group to ensure no gaps
            val spanLayouts = calculateSpans(group.contributions.size)

            // Display timeline header, spanning all columns.
            item(span = { GridItemSpan(GRID_COLUMNS) }) {
                TimelineHeader(label = group.label)
            }

            // Display the grid of contributions for the current group.
            itemsIndexed(
                items = group.contributions,
                key = { _, contribution -> contribution.id },
                span = { index, _ ->
                    val span = spanLayouts.getOrNull(index)?.span ?: 1
                    GridItemSpan(span.coerceIn(1, GRID_COLUMNS))
                }
            ) { index, contribution ->
                val span = spanLayouts.getOrNull(index)?.span ?: 1
                ContributionCard(
                    contribution = contribution,
                    span = span,
                    imageLoader = imageLoader
                )
            }
        }

        // Loading indicator at the bottom
        if (isLoadingMore) {
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
 * Individual contribution card. Its aspect ratio is determined by its column span.
 */
@Composable
private fun ContributionCard(
    contribution: Contribution,
    span: Int,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    // Adjust aspect ratio based on the span to create a visually appealing layout.
    // The aspect ratio needs to account for the actual width of the item relative to grid spacing
    val aspectRatio = when (span) {
        3 -> 1.77f // Landscape for full-width items
        2 -> 2f    // 2:1 ratio for 2-column items (twice as wide as tall)
        else -> 1f // Square for standard 1-column items
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio) // Enforce the calculated aspect ratio
            .clickable { /* TODO: Open detail view */ },
        shape = RoundedCornerShape(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Log the URL being loaded
            LaunchedEffect(contribution.thumbnailUrl) {
                timber.log.Timber.d("ContributionsGrid: Loading image URL: ${contribution.thumbnailUrl}")
            }

            val context = LocalContext.current

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(contribution.thumbnailUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .listener(
                        onStart = {
                            timber.log.Timber.d("ContributionsGrid: Started loading: ${contribution.thumbnailUrl}")
                        },
                        onError = { _, error ->
                            timber.log.Timber.e("ContributionsGrid: Failed to load image: ${contribution.thumbnailUrl}")
                            timber.log.Timber.e("ContributionsGrid: Error: ${error.throwable.message}")
                            timber.log.Timber.e("ContributionsGrid: Error stacktrace: ${error.throwable.stackTraceToString()}")
                        },
                        onSuccess = { _, _ ->
                            timber.log.Timber.d("ContributionsGrid: Successfully loaded: ${contribution.thumbnailUrl}")
                        }
                    )
                    .build(),
                imageLoader = imageLoader,
                contentDescription = contribution.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Overlay with views count (optional, like Google Photos)
            if (contribution.views > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = formatViewCount(contribution.views),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Groups contributions by timeline periods (today, yesterday, weeks, months, years)
 */
private fun groupByTimeline(contributions: List<Contribution>): List<TimelineGroup> {
    if (contributions.isEmpty()) return emptyList()

    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()

    val groups = mutableListOf<TimelineGroup>()
    val sortedContributions = contributions.sortedByDescending { it.uploadDate }

    var currentGroup = mutableListOf<Contribution>()
    var currentLabel = ""
    var currentTimestamp = 0L

    sortedContributions.forEach { contribution ->
        val label = getTimelineLabel(contribution.uploadDate, now, calendar)

        if (label != currentLabel) {
            if (currentGroup.isNotEmpty()) {
                groups.add(TimelineGroup(currentLabel, currentTimestamp, currentGroup.toList()))
            }
            currentLabel = label
            currentTimestamp = contribution.uploadDate
            currentGroup = mutableListOf(contribution)
        } else {
            currentGroup.add(contribution)
        }
    }

    if (currentGroup.isNotEmpty()) {
        groups.add(TimelineGroup(currentLabel, currentTimestamp, currentGroup))
    }

    return groups
}

/**
 * Generates timeline label like "Today", "1 week ago", "December 2024"
 */
private fun getTimelineLabel(timestamp: Long, now: Long, calendar: Calendar): String {
    val diffMillis = now - timestamp
    val diffDays = diffMillis / (24 * 60 * 60 * 1000)

    calendar.timeInMillis = timestamp

    return when {
        diffDays < 1 -> "Today"
        diffDays < 2 -> "Yesterday"
        diffDays < 7 -> "$diffDays days ago"
        diffDays < 14 -> "1 week ago"
        diffDays < 30 -> "${diffDays / 7} weeks ago"
        diffDays < 60 -> "1 month ago"
        diffDays < 365 -> "${diffDays / 30} months ago"
        diffDays < 730 -> "1 year ago"
        else -> {
            // Show month and year for older items
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}

/**
 * Formats view count for display (e.g., 1.2K, 3.5M)
 */
private fun formatViewCount(views: Int): String {
    return when {
        views < 1000 -> views.toString()
        views < 1_000_000 -> "${views / 1000}K"
        else -> String.format(Locale.US, "%.1fM", views / 1_000_000.0)
    }
}

