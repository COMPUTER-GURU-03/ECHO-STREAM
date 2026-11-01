package com.example.features.profile.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model representing a user profile in Supabase.
 * Maps to the `profiles` table in Supabase Postgrest.
 */
@Serializable
data class Profile(
    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: String = id ?: "",

    @SerialName("email")
    val email: String? = null,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("username")
    val username: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("bio")
    val bio: String? = null,

    @SerialName("favorite_genre")
    val favoriteGenre: String? = null,

    @SerialName("subscription_tier")
    val subscriptionTier: String? = "Standard",

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    /**
     * User-facing display name with fallback to username or email prefix.
     */
    val effectiveDisplayName: String
        get() = displayName?.ifBlank { null }
            ?: username?.ifBlank { null }
            ?: email?.substringBefore("@")
            ?: "Movie Fan"

    /**
     * Formatted two-letter initials for avatar placeholders.
     */
    val initials: String
        get() {
            val trimmed = effectiveDisplayName.trim()
            val tokens = trimmed.split(" ").filter { it.isNotBlank() }
            return when {
                tokens.size >= 2 -> "${tokens[0].first().uppercase()}${tokens[1].first().uppercase()}"
                tokens.isNotEmpty() && tokens[0].isNotEmpty() -> tokens[0].take(2).uppercase()
                else -> "U"
            }
        }
}
