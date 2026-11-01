package com.example.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.discovery.data.TMDBRepository
import com.example.features.discovery.domain.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val trending: List<Movie>,
        val popularMovies: List<Movie>,
        val popularTV: List<Movie>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val repository = TMDBRepository()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Fetch in parallel for better performance
                val trending = repository.getTrendingMovies()
                val popularMovies = repository.getPopularMovies()
                val popularTV = repository.getPopularTVShows()

                if (trending.isEmpty() && popularMovies.isEmpty() && popularTV.isEmpty()) {
                    _uiState.value = HomeUiState.Error("No data found. Ensure your Edge Function and TMDB token are configured.")
                } else {
                    _uiState.value = HomeUiState.Success(trending, popularMovies, popularTV)
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load movie data")
            }
        }
    }
}
