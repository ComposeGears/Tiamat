package composegears.tiamat.example.ui.core

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

class LocalThemConfig {
    var isDarkMode by mutableStateOf(false)
}

val LocalThemeConfig = staticCompositionLocalOf { LocalThemConfig() }

private val color_killMyEyesPlease = Color(0xffbd17b5)
private val color_primary = Color(0xff1e66d6)

private val LightColors = lightColorScheme(
    primary = color_primary,
    onPrimary = Color.White,
//    primaryContainer = color_killMyEyesPlease,
//    onPrimaryContainer = color_killMyEyesPlease,
//    inversePrimary = color_killMyEyesPlease,
//    secondary = color_killMyEyesPlease,
//    onSecondary = color_killMyEyesPlease,
//    secondaryContainer = color_killMyEyesPlease,
//    onSecondaryContainer = color_killMyEyesPlease,
//    tertiary = color_killMyEyesPlease,
//    onTertiary = color_killMyEyesPlease,
//    tertiaryContainer = color_killMyEyesPlease,
//    onTertiaryContainer = color_killMyEyesPlease,
//    background = color_killMyEyesPlease,
//    onBackground = color_killMyEyesPlease,
    surface = Color.White,
//    onSurface = color_killMyEyesPlease,
//    surfaceVariant = color_killMyEyesPlease,
//    onSurfaceVariant = color_killMyEyesPlease,
    surfaceTint = Color.Gray,
//    inverseSurface = color_killMyEyesPlease,
//    inverseOnSurface = color_killMyEyesPlease,
//    error = color_killMyEyesPlease,
//    onError = color_killMyEyesPlease,
//    errorContainer = color_killMyEyesPlease,
//    onErrorContainer = color_killMyEyesPlease,
//    outline = color_killMyEyesPlease,
//    outlineVariant = color_killMyEyesPlease,
//    scrim = color_killMyEyesPlease,
//    surfaceBright = color_killMyEyesPlease,
//    surfaceContainer = color_killMyEyesPlease,
//    surfaceContainerHigh = color_killMyEyesPlease,
//    surfaceContainerHighest = color_killMyEyesPlease,
//    surfaceContainerLow = color_killMyEyesPlease,
//    surfaceContainerLowest = color_killMyEyesPlease,
//    surfaceDim = color_killMyEyesPlease,
)

private val DarkColors = darkColorScheme(
    primary = color_primary,
    onPrimary = Color.White,
//    primaryContainer = color_killMyEyesPlease,
//    onPrimaryContainer = color_killMyEyesPlease,
//    inversePrimary = color_killMyEyesPlease,
//    secondary = color_killMyEyesPlease,
//    onSecondary = color_killMyEyesPlease,
//    secondaryContainer = color_killMyEyesPlease,
//    onSecondaryContainer = color_killMyEyesPlease,
//    tertiary = color_killMyEyesPlease,
//    onTertiary = color_killMyEyesPlease,
//    tertiaryContainer = color_killMyEyesPlease,
//    onTertiaryContainer = color_killMyEyesPlease,
//    background = color_killMyEyesPlease,
//    onBackground = color_killMyEyesPlease,
    surface = Color(0xff1e1f22),
//    onSurface = color_killMyEyesPlease,
//    surfaceVariant = color_killMyEyesPlease,
//    onSurfaceVariant = color_killMyEyesPlease,
    surfaceTint = Color.Gray,
//    inverseSurface = color_killMyEyesPlease,
//    inverseOnSurface = color_killMyEyesPlease,
//    error = color_killMyEyesPlease,
//    onError = color_killMyEyesPlease,
//    errorContainer = color_killMyEyesPlease,
//    onErrorContainer = color_killMyEyesPlease,
//    outline = color_killMyEyesPlease,
    outlineVariant = Color(0xff393b40),
//    scrim = color_killMyEyesPlease,
//    surfaceBright = color_killMyEyesPlease,
//    surfaceContainer = color_killMyEyesPlease,
//    surfaceContainerHigh = color_killMyEyesPlease,
//    surfaceContainerHighest = color_killMyEyesPlease,
//    surfaceContainerLow = color_killMyEyesPlease,
//    surfaceContainerLowest = color_killMyEyesPlease,
//    surfaceDim = color_killMyEyesPlease,
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val themeConfig = LocalThemeConfig.current
    MaterialTheme(
        colorScheme = if (themeConfig.isDarkMode) DarkColors else LightColors,
        content = content
    )
}