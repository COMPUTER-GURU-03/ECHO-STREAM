package com.example.features.playback.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

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
    val title: String? = null,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    val progressPercentage: Float
        get() = if (duration > 0L) (lastPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    val remainingMinutes: Long
        get() = if (duration > lastPosition) ((duration - lastPosition) / 1000L / 60L).coerceAtLeast(1L) else 0L

    val isFinished: Boolean
        get() = progressPercentage >= 0.95f || (duration > 0 && duration - lastPosition < 15_000L)

    val formattedRemainingTime: String
        get() {
            val minutes = remainingMinutes
            return when {
                minutes >= 60 -> "${minutes / 60}h ${minutes % 60}m left"
                else -> "${minutes}m left"
            }
        }

    val formattedLastPosition: String
        get() = formatTimeMs(lastPosition)

    val formattedDuration: String
        get() = formatTimeMs(duration)

    companion object {
        fun formatTimeMs(timeMs: Long): String {
            if (timeMs <= 0L) return "00:00"
            val totalSeconds = timeMs / 1000L
            val hours = totalSeconds / 3600L
            val minutes = (totalSeconds % 3600L) / 60L
            val seconds = totalSeconds % 60L

            return if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }
    }
}
