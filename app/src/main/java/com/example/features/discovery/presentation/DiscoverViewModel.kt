package com.example.features.discovery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.discovery.data.TMDBRepository
import com.example.features.discovery.domain.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DiscoverUiState {
    data object Idle : DiscoverUiState
    data object Loading : DiscoverUiState
    data class Success(val results: List<Movie>) : DiscoverUiState
    data class Error(val message: String) : DiscoverUiState
}

class DiscoverViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Idle)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private val repository = TMDBRepository()

    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.value = DiscoverUiState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = DiscoverUiState.Loading
            try {
                val results = repository.search(query)
                _uiState.value = DiscoverUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = DiscoverUiState.Error(e.message ?: "Search failed")
            }
        }
    }
}
