package com.example.features.authentication.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.SupabaseManager
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Error(val message: String) : AuthUiState
    data object Authenticated : AuthUiState
    data object NeedsVerification : AuthUiState
}

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val auth = SupabaseManager.client?.auth

    init {
        if (SupabaseManager.client == null) {
            _uiState.value = AuthUiState.Error("Supabase is not configured. Please set SUPABASE_URL and SUPABASE_ANON_KEY in the Secrets panel.")
        } else {
            checkSession()
        }
    }

    private fun checkSession() {
        viewModelScope.launch {
            auth?.let {
                val session = it.currentSessionOrNull()
                if (session != null) {
                    _uiState.value = AuthUiState.Authenticated
                }
            }
        }
    }

    fun login(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val currentAuth = auth ?: throw Exception("Auth not initialized")
                currentAuth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                _uiState.value = AuthUiState.Authenticated
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun signUp(emailInput: String, passwordInput: String, name: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val currentAuth = auth ?: throw Exception("Auth not initialized")
                currentAuth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                    data = buildJsonObject {
                        put("display_name", name)
                    }
                }
                _uiState.value = AuthUiState.NeedsVerification
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun resetPassword(emailInput: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val currentAuth = auth ?: throw Exception("Auth not initialized")
                currentAuth.resetPasswordForEmail(emailInput)
                _uiState.value = AuthUiState.Idle // Or a specific success state
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Reset request failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth?.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle
    }
}
