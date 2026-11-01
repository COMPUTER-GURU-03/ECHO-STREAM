package com.example.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.features.discovery.domain.Movie
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.SecondaryText
import java.util.Locale

@Composable
fun VerticalMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF141721))
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.displayTitle,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Rating Badge overlay
            val rating = movie.voteAverage
            if (rating != null && rating > 0.0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", rating),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Text(
            text = movie.displayTitle,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (movie.releaseYear.isNotBlank()) {
                Text(
                    text = movie.releaseYear,
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryText
                )
            }
            if (movie.mediaType != null) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryText.copy(alpha = 0.5f)
                )
                Text(
                    text = if (movie.mediaType.equals("tv", ignoreCase = true)) "Series" else "Movie",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricIndigo.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun HeroBanner(
    movie: Movie,
    onDetailsClick: () -> Unit,
    onPlayClick: (() -> Unit)? = null
) {
    val imageUrl = if (!movie.backdropPath.isNullOrBlank()) movie.backdropUrl else movie.posterUrl

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp)
            .clickable(onClick = onDetailsClick)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = movie.displayTitle,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Multi-stop cinematic gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Genre / Type / Year pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                val rating = movie.voteAverage
                if (rating != null && rating > 0.0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricIndigo.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", rating),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (movie.releaseYear.isNotBlank()) {
                    Text(
                        text = movie.releaseYear,
                        color = SecondaryText,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                val mediaLabel = if (movie.mediaType.equals("tv", ignoreCase = true)) "Series" else "Movie"
                Text(
                    text = "•",
                    color = SecondaryText.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = mediaLabel,
                    color = ElectricIndigo,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = movie.displayTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!movie.overview.isNullOrBlank()) {
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onPlayClick?.invoke() ?: onDetailsClick() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedButton(
                    onClick = onDetailsClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White
                    ),
                    border = null,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Details",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (onSeeAllClick != null) {
            Text(
                text = "See All",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricIndigo,
                modifier = Modifier.clickable(onClick = onSeeAllClick)
            )
        }
    }
}
