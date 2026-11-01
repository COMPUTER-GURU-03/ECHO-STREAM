package com.example.features.discovery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.discovery.data.TMDBRepository
import com.example.features.discovery.domain.Movie
import com.example.features.discovery.domain.Season
import com.example.core.data.SupabaseManager
import com.example.features.library.data.WatchlistRepository
import com.example.features.library.domain.WatchlistItem
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(
        val movie: Movie,
        val isInWatchlist: Boolean = false,
        val selectedSeasonNumber: Int = 1,
        val currentSeason: Season? = null,
        val isSeasonLoading: Boolean = false
    ) : DetailsUiState
    data class Error(val message: String) : DetailsUiState
}

class DetailsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val tmdbRepository = TMDBRepository()
    private val watchlistRepository = WatchlistRepository()

    fun loadDetails(id: Int, mediaType: String) {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            try {
                val movie = tmdbRepository.getTitleDetails(id, mediaType)
                if (movie != null) {
                    val isInWatchlist = watchlistRepository.isInWatchlist(id, mediaType)
                    
                    // If it is a TV show, prefetch the first season
                    if (mediaType.equals("tv", ignoreCase = true)) {
                        val firstSeasonNumber = movie.seasons?.firstOrNull { it.seasonNumber > 0 }?.seasonNumber ?: 1
                        _uiState.value = DetailsUiState.Success(
                            movie = movie,
                            isInWatchlist = isInWatchlist,
                            selectedSeasonNumber = firstSeasonNumber,
                            isSeasonLoading = true
                        )
                        loadSeason(id, firstSeasonNumber)
                    } else {
                        _uiState.value = DetailsUiState.Success(
                            movie = movie,
                            isInWatchlist = isInWatchlist
                        )
                    }
                } else {
                    _uiState.value = DetailsUiState.Error("Unable to load title details.")
                }
            } catch (e: Exception) {
                _uiState.value = DetailsUiState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }

    fun selectSeason(seasonNumber: Int) {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            _uiState.value = currentState.copy(
                selectedSeasonNumber = seasonNumber,
                isSeasonLoading = true
            )
            loadSeason(currentState.movie.id, seasonNumber)
        }
    }

    private fun loadSeason(tvId: Int, seasonNumber: Int) {
        viewModelScope.launch {
            try {
                val season = tmdbRepository.getSeasonDetails(tvId, seasonNumber)
                val currentState = _uiState.value
                if (currentState is DetailsUiState.Success && currentState.movie.id == tvId) {
                    _uiState.value = currentState.copy(
                        currentSeason = season,
                        isSeasonLoading = false
                    )
                }
            } catch (e: Exception) {
                val currentState = _uiState.value
                if (currentState is DetailsUiState.Success) {
                    _uiState.value = currentState.copy(isSeasonLoading = false)
                }
            }
        }
    }

    fun toggleWatchlist() {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            val movie = currentState.movie
            val newWatchlistState = !currentState.isInWatchlist

            // Optimistic UI update
            _uiState.value = currentState.copy(isInWatchlist = newWatchlistState)

            viewModelScope.launch {
                val userId = SupabaseManager.client?.auth?.currentUserOrNull()?.id ?: "local_user"
                val mediaType = movie.mediaType ?: "movie"

                if (newWatchlistState) {
                    val item = WatchlistItem(
                        userId = userId,
                        tmdbId = movie.id,
                        mediaType = mediaType,
                        title = movie.title ?: movie.name ?: "Unknown",
                        posterPath = movie.posterPath
                    )
                    watchlistRepository.addToWatchlist(item)
                } else {
                    watchlistRepository.removeFromWatchlist(movie.id, mediaType)
                }
            }
        }
    }
}
