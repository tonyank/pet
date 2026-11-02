package ytstudio.pet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralContainer,
    onPrimaryContainer = CoralDark,
    secondary = CoralLight,
    onSecondary = Color.White,
    background = SurfaceLight,
    surface = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4E2622),
    onPrimaryContainer = CoralLight,
    secondary = CoralLight,
    onSecondary = Color.Black,
    background = SurfaceDark,
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFECEFF1)
)

@Composable
fun PetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
