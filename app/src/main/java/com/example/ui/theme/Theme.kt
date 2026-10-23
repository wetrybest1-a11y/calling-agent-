package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = PureWhite,
    primaryContainer = Indigo50,
    onPrimaryContainer = Indigo700,
    secondary = Violet600,
    onSecondary = PureWhite,
    secondaryContainer = Violet50,
    onSecondaryContainer = Violet600,
    tertiary = Emerald600,
    onTertiary = PureWhite,
    tertiaryContainer = Emerald50,
    onTertiaryContainer = Emerald600,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    outlineVariant = Slate300,
    error = Rose600,
    onError = PureWhite,
    errorContainer = Rose50,
    onErrorContainer = Rose600
)

private val DarkColorScheme = darkColorScheme(
    primary = Indigo200,
    onPrimary = Slate950,
    primaryContainer = Slate800,
    onPrimaryContainer = Indigo100,
    secondary = Violet50,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = Violet50,
    tertiary = Emerald100,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    error = Rose600,
    onError = PureWhite
)

@Composable
fun CallPilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize CallPilot branded SaaS tokens
    content: @Composable () -> Unit,
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
