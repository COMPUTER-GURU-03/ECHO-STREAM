package com.example.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.discovery.data.TMDBRepository
import com.example.features.discovery.domain.Movie
import com.example.features.playback.data.WatchProgressRepository
import com.example.features.playback.domain.WatchProgress
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val trending: List<Movie>,
        val popularMovies: List<Movie>,
        val popularTV: List<Movie>,
        val topRated: List<Movie> = emptyList(),
        val continueWatching: List<WatchProgress> = emptyList(),
        val selectedCategory: String = "All"
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val repository = TMDBRepository()
    private val progressRepository = WatchProgressRepository()

    private var rawTrending: List<Movie> = emptyList()
    private var rawPopularMovies: List<Movie> = emptyList()
    private var rawPopularTV: List<Movie> = emptyList()
    private var rawTopRated: List<Movie> = emptyList()
    private var rawContinueWatching: List<WatchProgress> = emptyList()
    private var currentCategory: String = "All"

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Fetch in parallel for high responsiveness
                val trendingDeferred = async { repository.getTrendingMovies() }
                val popularMoviesDeferred = async { repository.getPopularMovies() }
                val popularTVDeferred = async { repository.getPopularTVShows() }
                val topRatedDeferred = async { repository.getTopRatedMovies() }
                val progressDeferred = async { progressRepository.getAllProgress() }

                rawTrending = trendingDeferred.await()
                rawPopularMovies = popularMoviesDeferred.await()
                rawPopularTV = popularTVDeferred.await()
                rawTopRated = topRatedDeferred.await()
                rawContinueWatching = progressDeferred.await()

                if (rawTrending.isEmpty() && rawPopularMovies.isEmpty() && rawPopularTV.isEmpty() && rawTopRated.isEmpty()) {
                    _uiState.value = HomeUiState.Error(
                        "Unable to fetch TMDB titles. Please ensure TMDB_API_KEY is configured in the Secrets panel, or your Supabase Edge Function is active."
                    )
                } else {
                    emitSuccessState()
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load cinematic content")
            }
        }
    }

    fun refreshProgress() {
        viewModelScope.launch {
            rawContinueWatching = progressRepository.getAllProgress()
            if (_uiState.value is HomeUiState.Success) {
                emitSuccessState()
            }
        }
    }

    fun selectCategory(category: String) {
        currentCategory = category
        emitSuccessState()
    }

    private fun emitSuccessState() {
        _uiState.value = HomeUiState.Success(
            trending = rawTrending,
            popularMovies = rawPopularMovies,
            popularTV = rawPopularTV,
            topRated = rawTopRated,
            continueWatching = rawContinueWatching,
            selectedCategory = currentCategory
        )
    }
}
