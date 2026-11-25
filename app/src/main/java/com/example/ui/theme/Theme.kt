package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ScoutYellow,
    onPrimary = ScoutBrownDark,
    primaryContainer = ScoutGreenDark,
    onPrimaryContainer = ScoutGreenContainer,
    secondary = ScoutYellowLight,
    onSecondary = ScoutBrownDark,
    secondaryContainer = ScoutBrown,
    onSecondaryContainer = ScoutYellowContainer,
    tertiary = ScoutBrownLight,
    onTertiary = Color.White,
    background = Color(0xFF131A16),
    surface = Color(0xFF1B231F),
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF28342D),
    onSurfaceVariant = Color(0xFFC4D1C7)
)

private val LightColorScheme = lightColorScheme(
    primary = ScoutGreen,
    onPrimary = Color.White,
    primaryContainer = ScoutGreenContainer,
    onPrimaryContainer = ScoutGreenDark,
    secondary = ScoutYellow,
    onSecondary = ScoutBrownDark,
    secondaryContainer = ScoutYellowContainer,
    onSecondaryContainer = Color(0xFF5B4500),
    tertiary = ScoutBrown,
    onTertiary = Color.White,
    tertiaryContainer = ScoutBrownContainer,
    onTertiaryContainer = ScoutBrownDark,
    background = SneatBg,
    surface = SneatSurface,
    onBackground = SneatTextPrimary,
    onSurface = SneatTextPrimary,
    surfaceVariant = SneatSurfaceVariant,
    onSurfaceVariant = SneatTextSecondary
)

@Composable
fun PramukaGarudaTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // Set to false to preserve strict Pramuka Garuda identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
