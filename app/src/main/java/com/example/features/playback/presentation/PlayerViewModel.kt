package com.example.features.playback.presentation

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.core.data.SupabaseManager
import com.example.features.discovery.data.TMDBRepository
import com.example.features.discovery.domain.Movie
import com.example.features.playback.data.VideoAssetRepository
import com.example.features.playback.data.WatchProgressRepository
import com.example.features.playback.domain.WatchProgress
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerPlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isBuffering: Boolean = true,
    val title: String = "",
    val mediaType: String = "movie",
    val posterPath: String? = null,
    val showResumeDialog: Boolean = false,
    val resumePosition: Long = 0L,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() = if (duration > 0L) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
}

@OptIn(UnstableApi::class)
class PlayerViewModel : ViewModel() {
    private var exoPlayer: ExoPlayer? = null
    private val progressRepository = WatchProgressRepository()
    private val videoRepository = VideoAssetRepository()
    private val tmdbRepository = TMDBRepository()

    private var progressSyncJob: Job? = null
    private var timeTrackerJob: Job? = null

    private val _playbackState = MutableStateFlow(PlayerPlaybackState())
    val playbackState: StateFlow<PlayerPlaybackState> = _playbackState.asStateFlow()

    private var currentMovie: Movie? = null

    fun initializePlayer(
        context: Context,
        tmdbId: Int,
        mediaType: String,
        passedTitle: String? = null,
        passedPoster: String? = null
    ): ExoPlayer {
        if (exoPlayer != null) return exoPlayer!!

        val title = passedTitle?.takeIf { it.isNotBlank() && it != "Loading..." } ?: "Loading Title..."
        _playbackState.update {
            it.copy(
                title = title,
                mediaType = mediaType,
                posterPath = passedPoster
            )
        }

        // Fetch detailed title metadata in background if passedTitle was empty
        viewModelScope.launch {
            if (passedTitle.isNullOrBlank() || passedTitle == "Loading...") {
                val details = tmdbRepository.getTitleDetails(tmdbId, mediaType)
                if (details != null) {
                    _playbackState.update {
                        it.copy(
                            title = details.displayTitle,
                            posterPath = details.posterPath
                        )
                    }
                    currentMovie = details
                }
            }
        }

        val player = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _playbackState.update { it.copy(isBuffering = true) }
                        }
                        Player.STATE_READY -> {
                            _playbackState.update {
                                it.copy(
                                    isBuffering = false,
                                    duration = duration.coerceAtLeast(0L),
                                    currentPosition = currentPosition.coerceAtLeast(0L)
                                )
                            }
                        }
                        Player.STATE_ENDED -> {
                            _playbackState.update {
                                it.copy(isPlaying = false, currentPosition = duration)
                            }
                            saveProgress()
                        }
                        Player.STATE_IDLE -> {
                            _playbackState.update { it.copy(isBuffering = false) }
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playbackState.update { it.copy(isPlaying = isPlaying) }
                }
            })
        }

        exoPlayer = player

        // Start time tracker ticker
        startTimeTracker()

        // Load media source & resume progress
        viewModelScope.launch {
            try {
                val streamUrl = videoRepository.resolveStreamUrl(tmdbId, mediaType)
                player.setMediaItem(MediaItem.fromUri(streamUrl))
                player.prepare()

                // Check for existing progress
                val savedProgress = progressRepository.getProgress(tmdbId, mediaType)
                if (savedProgress != null && savedProgress.lastPosition > 10_000L && !savedProgress.isFinished) {
                    _playbackState.update {
                        it.copy(
                            showResumeDialog = true,
                            resumePosition = savedProgress.lastPosition
                        )
                    }
                } else {
                    player.playWhenReady = true
                }

                startPeriodicProgressSync(tmdbId, mediaType)
            } catch (e: Exception) {
                _playbackState.update {
                    it.copy(isBuffering = false, errorMessage = e.message ?: "Failed to load video stream")
                }
            }
        }

        return player
    }

    private fun startTimeTracker() {
        timeTrackerJob?.cancel()
        timeTrackerJob = viewModelScope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val dur = player.duration.coerceAtLeast(0L)
                    _playbackState.update {
                        it.copy(
                            currentPosition = pos,
                            duration = dur,
                            isPlaying = player.isPlaying
                        )
                    }
                }
                delay(300)
            }
        }
    }

    private fun startPeriodicProgressSync(tmdbId: Int, mediaType: String) {
        progressSyncJob?.cancel()
        progressSyncJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                saveProgress(tmdbId, mediaType)
            }
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            saveProgress()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        val clamped = positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L))
        player.seekTo(clamped)
        _playbackState.update { it.copy(currentPosition = clamped) }
    }

    fun skipForward(deltaMs: Long = 10_000L) {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition + deltaMs).coerceAtMost(player.duration.coerceAtLeast(0L))
        player.seekTo(newPos)
    }

    fun skipBackward(deltaMs: Long = 10_000L) {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition - deltaMs).coerceAtLeast(0L)
        player.seekTo(newPos)
    }

    fun resumeFromSavedPosition() {
        val pos = _playbackState.value.resumePosition
        exoPlayer?.seekTo(pos)
        exoPlayer?.playWhenReady = true
        _playbackState.update { it.copy(showResumeDialog = false) }
    }

    fun startFromBeginning() {
        exoPlayer?.seekTo(0L)
        exoPlayer?.playWhenReady = true
        _playbackState.update { it.copy(showResumeDialog = false) }
    }

    fun dismissResumeDialog() {
        exoPlayer?.playWhenReady = true
        _playbackState.update { it.copy(showResumeDialog = false) }
    }

    fun saveProgress(explicitTmdbId: Int? = null, explicitMediaType: String? = null) {
        val player = exoPlayer ?: return
        val position = player.currentPosition
        val duration = player.duration
        if (duration <= 0) return

        val state = _playbackState.value
        val userId = try { SupabaseManager.client?.auth?.currentUserOrNull()?.id } catch (_: Exception) { null } ?: "guest_user"

        viewModelScope.launch {
            val progress = WatchProgress(
                userId = userId,
                tmdbId = explicitTmdbId ?: (currentMovie?.id ?: 0),
                mediaType = explicitMediaType ?: state.mediaType,
                lastPosition = position,
                duration = duration,
                title = state.title,
                posterPath = state.posterPath
            )
            progressRepository.saveProgress(progress)
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveProgress()
        timeTrackerJob?.cancel()
        progressSyncJob?.cancel()
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }
}
