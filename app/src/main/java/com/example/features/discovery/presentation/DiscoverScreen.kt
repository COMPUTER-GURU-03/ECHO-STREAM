package com.example.features.discovery.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.core.ui.VerticalMovieCard
import com.example.core.ui.glassEffect
import com.example.features.discovery.domain.Movie
import com.example.ui.theme.ElectricIndigo
import kotlinx.coroutines.delay

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onMovieClick: (Movie) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    // Debounce search
    LaunchedEffect(searchQuery) {
        delay(500)
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
                .padding(24.dp)
                .glassEffect(16.dp),
            placeholder = { Text("Search movies, TV shows, actors...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricIndigo) },
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
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.2f)
                        )
                        Text(
                            "Find your next favorite title",
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
                is DiscoverUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DiscoverUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DiscoverUiState.Success -> {
                    if (state.results.isEmpty()) {
                        Text(
                            "No results found for \"$searchQuery\"",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(110.dp),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.results) { movie ->
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
