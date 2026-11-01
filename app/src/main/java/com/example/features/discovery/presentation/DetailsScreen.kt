package com.example.features.discovery.presentation

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.ui.GlassSurface
import com.example.features.discovery.domain.CastMember
import com.example.features.discovery.domain.Episode
import com.example.features.discovery.domain.Movie
import com.example.features.discovery.domain.Season
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.SecondaryText
import java.util.Locale

@Composable
fun DetailsScreen(
    tmdbId: Int,
    mediaType: String,
    viewModel: DetailsViewModel,
    onBackClick: () -> Unit,
    onPlayClick: (Movie) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(tmdbId, mediaType) {
        viewModel.loadDetails(tmdbId, mediaType)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val state = uiState) {
            is DetailsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ElectricIndigo
                )
            }
            is DetailsUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadDetails(tmdbId, mediaType) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                    ) {
                        Text("Retry")
                    }
                }
            }
            is DetailsUiState.Success -> {
                DetailsContent(
                    movie = state.movie,
                    isInWatchlist = state.isInWatchlist,
                    selectedSeasonNumber = state.selectedSeasonNumber,
                    currentSeason = state.currentSeason,
                    isSeasonLoading = state.isSeasonLoading,
                    onSeasonSelect = { viewModel.selectSeason(it) },
                    onBackClick = onBackClick,
                    onPlayClick = { onPlayClick(state.movie) },
                    onPlayEpisode = { episode -> onPlayClick(state.movie) },
                    onWatchlistToggle = { viewModel.toggleWatchlist() }
                )
            }
        }

        // Floating Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}

@Composable
fun DetailsContent(
    movie: Movie,
    isInWatchlist: Boolean,
    selectedSeasonNumber: Int,
    currentSeason: Season?,
    isSeasonLoading: Boolean,
    onSeasonSelect: (Int) -> Unit,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPlayEpisode: (Episode) -> Unit,
    onWatchlistToggle: () -> Unit
) {
    val isTv = movie.mediaType.equals("tv", ignoreCase = true)
    val seasons = movie.seasons?.filter { it.seasonNumber > 0 } ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        // Backdrop Header
        item {
            Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                val bannerUrl = if (!movie.backdropPath.isNullOrBlank()) movie.backdropUrl else movie.posterUrl
                AsyncImage(
                    model = bannerUrl,
                    contentDescription = movie.displayTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )
            }
        }

        // Title and Metadata Block
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-50).dp)
            ) {
                Text(
                    text = movie.displayTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (!movie.tagline.isNullOrBlank()) {
                    Text(
                        text = "\"${movie.tagline}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = SecondaryText,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Meta row: Rating, Year, Runtime/Seasons, Genres
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val rating = movie.voteAverage
                    if (rating != null && rating > 0.0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E2230))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", rating),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (movie.releaseYear.isNotBlank()) {
                        Text(
                            text = movie.releaseYear,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText
                        )
                    }

                    if (isTv) {
                        val count = movie.numberOfSeasons ?: seasons.size
                        if (count > 0) {
                            Text(text = "•", color = SecondaryText)
                            Text(
                                text = "$count ${if (count == 1) "Season" else "Seasons"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryText
                            )
                        }
                    } else if (movie.formattedRuntime.isNotBlank()) {
                        Text(text = "•", color = SecondaryText)
                        Text(
                            text = movie.formattedRuntime,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText
                        )
                    }
                }

                // Genre Pills
                if (!movie.genres.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        movie.genres.take(3).forEach { genre ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricIndigo.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = genre.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricIndigo,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary CTA Row: Play / Watch Now and Add to Watchlist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPlayClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTv) "Stream Series" else "Watch Movie",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    GlassSurface(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        IconButton(
                            onClick = onWatchlistToggle,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isInWatchlist) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = if (isInWatchlist) "Remove from Watchlist" else "Add to Watchlist",
                                tint = if (isInWatchlist) ElectricIndigo else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Storyline Overview
                Text(
                    text = "Storyline",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = movie.overview?.ifBlank { null } ?: "No synopsis available for this title.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                    modifier = Modifier.padding(top = 10.dp),
                    lineHeight = 22.sp
                )
            }
        }

        // TV Show Season & Episodes Section
        if (isTv && seasons.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)) {
                    Text(
                        text = "Episodes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    // Season Selector Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        seasons.forEach { season ->
                            val isSelected = season.seasonNumber == selectedSeasonNumber
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) ElectricIndigo else Color(0xFF161924)
                                    )
                                    .clickable { onSeasonSelect(season.seasonNumber) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = season.displayName,
                                    color = if (isSelected) Color.White else SecondaryText,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isSeasonLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ElectricIndigo, modifier = Modifier.size(28.dp))
                        }
                    } else {
                        val episodes = currentSeason?.episodes ?: emptyList()
                        if (episodes.isEmpty()) {
                            Text(
                                text = "Episodes information unavailable for this season.",
                                color = SecondaryText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                episodes.forEach { episode ->
                                    EpisodeItemCard(
                                        episode = episode,
                                        onPlay = { onPlayEpisode(episode) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cast rail
        movie.credits?.cast?.let { cast ->
            if (cast.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Text(
                            text = "Top Cast",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            color = Color.White
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(cast.take(15)) { member ->
                                CastCard(member)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EpisodeItemCard(
    episode: Episode,
    onPlay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12141D)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E2230))
            ) {
                AsyncImage(
                    model = episode.stillUrl,
                    contentDescription = episode.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "E${episode.episodeNumber} • ${episode.name}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!episode.overview.isNullOrBlank()) {
                    Text(
                        text = episode.overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (episode.formattedDuration.isNotBlank()) {
                    Text(
                        text = episode.formattedDuration,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricIndigo,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CastCard(member: CastMember) {
    Column(modifier = Modifier.width(84.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E2230))
        ) {
            AsyncImage(
                model = member.profileUrl,
                contentDescription = member.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Text(
            text = member.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (!member.character.isNullOrBlank()) {
            Text(
                text = member.character,
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
