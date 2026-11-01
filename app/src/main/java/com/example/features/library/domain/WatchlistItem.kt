package com.example.features.library.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WatchlistItem(
    val id: Int? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("tmdb_id")
    val tmdbId: Int,
    @SerialName("media_type")
    val mediaType: String,
    val title: String,
    @SerialName("poster_path")
    val posterPath: String?,
    @SerialName("created_at")
    val createdAt: String? = null
)
