package fr.free.nrw.commons.feature.profile.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.imageLoader
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardCategory
import fr.free.nrw.commons.feature.profile.data.local.entity.LeaderboardDuration
import fr.free.nrw.commons.feature.profile.domain.model.Achievement
import fr.free.nrw.commons.feature.profile.domain.model.LeaderboardUser
import fr.free.nrw.commons.feature.profile.domain.model.UserProfile
import fr.free.nrw.commons.feature.profile.presentation.components.AchievementsGrid
import fr.free.nrw.commons.feature.profile.presentation.components.LeaderboardList
import fr.free.nrw.commons.feature.profile.presentation.components.ProfileHeader
import fr.free.nrw.commons.feature.contributions.ui.components.ContributionsGrid
import fr.free.nrw.commons.feature.contributions.ui.models.Contribution

/**
 * Main Profile screen with adaptive layout for phones and tablets.
 * Implements Material3 design with Wikipedia app aesthetics.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    // Determine if we're on a tablet or phone
    val isComponentActivity = context is ComponentActivity
    val windowSizeClass = if (isComponentActivity) {
        calculateWindowSizeClass(context)
    } else {
        null
    }
    val isExpandedScreen = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded
    val isCompact = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Compact

    Scaffold { paddingValues ->
        when {
            uiState.isLoading && uiState.userProfile == null -> {
                LoadingScreen(modifier = Modifier.padding(paddingValues))
            }
            uiState.error != null && uiState.userProfile == null -> {
                ErrorScreen(
                    error = uiState.error ?: "Unknown error",
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                if (isExpandedScreen) {
                    ExpandedProfileContent(
                        uiState = uiState,
                        imageLoader = imageLoader,
                        onTabSelected = viewModel::onTabSelected,
                        onCategorySelected = viewModel::onCategorySelected,
                        onDurationSelected = viewModel::onDurationSelected,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    CompactProfileContent(
                        uiState = uiState,
                        imageLoader = imageLoader,
                        onTabSelected = viewModel::onTabSelected,
                        onCategorySelected = viewModel::onCategorySelected,
                        onDurationSelected = viewModel::onDurationSelected,
                        onRefresh = viewModel::refresh,
                        isCompact = isCompact,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

/**
 * Compact layout for phones (single column).
 */
@Composable
private fun CompactProfileContent(
    uiState: ProfileUiState,
    imageLoader: ImageLoader,
    onTabSelected: (ProfileTab) -> Unit,
    onCategorySelected: (LeaderboardCategory) -> Unit,
    onDurationSelected: (LeaderboardDuration) -> Unit,
    onRefresh: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Profile header
        uiState.userProfile?.let { profile ->
            ProfileHeader(
                profile = profile,
                isCompact = isCompact
            )
        }

        ContributionsTab(
            contributions = uiState.contributions,
            imageLoader = imageLoader
        )
    }
}

/**
 * Expanded layout for tablets (two columns).
 */
