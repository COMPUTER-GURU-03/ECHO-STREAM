package com.example.features.playback.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WatchProgress(
    val id: Int? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("tmdb_id")
    val tmdbId: Int,
    @SerialName("media_type")
    val mediaType: String,
    @SerialName("last_position")
    val lastPosition: Long,
    val duration: Long,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
