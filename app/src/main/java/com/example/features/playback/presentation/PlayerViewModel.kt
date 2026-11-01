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
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlayerViewModel : ViewModel() {
    private var exoPlayer: ExoPlayer? = null
    private val repository = WatchProgressRepository()
    private var progressJob: Job? = null

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Initial)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val videoRepository = VideoAssetRepository()

    fun getPlayer(context: Context, movie: Movie): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                viewModelScope.launch {
                    val videoUrl = videoRepository.resolveStreamUrl(movie.id, movie.mediaType ?: "movie")
                        ?: "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                    setMediaItem(MediaItem.fromUri(videoUrl))
                    prepare()
                }
                
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            startProgressSync(movie)
                        }
                    }
                })
            }
            loadInitialPosition(movie)
        }
        return exoPlayer!!
    }

    private fun loadInitialPosition(movie: Movie) {
        viewModelScope.launch {
            val progress = repository.getProgress(movie.id, movie.mediaType ?: "movie")
            if (progress != null && progress.lastPosition > 0) {
                _uiState.value = PlayerUiState.ResumePrompt(progress.lastPosition)
            } else {
                exoPlayer?.playWhenReady = true
            }
        }
    }

    fun resumeFrom(position: Long) {
        exoPlayer?.seekTo(position)
        exoPlayer?.playWhenReady = true
        _uiState.value = PlayerUiState.Initial
    }

    fun startFromBeginning() {
        exoPlayer?.seekTo(0)
        exoPlayer?.playWhenReady = true
        _uiState.value = PlayerUiState.Initial
    }

    private fun startProgressSync(movie: Movie) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                delay(5000) // Every 5 seconds
                syncProgress(movie)
            }
        }
    }

    private suspend fun syncProgress(movie: Movie) {
        val player = exoPlayer ?: return
        val userId = SupabaseManager.client?.auth?.currentUserOrNull()?.id ?: return
        
        val progress = WatchProgress(
            userId = userId,
            tmdbId = movie.id,
            mediaType = movie.mediaType ?: "movie",
            lastPosition = player.currentPosition,
            duration = player.duration
        )
        repository.saveProgress(progress)
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
}

sealed interface PlayerUiState {
    data object Initial : PlayerUiState
    data class ResumePrompt(val position: Long) : PlayerUiState
}
