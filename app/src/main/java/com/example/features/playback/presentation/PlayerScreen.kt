package com.example.features.playback.presentation

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.features.playback.domain.WatchProgress
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.SecondaryText
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    tmdbId: Int,
    mediaType: String,
    title: String? = null,
    posterPath: String? = null,
    viewModel: PlayerViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.playbackState.collectAsState()

    val exoPlayer = remember {
        viewModel.initializePlayer(
            context = context,
            tmdbId = tmdbId,
            mediaType = mediaType,
            passedTitle = title,
            passedPoster = posterPath
        )
    }

    var areControlsVisible by remember { mutableStateOf(true) }
    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableFloatStateOf(0f) }

    // Auto-hide controls after 4 seconds of inactivity if playing and not scrubbing
    LaunchedEffect(areControlsVisible, state.isPlaying, isUserScrubbing) {
        if (areControlsVisible && state.isPlaying && !isUserScrubbing && !state.showResumeDialog) {
            delay(4000)
            areControlsVisible = false
        }
    }

    val handleBack: () -> Unit = {
        viewModel.saveProgress(tmdbId, mediaType)
        onBackClick()
    }

    BackHandler(onBack = handleBack)

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveProgress(tmdbId, mediaType)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                areControlsVisible = !areControlsVisible
            }
    ) {
        // Video View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Controls
        AnimatedVisibility(
            visible = areControlsVisible || state.isBuffering || !state.isPlaying,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = handleBack,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.title.ifBlank { "Cinematic Playback" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (state.mediaType == "tv") "TV Series" else "Movie Feature",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricIndigo
                        )
                    }
                }

                // Center Controls: Rewind 10, Play/Pause, Forward 10
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = { viewModel.skipBackward(10_000L) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play / Pause / Buffering
                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isBuffering) {
                            CircularProgressIndicator(
                                color = ElectricIndigo,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(64.dp)
                            )
                        } else {
                            IconButton(
                                onClick = { viewModel.togglePlayPause() },
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(ElectricIndigo)
                            ) {
                                Icon(
                                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }

                    // Forward 10s
                    IconButton(
                        onClick = { viewModel.skipForward(10_000L) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Timeline & Scrubber Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    val currentPos = if (isUserScrubbing) {
                        (scrubPosition * state.duration).toLong()
                    } else {
                        state.currentPosition
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = WatchProgress.formatTimeMs(currentPos),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = WatchProgress.formatTimeMs(state.duration),
                            color = SecondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Slider(
                        value = if (isUserScrubbing) scrubPosition else state.progressFraction,
                        onValueChange = { value ->
                            isUserScrubbing = true
                            scrubPosition = value
                        },
                        onValueChangeFinished = {
                            isUserScrubbing = false
                            val targetMs = (scrubPosition * state.duration).toLong()
                            viewModel.seekTo(targetMs)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricIndigo,
                            activeTrackColor = ElectricIndigo,
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Resume Playback Dialog
        if (state.showResumeDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissResumeDialog() },
                containerColor = Color(0xFF13151F),
                titleContentColor = Color.White,
                textContentColor = SecondaryText,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Resume Playback?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = "You were watching at ${WatchProgress.formatTimeMs(state.resumePosition)}. Would you like to continue from where you left off?",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resumeFromSavedPosition() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Resume (${WatchProgress.formatTimeMs(state.resumePosition)})", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.startFromBeginning() }
                    ) {
                        Text("Start from Beginning", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }
    }
}
