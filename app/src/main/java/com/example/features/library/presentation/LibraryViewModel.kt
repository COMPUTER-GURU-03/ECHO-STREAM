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
        val continueWatching: List<WatchlistItem> = emptyList() // TODO: Implement watch progress
    ) : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}

class LibraryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val repository = WatchlistRepository()

    init {
        loadLibrary()
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _uiState.value = LibraryUiState.Loading
            try {
                val watchlist = repository.getWatchlist()
                _uiState.value = LibraryUiState.Success(watchlist = watchlist)
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error(e.message ?: "Failed to load library")
            }
        }
    }
}
