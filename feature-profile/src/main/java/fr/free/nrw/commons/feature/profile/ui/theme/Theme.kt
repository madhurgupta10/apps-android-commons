package fr.free.nrw.commons.feature.profile.ui.theme

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

private val WikipediaLightColorScheme = lightColorScheme(
    primary = WikipediaPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001C3A),

    secondary = WikipediaSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8F4E4),
    onSecondaryContainer = Color(0xFF002118),

    tertiary = WikipediaAccent,
    onTertiary = Color(0xFF3D3000),
    tertiaryContainer = Color(0xFFFFE9A8),
    onTertiaryContainer = Color(0xFF3D3000),

    error = Color(0xFFD33333),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = WikiLightBackground,
    onBackground = WikiLightOnSurface,

    surface = WikiLightSurface,
    onSurface = WikiLightOnSurface,
    surfaceVariant = WikiLightSurfaceVariant,
    onSurfaceVariant = WikiLightOnSurfaceVariant,

    outline = WikiLightBorder,
    outlineVariant = Color(0xFFC8CCD0)
)

private val WikipediaDarkColorScheme = darkColorScheme(
    primary = Color(0xFF6699FF),
    onPrimary = Color(0xFF00205A),
    primaryContainer = Color(0xFF003C8F),
    onPrimaryContainer = Color(0xFFD6E4FF),

    secondary = Color(0xFF5DDDB8),
    onSecondary = Color(0xFF00382B),
    secondaryContainer = Color(0xFF005741),
    onSecondaryContainer = Color(0xFFB8F4E4),

    tertiary = Color(0xFFFFDD66),
    onTertiary = Color(0xFF3D3000),
    tertiaryContainer = Color(0xFF5C4800),
    onTertiaryContainer = Color(0xFFFFE9A8),

    error = Color(0xFFFF6B6B),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = WikiDarkBackground,
    onBackground = WikiDarkOnSurface,

    surface = WikiDarkSurface,
    onSurface = WikiDarkOnSurface,
    surfaceVariant = WikiDarkSurfaceVariant,
    onSurfaceVariant = WikiDarkOnSurfaceVariant,

    outline = WikiDarkBorder,
    outlineVariant = Color(0xFF54595D)
)

@Composable
fun CommonsAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use Wikipedia branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> WikipediaDarkColorScheme
        else -> WikipediaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}