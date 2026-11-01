package com.example.features.playback.presentation

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.features.discovery.domain.Movie

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    movie: Movie,
    viewModel: PlayerViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val exoPlayer = remember { viewModel.getPlayer(context, movie) }

    DisposableEffect(Unit) {
        onDispose {
            // Player is released in ViewModel's onCleared or when ViewModel is cleared
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // UI State handling (Resume Prompt)
        when (val state = uiState) {
            is PlayerUiState.ResumePrompt -> {
                ResumeDialog(
                    position = state.position,
                    onStartFromBeginning = { viewModel.startFromBeginning() },
                    onResume = { viewModel.resumeFrom(state.position) }
                )
            }
            else -> {}
        }
    }
}

@Composable
fun ResumeDialog(
    position: Long,
    onStartFromBeginning: () -> Unit,
    onResume: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onResume,
        title = { Text("Resume Playback?") },
        text = { Text("Would you like to resume from where you left off?") },
        confirmButton = {
            Button(onClick = onResume) {
                Text("Resume")
            }
        },
        dismissButton = {
            TextButton(onClick = onStartFromBeginning) {
                Text("Start from beginning")
            }
        }
    )
}
