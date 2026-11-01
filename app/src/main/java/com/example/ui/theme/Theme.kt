package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ElectricIndigo,
    onPrimary = PrimaryText,
    primaryContainer = ElectricIndigo.copy(alpha = 0.2f),
    onPrimaryContainer = PrimaryText,
    secondary = SecondaryText,
    onSecondary = DeepBlack,
    background = DeepBlack,
    onBackground = PrimaryText,
    surface = Charcoal,
    onSurface = PrimaryText,
    surfaceVariant = DarkGrey,
    onSurfaceVariant = SecondaryText,
    error = ErrorColor,
    onError = DeepBlack,
    outline = MutedText
)

@Composable
fun MovieDBTheme(
    darkTheme: Boolean = true, // Force dark theme for cinematic experience
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            activity?.window?.let { window ->
                window.statusBarColor = DeepBlack.toArgb()
                window.navigationBarColor = DeepBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
