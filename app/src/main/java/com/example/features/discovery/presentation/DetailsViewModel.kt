package com.example.features.discovery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.discovery.data.TMDBRepository
import com.example.features.discovery.domain.Movie
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
    data class Success(val movie: Movie, val isInWatchlist: Boolean = false) : DetailsUiState
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
                    _uiState.value = DetailsUiState.Success(movie, isInWatchlist)
                } else {
                    _uiState.value = DetailsUiState.Error("Failed to load title details")
                }
            } catch (e: Exception) {
                _uiState.value = DetailsUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun toggleWatchlist() {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            viewModelScope.launch {
                val movie = currentState.movie
                val userId = SupabaseManager.client?.auth?.currentUserOrNull()?.id ?: return@launch
                
                if (currentState.isInWatchlist) {
                    if (watchlistRepository.removeFromWatchlist(movie.id, movie.mediaType ?: "movie")) {
                        _uiState.value = currentState.copy(isInWatchlist = false)
                    }
                } else {
                    val item = WatchlistItem(
                        userId = userId,
                        tmdbId = movie.id,
                        mediaType = movie.mediaType ?: "movie",
                        title = movie.title ?: movie.name ?: "Unknown",
                        posterPath = movie.posterPath
                    )
                    if (watchlistRepository.addToWatchlist(item)) {
                        _uiState.value = currentState.copy(isInWatchlist = true)
                    }
                }
            }
        }
    }
}
