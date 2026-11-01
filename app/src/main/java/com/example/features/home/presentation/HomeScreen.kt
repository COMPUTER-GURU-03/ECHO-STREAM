package com.example.features.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.core.ui.HeroBanner
import com.example.core.ui.SectionHeader
import com.example.core.ui.VerticalMovieCard
import com.example.features.discovery.domain.Movie

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMovieClick: (Movie) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is HomeUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadHomeData() }) {
                        Text("Retry")
                    }
                }
            }
            is HomeUiState.Success -> {
                HomeContent(
                    trending = state.trending,
                    popularMovies = state.popularMovies,
                    popularTV = state.popularTV,
                    onMovieClick = onMovieClick
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
    onMovieClick: (Movie) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp) // Spacing for bottom nav
    ) {
        // Hero Section
        if (trending.isNotEmpty()) {
            item {
                HeroBanner(
                    movie = trending.first(),
                    onClick = { onMovieClick(trending.first()) }
                )
            }
        }

        item {
            MovieRail(
                title = "Trending Now",
                movies = trending,
                onMovieClick = onMovieClick
            )
        }

        item {
            MovieRail(
                title = "Popular Movies",
                movies = popularMovies,
                onMovieClick = onMovieClick
            )
        }

        item {
            MovieRail(
                title = "Popular TV Shows",
                movies = popularTV,
                onMovieClick = onMovieClick
            )
        }
    }
}

@Composable
fun MovieRail(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        SectionHeader(title = title, onSeeAllClick = { /* TODO */ })
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(movies) { movie ->
                VerticalMovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) }
                )
            }
        }
    }
}
