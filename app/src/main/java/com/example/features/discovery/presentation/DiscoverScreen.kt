package com.example.features.discovery.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.VerticalMovieCard
import com.example.core.ui.glassEffect
import com.example.features.discovery.domain.Movie
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.SecondaryText
import kotlinx.coroutines.delay

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onMovieClick: (Movie) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    val suggestedGenres = listOf("Action", "Sci-Fi", "Thriller", "Adventure", "Comedy", "Drama", "Animation", "Horror")

    // Debounce search
    LaunchedEffect(searchQuery) {
        delay(400)
        if (searchQuery.isNotBlank()) {
            viewModel.search(searchQuery)
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .glassEffect(16.dp),
            placeholder = { Text("Search movies, TV series, actors...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = ElectricIndigo) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        viewModel.search("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f))
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = ElectricIndigo,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is DiscoverUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(48.dp))
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = ElectricIndigo.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Discover Cinema & Series",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = "Search titles or tap popular categories below",
                            color = SecondaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
                        )

                        // Quick tags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    suggestedGenres.take(4).forEach { genre ->
                                        SuggestionChip(
                                            label = genre,
                                            onClick = { searchQuery = genre }
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    suggestedGenres.drop(4).forEach { genre ->
                                        SuggestionChip(
                                            label = genre,
                                            onClick = { searchQuery = genre }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is DiscoverUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ElectricIndigo
                    )
                }
                is DiscoverUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }
                is DiscoverUiState.Success -> {
                    if (state.results.isEmpty()) {
                        Text(
                            text = "No results found for \"$searchQuery\"",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(110.dp),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 110.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.results, key = { "${it.id}-${it.mediaType}" }) { movie ->
                                VerticalMovieCard(
                                    movie = movie,
                                    onClick = { onMovieClick(movie) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF161924))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
