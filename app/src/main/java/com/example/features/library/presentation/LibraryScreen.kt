package com.example.features.library.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.features.library.domain.WatchlistItem
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onTitleClick: (Int, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLibrary()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Watchlist",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadLibrary() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is LibraryUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ElectricIndigo
                    )
                }
                is LibraryUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadLibrary() },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is LibraryUiState.Success -> {
                    if (state.watchlist.isEmpty()) {
                        EmptyLibrary(
                            modifier = Modifier.align(Alignment.Center),
                            onRefresh = { viewModel.loadLibrary() }
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Filter Tabs: All, Movies, TV Shows
                            val filters = listOf("All", "Movies", "TV Shows")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filters.forEach { filter ->
                                    val count = when (filter) {
                                        "Movies" -> state.watchlist.count { it.mediaType.equals("movie", ignoreCase = true) }
                                        "TV Shows" -> state.watchlist.count { it.mediaType.equals("tv", ignoreCase = true) }
                                        else -> state.watchlist.size
                                    }
                                    val isSelected = filter == state.selectedFilter

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                if (isSelected) ElectricIndigo else Color(0xFF161924)
                                            )
                                            .clickable { viewModel.setFilter(filter) }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$filter ($count)",
                                            color = if (isSelected) Color.White else SecondaryText,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            WatchlistGrid(
                                items = state.filteredWatchlist,
                                onItemClick = onTitleClick,
                                onRemoveItem = { id, type -> viewModel.removeFromWatchlist(id, type) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistGrid(
    items: List<WatchlistItem>,
    onItemClick: (Int, String) -> Unit,
    onRemoveItem: (Int, String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(110.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 110.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(items, key = { "${it.tmdbId}-${it.mediaType}" }) { item ->
            WatchlistItemCard(
                item = item,
                onClick = { onItemClick(item.tmdbId, item.mediaType) },
                onRemove = { onRemoveItem(item.tmdbId, item.mediaType) }
            )
        }
    }
}

@Composable
fun WatchlistItemCard(
    item: WatchlistItem,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF141721))
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${item.posterPath}",
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Remove button overlay on top-end
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Text(
            text = item.title,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )

        val mediaLabel = if (item.mediaType.equals("tv", ignoreCase = true)) "Series" else "Movie"
        Text(
            text = mediaLabel,
            style = MaterialTheme.typography.labelSmall,
            color = ElectricIndigo,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun EmptyLibrary(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(ElectricIndigo.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = ElectricIndigo,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Your Watchlist is Empty",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Browse trending movies and TV series, and tap the + icon to save them to your personal watchlist.",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Button(
            onClick = onRefresh,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh Library")
        }
    }
}
