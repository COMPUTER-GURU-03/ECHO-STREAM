package com.example.features.authentication.data

import com.example.core.data.SupabaseConfig
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Authentication Repository implementing user authentication via Supabase Auth.
 * Handles sign in, sign up, session management, and password recovery.
 */
class AuthRepository {
    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    }

    private val auth get() = SupabaseConfig.auth

    val sessionStatus: Flow<SessionStatus>
        get() = auth?.sessionStatus ?: emptyFlow()

    val currentUser: UserInfo?
        get() = auth?.currentUserOrNull()

    val currentSession: UserSession?
        get() = auth?.currentSessionOrNull()

    val isUserLoggedIn: Boolean
        get() = currentSession != null

    /**
     * Signs in a user using Supabase Auth Email provider.
     */
    suspend fun signIn(emailInput: String, passwordInput: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedEmail = emailInput.trim()
        val trimmedPassword = passwordInput.trim()

        if (trimmedEmail.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your email address."))
        }
        if (trimmedPassword.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your password."))
        }
        if (!EMAIL_REGEX.matches(trimmedEmail)) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address format."))
        }

        val clientAuth = auth
            ?: return@withContext Result.failure(
                IllegalStateException("Supabase is not configured. Please set SUPABASE_URL and SUPABASE_ANON_KEY in the Secrets panel, or continue as guest.")
            )

        try {
            clientAuth.signInWith(Email) {
                email = trimmedEmail
                password = trimmedPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("Invalid login credentials", ignoreCase = true) == true ->
                    "Invalid email or password. Please check your credentials and try again."
                e.message?.contains("Email not confirmed", ignoreCase = true) == true ->
                    "Email not confirmed. To login without email confirmation, please turn off 'Confirm email' in your Supabase dashboard (Authentication -> Providers -> Email)."
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                        e.message?.contains("ConnectException", ignoreCase = true) == true ->
                    "Network error: Unable to reach Supabase. Please check your internet connection."
                else -> e.message ?: "Authentication failed. Please try again."
            }
            Result.failure(Exception(message, e))
        }
    }

    /**
     * Registers a new user with email and password via Supabase Auth and immediately signs them in.
     */
    suspend fun signUp(
        emailInput: String,
        passwordInput: String,
        displayName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedEmail = emailInput.trim()
        val trimmedPassword = passwordInput.trim()
        val trimmedName = displayName.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank() || trimmedName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("All fields are required."))
        }
        if (trimmedPassword.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val clientAuth = auth
            ?: return@withContext Result.failure(
                IllegalStateException("Supabase is not configured. Please set SUPABASE_URL and SUPABASE_ANON_KEY in the Secrets panel.")
            )

        try {
            clientAuth.signUpWith(Email) {
                email = trimmedEmail
                password = trimmedPassword
                data = buildJsonObject {
                    put("display_name", trimmedName)
                }
            }

            // Immediately sign in if no active session yet, bypassing client-side email confirmation requirement
            if (clientAuth.currentSessionOrNull() == null) {
                try {
                    clientAuth.signInWith(Email) {
                        email = trimmedEmail
                        password = trimmedPassword
                    }
                } catch (signInErr: Exception) {
                    if (signInErr.message?.contains("Email not confirmed", ignoreCase = true) == true) {
                        return@withContext Result.failure(
                            Exception("Account created! Supabase backend still has 'Confirm email' enabled. To allow immediate sign-in without email verification, disable 'Confirm email' under Supabase Dashboard -> Authentication -> Providers -> Email.")
                        )
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("already registered", ignoreCase = true) == true ->
                    "An account with this email already exists."
                else -> e.message ?: "Registration failed. Please try again."
            }
            Result.failure(Exception(message, e))
        }
    }

    /**
     * Requests a password reset link for the provided email.
     */
    suspend fun resetPassword(emailInput: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedEmail = emailInput.trim()
        if (trimmedEmail.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your email address."))
        }

        val clientAuth = auth
            ?: return@withContext Result.failure(
                IllegalStateException("Supabase is not configured.")
            )

        try {
            clientAuth.resetPasswordForEmail(trimmedEmail)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Password reset request failed.", e))
        }
    }

    /**
     * Signs out the current user session from Supabase Auth.
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth?.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
