package com.example.features.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.library.data.WatchlistRepository
import com.example.features.library.domain.WatchlistItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data class Success(
        val watchlist: List<WatchlistItem>,
        val filteredWatchlist: List<WatchlistItem>,
        val selectedFilter: String = "All"
    ) : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}

class LibraryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val repository = WatchlistRepository()
    private var allItems: List<WatchlistItem> = emptyList()
    private var activeFilter: String = "All"

    init {
        loadLibrary()
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _uiState.value = LibraryUiState.Loading
            try {
                allItems = repository.getWatchlist()
                emitSuccess()
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error(e.message ?: "Failed to load library")
            }
        }
    }

    fun setFilter(filter: String) {
        activeFilter = filter
        emitSuccess()
    }

    fun removeFromWatchlist(tmdbId: Int, mediaType: String) {
        allItems = allItems.filterNot { it.tmdbId == tmdbId && it.mediaType == mediaType }
        emitSuccess()

        viewModelScope.launch {
            repository.removeFromWatchlist(tmdbId, mediaType)
        }
    }

    private fun emitSuccess() {
        val filtered = when (activeFilter) {
            "Movies" -> allItems.filter { it.mediaType.equals("movie", ignoreCase = true) }
            "TV Shows" -> allItems.filter { it.mediaType.equals("tv", ignoreCase = true) }
            else -> allItems
        }
        _uiState.value = LibraryUiState.Success(
            watchlist = allItems,
            filteredWatchlist = filtered,
            selectedFilter = activeFilter
        )
    }
}
