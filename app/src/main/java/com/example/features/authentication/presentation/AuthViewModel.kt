package com.example.features.authentication.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.SupabaseConfig
import com.example.features.authentication.data.AuthRepository
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Error(val message: String) : AuthUiState
    data object Authenticated : AuthUiState
    data object NeedsVerification : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var sessionJob: Job? = null

    init {
        initializeAuth()
    }

    fun continueAsGuest() {
        _uiState.value = AuthUiState.Authenticated
    }

    fun initializeAuth() {
        SupabaseConfig.initialize()
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            try {
                if (repository.isUserLoggedIn) {
                    _uiState.value = AuthUiState.Authenticated
                }

                repository.sessionStatus.collect { status ->
                    when (status) {
                        is SessionStatus.Authenticated -> {
                            _uiState.value = AuthUiState.Authenticated
                        }
                        is SessionStatus.NotAuthenticated -> {
                            if (_uiState.value is AuthUiState.Authenticated) {
                                _uiState.value = AuthUiState.Idle
                            }
                        }
                        is SessionStatus.LoadingFromStorage -> {
                            // Waiting for session to be restored from local storage
                        }
                        is SessionStatus.NetworkError -> {
                            if (repository.isUserLoggedIn) {
                                _uiState.value = AuthUiState.Authenticated
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Silently retain current state if session collection is interrupted
            }
        }
    }

    /**
     * Signs in a user using Supabase Auth Email/Password.
     */
    fun signIn(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.signIn(emailInput, passwordInput)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Authenticated
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Sign-in failed.")
                }
            )
        }
    }

    // Alias for backward compatibility with UI components
    fun login(emailInput: String, passwordInput: String) = signIn(emailInput, passwordInput)

    /**
     * Signs up a new user using Supabase Auth.
     */
    fun signUp(emailInput: String, passwordInput: String, name: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.signUp(emailInput, passwordInput, name)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Authenticated
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Sign-up failed.")
                }
            )
        }
    }

    /**
     * Requests a password reset email via Supabase Auth.
     */
    fun resetPassword(emailInput: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.resetPassword(emailInput)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Idle
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Reset request failed.")
                }
            )
        }
    }

    /**
     * Signs out from Supabase Auth session.
     */
    fun logout() {
        viewModelScope.launch {
            repository.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle
    }
}
