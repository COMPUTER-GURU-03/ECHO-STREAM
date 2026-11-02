package com.example.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.SupabaseManager
import com.example.features.profile.data.ProfileRepository
import com.example.features.profile.domain.Profile
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profile: Profile, val email: String) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val repository = ProfileRepository()
    private val auth = SupabaseManager.client?.auth

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val user = auth?.currentUserOrNull()
            if (user != null) {
                val profile = repository.getProfile(user.id)
                if (profile != null) {
                    _uiState.value = ProfileUiState.Success(profile, user.email ?: "")
                } else {
                    // Create default profile if not found
                    val metaName = user.userMetadata?.get("display_name")?.toString()?.removeSurrounding("\"")
                    val newProfile = Profile(
                        id = user.id,
                        userId = user.id,
                        email = user.email,
                        displayName = metaName,
                        subscriptionTier = "Standard Cinema"
                    )
                    repository.updateProfile(newProfile)
                    _uiState.value = ProfileUiState.Success(newProfile, user.email ?: "")
                }
            } else {
                val guestProfile = Profile(
                    id = "guest_user",
                    userId = "guest_user",
                    displayName = "Cinema Guest",
                    email = "guest@moviedb.internal",
                    subscriptionTier = "Preview Pass"
                )
                _uiState.value = ProfileUiState.Success(guestProfile, "guest@moviedb.internal")
            }
        }
    }

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ProfileUiState.Success) {
                val user = auth?.currentUserOrNull() ?: return@launch
                val updatedProfile = currentState.profile.copy(displayName = newName)
                if (repository.updateProfile(updatedProfile)) {
                    _uiState.value = currentState.copy(profile = updatedProfile)
                }
            }
        }
    }
}
