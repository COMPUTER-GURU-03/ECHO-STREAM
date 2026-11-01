package com.example.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.ui.HeroBanner
import com.example.core.ui.SectionHeader
import com.example.core.ui.VerticalMovieCard
import com.example.features.discovery.domain.Movie
import com.example.features.playback.domain.WatchProgress
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.SecondaryText

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMovieClick: (Movie) -> Unit,
    onPlayProgressClick: ((WatchProgress) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresh Continue Watching progress when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.refreshProgress()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = ElectricIndigo,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading cinema feed...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF12141D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(ElectricIndigo.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Setup TMDB Access",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                color = SecondaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.loadHomeData() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry Connection")
                            }
                        }
                    }
                }
            }
            is HomeUiState.Success -> {
                HomeContent(
                    trending = state.trending,
                    popularMovies = state.popularMovies,
                    popularTV = state.popularTV,
                    topRated = state.topRated,
                    continueWatching = state.continueWatching,
                    selectedCategory = state.selectedCategory,
                    onSelectCategory = { viewModel.selectCategory(it) },
                    onMovieClick = onMovieClick,
                    onPlayProgressClick = onPlayProgressClick
                )
            }
        }
    }
}

@Composable
fun HomeContent(
    trending: List<Movie>,
    popularMovies: List<Movie>,
    popularTV: List<Movie>,
    topRated: List<Movie>,
    continueWatching: List<WatchProgress>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onPlayProgressClick: ((WatchProgress) -> Unit)? = null
) {
    val categories = listOf("All", "Movies", "TV Shows", "Top Rated")

    val heroMovie = when (selectedCategory) {
        "Movies" -> popularMovies.firstOrNull() ?: trending.firstOrNull()
        "TV Shows" -> popularTV.firstOrNull() ?: trending.firstOrNull()
        "Top Rated" -> topRated.firstOrNull() ?: trending.firstOrNull()
        else -> trending.firstOrNull() ?: popularMovies.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Hero Section
        if (heroMovie != null) {
            item {
                Box {
                    HeroBanner(
                        movie = heroMovie,
                        onDetailsClick = { onMovieClick(heroMovie) },
                        onPlayClick = { onMovieClick(heroMovie) }
                    )

                    // Floating Category Selector over the top of the screen
                    Row(
                        modifier = Modifier
                            .statusBarsPadding()
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = category == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) ElectricIndigo else Color.Black.copy(alpha = 0.55f)
                                    )
                                    .clickable { onSelectCategory(category) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Continue Watching Rail (Shown when user has active in-progress items)
        if (continueWatching.isNotEmpty() && (selectedCategory == "All")) {
            item {
                ContinueWatchingRail(
                    items = continueWatching,
                    onProgressClick = { progress ->
                        if (onPlayProgressClick != null) {
                            onPlayProgressClick(progress)
                        } else {
                            onMovieClick(
                                Movie(
                                    id = progress.tmdbId,
                                    title = progress.title ?: "Continue Watching",
                                    mediaType = progress.mediaType,
                                    posterPath = progress.posterPath
                                )
                            )
                        }
                    }
                )
            }
        }

        // Section Rails depending on category filter
        if (selectedCategory == "All" || selectedCategory == "Movies") {
            if (trending.isNotEmpty()) {
                item {
                    MovieRail(
                        title = "Trending Now",
                        movies = trending,
                        onMovieClick = onMovieClick
                    )
                }
            }
        }

        if (selectedCategory == "All" || selectedCategory == "Movies") {
            if (popularMovies.isNotEmpty()) {
                item {
                    MovieRail(
                        title = "Popular Movies",
                        movies = popularMovies,
                        onMovieClick = onMovieClick
                    )
                }
            }
        }

        if (selectedCategory == "All" || selectedCategory == "TV Shows") {
            if (popularTV.isNotEmpty()) {
                item {
                    MovieRail(
                        title = "Popular TV Series",
                        movies = popularTV,
                        onMovieClick = onMovieClick
                    )
                }
            }
        }

        if (selectedCategory == "All" || selectedCategory == "Top Rated") {
            if (topRated.isNotEmpty()) {
                item {
                    MovieRail(
                        title = "Top Rated Masterpieces",
                        movies = topRated,
                        onMovieClick = onMovieClick
                    )
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingRail(
    items: List<WatchProgress>,
    onProgressClick: (WatchProgress) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        SectionHeader(title = "Continue Watching", onSeeAllClick = null)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items, key = { "${it.tmdbId}-${it.mediaType}" }) { progress ->
                ContinueWatchingCard(
                    progress = progress,
                    onClick = { onProgressClick(progress) }
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    progress: WatchProgress,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12141D)),
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp)
                    .background(Color(0xFF1E2230))
            ) {
                val imageUrl = if (!progress.posterPath.isNullOrBlank()) {
                    if (progress.posterPath.startsWith("http")) progress.posterPath else "https://image.tmdb.org/t/p/w500${progress.posterPath}"
                } else null

                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = progress.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Dark vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )

                // Play Icon Button
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(ElectricIndigo.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Progress Bar pinned to bottom edge
                LinearProgressIndicator(
                    progress = { progress.progressPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter),
                    color = ElectricIndigo,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = progress.title ?: "Untitled",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = progress.formattedRemainingTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricIndigo,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun MovieRail(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(title = title, onSeeAllClick = null)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(movies, key = { "${it.id}-${it.mediaType}" }) { movie ->
                VerticalMovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) }
                )
            }
        }
    }
}
