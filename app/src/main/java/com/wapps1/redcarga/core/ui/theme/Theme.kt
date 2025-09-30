package com.wapps1.redcarga.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RcLightColors = lightColorScheme(
    primary = RcColor4,              onPrimary = White,
    primaryContainer = RcColor2,     onPrimaryContainer = RcColor6,

    secondary = RcColor5,            onSecondary = White,
    secondaryContainer = RcColor3,   onSecondaryContainer = RcColor6,

    tertiary = RcColor3,             onTertiary = RcColor6,
    tertiaryContainer = RcColor7,    onTertiaryContainer = RcColor6,

    background = RcColor1,           onBackground = RcColor6,
    surface = RcColor1,              onSurface = RcColor6,
    surfaceVariant = RcColor7,       onSurfaceVariant = RcColor6,

    outline = RcColor8,
    error = RcError,                 onError = White,

    inverseSurface = Color(0xFF121212),
    inverseOnSurface = Color(0xFFEAEAEA),
    inversePrimary = RcColor5,
    surfaceTint = RcColor4,
    scrim = Black.copy(alpha = 0.32f)
)

private val RcDarkColors = darkColorScheme(
    primary = RcColor4,              onPrimary = White,
    primaryContainer = RcColor5,     onPrimaryContainer = White,

    secondary = RcColor5,            onSecondary = White,
    secondaryContainer = RcColor4,   onSecondaryContainer = White,

    tertiary = RcColor3,             onTertiary = RcColor6,
    tertiaryContainer = RcColor7,    onTertiaryContainer = RcColor6,

    background = Color(0xFF121212),  onBackground = Color(0xFFEAEAEA),
    surface = Color(0xFF1E1E1E),     onSurface = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFF2A2A2A), onSurfaceVariant = Color(0xFFCCCCCC),

    outline = RcColor8,
    error = RcError,                 onError = White,

    inverseSurface = Color(0xFFEAEAEA),
    inverseOnSurface = Color(0xFF121212),
    inversePrimary = RcColor4,
    surfaceTint = RcColor4,
    scrim = Black.copy(alpha = 0.32f)
)

@Composable
fun RedcargaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) RcDarkColors else RcLightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