@Composable
private fun ExpandedProfileContent(
    uiState: ProfileUiState,
    imageLoader: ImageLoader,
    onTabSelected: (ProfileTab) -> Unit,
    onCategorySelected: (LeaderboardCategory) -> Unit,
    onDurationSelected: (LeaderboardDuration) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Left column - Profile info and tabs
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            uiState.userProfile?.let { profile ->
                ProfileHeader(
                    profile = profile,
                    isCompact = false
                )
            }

            HorizontalDivider()

            NavigationRail(
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileTab.entries.forEach { tab ->
                    NavigationRailItem(
                        selected = uiState.selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = { /* Add icons later */ },
                        label = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }

        VerticalDivider()

        // Right column - Content
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        ) {
            when (uiState.selectedTab) {
                ProfileTab.ACHIEVEMENTS -> {
                    AchievementsTab(
                        achievements = uiState.achievements,
                        unlockedCount = uiState.unlockedAchievementsCount,
                        columns = 3
                    )
                }
                ProfileTab.LEADERBOARD -> {
                    LeaderboardTab(
                        leaderboard = uiState.leaderboard,
                        userRank = uiState.userRank,
                        selectedCategory = uiState.selectedCategory,
                        selectedDuration = uiState.selectedDuration,
                        onCategorySelected = onCategorySelected,
                        onDurationSelected = onDurationSelected,
                        isCompact = false
                    )
                }
                ProfileTab.CONTRIBUTIONS -> {
                    ContributionsTab(
                        contributions = uiState.contributions,
                        imageLoader = imageLoader
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementsSummary(
    unlockedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "$unlockedCount / $totalCount unlocked",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun AchievementsTab(
    achievements: List<Achievement>,
    unlockedCount: Int,
    columns: Int
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AchievementsSummary(
            unlockedCount = unlockedCount,
            totalCount = achievements.size
        )
        AchievementsGrid(
            achievements = achievements,
            columns = columns
        )
    }
}

@Composable
private fun LeaderboardTab(
    leaderboard: List<LeaderboardUser>,
    userRank: LeaderboardUser?,
    selectedCategory: LeaderboardCategory,
    selectedDuration: LeaderboardDuration,
    onCategorySelected: (LeaderboardCategory) -> Unit,
    onDurationSelected: (LeaderboardDuration) -> Unit,
    isCompact: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filters
        FilterChips(
            selectedCategory = selectedCategory,
            selectedDuration = selectedDuration,
            onCategorySelected = onCategorySelected,
            onDurationSelected = onDurationSelected,
            modifier = Modifier.padding(16.dp)
        )

        LeaderboardList(
            leaderboard = leaderboard,
            currentUserRank = userRank,
            isCompact = isCompact
        )
    }
}

@Composable
private fun FilterChips(
    selectedCategory: LeaderboardCategory,
    selectedDuration: LeaderboardDuration,
    onCategorySelected: (LeaderboardCategory) -> Unit,
    onDurationSelected: (LeaderboardDuration) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LeaderboardCategory.entries.take(3).forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        Text(
            text = "Duration",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LeaderboardDuration.entries.forEach { duration ->
                FilterChip(
                    selected = selectedDuration == duration,
                    onClick = { onDurationSelected(duration) },
                    label = { Text(duration.displayName) }
                )
            }
        }
    }
}

@Composable
private fun ContributionsTab(
    contributions: List<Contribution>,
    imageLoader: ImageLoader
) {
    if (contributions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading contributions...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Fetching from Wikimedia Commons",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        ContributionsGrid(
            contributions = contributions,
            imageLoader = imageLoader
        )
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

// Preview composables
@Preview(showBackground = true, name = "Compact Profile - Leaderboard")
@Composable
private fun PreviewCompactProfileLeaderboard() {
    val context = LocalContext.current
    MaterialTheme {
        CompactProfileContent(
            uiState = createMockProfileUiState(),
            imageLoader = ImageLoader.Builder(context).build(),
            onTabSelected = {},
            onCategorySelected = {},
            onDurationSelected = {},
            onRefresh = {},
            isCompact = true
        )
    }
}

@Preview(showBackground = true, name = "Compact Profile - Achievements")
@Composable
private fun PreviewCompactProfileAchievements() {
    val context = LocalContext.current
    MaterialTheme {
        CompactProfileContent(
            uiState = createMockProfileUiState().copy(selectedTab = ProfileTab.ACHIEVEMENTS),
            imageLoader = ImageLoader.Builder(context).build(),
            onTabSelected = {},
            onCategorySelected = {},
            onDurationSelected = {},
            onRefresh = {},
            isCompact = true
        )
    }
}

@Preview(showBackground = true, name = "Compact Profile - Contributions")
@Composable
private fun PreviewCompactProfileContributions() {
    val context = LocalContext.current
    MaterialTheme {
        CompactProfileContent(
            uiState = createMockProfileUiState().copy(selectedTab = ProfileTab.CONTRIBUTIONS),
            imageLoader = ImageLoader.Builder(context).build(),
            onTabSelected = {},
            onCategorySelected = {},
            onDurationSelected = {},
            onRefresh = {},
            isCompact = true
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun PreviewLoadingState() {
    MaterialTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun PreviewErrorState() {
    MaterialTheme {
        ErrorScreen(
            error = "Failed to load profile data",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Expanded Profile", widthDp = 1024, heightDp = 768)
@Composable
private fun PreviewExpandedProfile() {
    val context = LocalContext.current
    MaterialTheme {
        ExpandedProfileContent(
            uiState = createMockProfileUiState(),
            imageLoader = ImageLoader.Builder(context).build(),
            onTabSelected = {},
            onCategorySelected = {},
            onDurationSelected = {},
            onRefresh = {}
        )
    }
}

// Mock data for previews
private fun createMockProfileUiState() = ProfileUiState(
    isLoading = false,
    userProfile = UserProfile(
        username = "TestUser",
        avatarUrl = null,
        rank = 15,
        uploadCount = 1234,
        thanksReceived = 567,
        uniqueImagesUsed = 890,
        articlesUsingImages = 345,
        deletedUploads = 12,
        qualityScore = 0.85f
    ),
    selectedTab = ProfileTab.LEADERBOARD,
    achievements = listOf(
        Achievement(
            id = 1,
            type = fr.free.nrw.commons.feature.profile.data.local.entity.AchievementType.UPLOADS,
            title = "First Upload",
            description = "Upload your first image",
            iconUrl = null,
            currentValue = 10,
            targetValue = 10,
            progress = 1.0f,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis(),
            level = 1
        ),
        Achievement(
            id = 2,
            type = fr.free.nrw.commons.feature.profile.data.local.entity.AchievementType.UPLOADS,
            title = "10 Uploads",
            description = "Upload 10 images",
            iconUrl = null,
            currentValue = 10,
            targetValue = 10,
            progress = 1.0f,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis(),
            level = 1
        ),
        Achievement(
            id = 3,
            type = fr.free.nrw.commons.feature.profile.data.local.entity.AchievementType.UPLOADS,
            title = "100 Uploads",
            description = "Upload 100 images",
            iconUrl = null,
            currentValue = 45,
            targetValue = 100,
            progress = 0.45f,
            isUnlocked = false,
            unlockedAt = null,
            level = 1
        )
    ),
    unlockedAchievementsCount = 2,
    leaderboard = listOf(
        LeaderboardUser(
            username = "User1",
            avatarUrl = null,
            rank = 1,
            score = 5000
        ),
        LeaderboardUser(
            username = "User2",
            avatarUrl = null,
            rank = 2,
            score = 4500
        ),
        LeaderboardUser(
            username = "TestUser",
            avatarUrl = null,
            rank = 15,
            score = 1234,
            isCurrentUser = true
        )
    ),
    userRank = LeaderboardUser(
        username = "TestUser",
        avatarUrl = null,
        rank = 15,
        score = 1234,
        isCurrentUser = true
    ),
    selectedCategory = LeaderboardCategory.UPLOAD,
    selectedDuration = LeaderboardDuration.WEEKLY,
    contributions = createMockContributions()
)

/**
 * Creates mock contributions with varying aspect ratios for realistic staggered grid preview
 */
private fun createMockContributions(): List<Contribution> {
    val now = System.currentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L

    return listOf(
        // Today - varying sizes
        Contribution(
            id = "1",
            title = "Forest landscape",
            thumbnailUrl = "https://picsum.photos/800/600",
            uploadDate = now - (2 * 60 * 60 * 1000L), // 2 hours ago
            views = 1234,
            aspectRatio = 1.33f, // Landscape
            width = 800,
            height = 600
        ),
        Contribution(
            id = "2",
            title = "Portrait photo",
            thumbnailUrl = "https://picsum.photos/600/900",
            uploadDate = now - (3 * 60 * 60 * 1000L),
            views = 567,
            aspectRatio = 0.67f, // Portrait
            width = 600,
            height = 900
        ),
        Contribution(
            id = "3",
            title = "Square image",
            thumbnailUrl = "https://picsum.photos/800/800",
            uploadDate = now - (4 * 60 * 60 * 1000L),
            views = 890,
            aspectRatio = 1.0f, // Square
            width = 800,
            height = 800
        ),
        Contribution(
            id = "4",
            title = "Wide panorama",
            thumbnailUrl = "https://picsum.photos/1200/600",
            uploadDate = now - (5 * 60 * 60 * 1000L),
            views = 2100,
            aspectRatio = 2.0f, // Wide
            width = 1200,
            height = 600
        ),
        // Yesterday
        Contribution(
            id = "5",
            title = "City street",
            thumbnailUrl = "https://picsum.photos/700/1000",
            uploadDate = now - (oneDayMs + 3 * 60 * 60 * 1000L),
            views = 456,
            aspectRatio = 0.7f, // Portrait
            width = 700,
            height = 1000
        ),
        Contribution(
            id = "6",
            title = "Architecture",
            thumbnailUrl = "https://picsum.photos/900/700",
            uploadDate = now - (oneDayMs + 5 * 60 * 60 * 1000L),
            views = 789,
            aspectRatio = 1.29f, // Landscape
            width = 900,
            height = 700
        ),
        Contribution(
            id = "7",
            title = "Concert hall",
            thumbnailUrl = "https://picsum.photos/1000/600",
            uploadDate = now - (oneDayMs + 6 * 60 * 60 * 1000L),
            views = 1500,
            aspectRatio = 1.67f, // Wide landscape
            width = 1000,
            height = 600
        ),
        Contribution(
            id = "8",
            title = "Hot air balloon",
            thumbnailUrl = "https://picsum.photos/750/750",
            uploadDate = now - (oneDayMs + 8 * 60 * 60 * 1000L),
            views = 345,
            aspectRatio = 1.0f, // Square
            width = 750,
            height = 750
        ),
        // 3 days ago
        Contribution(
            id = "9",
            title = "Sunset",
            thumbnailUrl = "https://picsum.photos/800/500",
            uploadDate = now - (3 * oneDayMs),
            views = 2300,
            aspectRatio = 1.6f,
            width = 800,
            height = 500
        ),
        Contribution(
            id = "10",
            title = "Mountain landscape",
            thumbnailUrl = "https://picsum.photos/600/1100",
            uploadDate = now - (3 * oneDayMs + 2 * 60 * 60 * 1000L),
            views = 678,
            aspectRatio = 0.55f, // Tall portrait
            width = 600,
            height = 1100
        ),
        Contribution(
            id = "11",
            title = "Abstract pattern",
            thumbnailUrl = "https://picsum.photos/850/850",
            uploadDate = now - (3 * oneDayMs + 4 * 60 * 60 * 1000L),
            views = 234,
            aspectRatio = 1.0f,
            width = 850,
            height = 850
        ),
        Contribution(
            id = "12",
            title = "Beach sunset",
            thumbnailUrl = "https://picsum.photos/1100/700",
            uploadDate = now - (3 * oneDayMs + 6 * 60 * 60 * 1000L),
            views = 1890,
            aspectRatio = 1.57f,
            width = 1100,
            height = 700
        )
    )
}

