@file:Suppress("MissingPackageDeclaration")

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val color_active1 = Color(0xff375fad)
private val color_active2 = Color(0xff549159)
private val color_active3 = Color(0xff8e5f34)
private val color_onActive = Color(0xffdfe1e5)
private val color_bg1 = Color(0xff2b2d30)
private val color_bg2 = Color(0xff43454a)
private val color_onBg = Color(0xffdfe1e5)
private val color_outline = Color(0xff393b40)

private val DarkColors = darkColorScheme(
    primary = color_active1,
    onPrimary = color_onActive,
    secondary = color_active2,
    onSecondary = color_onActive,
    surface = color_bg1,
    onSurface = color_onBg,
    surfaceVariant = color_bg2,
    onSurfaceVariant = color_onBg,
    outline = color_onBg,
    primaryContainer = Color.Black,
    onPrimaryContainer = Color.Black,
    secondaryContainer = color_bg1,
    onSecondaryContainer = color_onBg,
    tertiary = color_active3,
    onTertiary = color_onBg,
    tertiaryContainer = Color.Blue,
    onTertiaryContainer = Color.Black,
    error = Color.Black,
    errorContainer = Color.Black,
    onError = Color.Black,
    onErrorContainer = Color.Black,
    background = Color.Black,
    onBackground = Color.Black,
    inverseOnSurface = Color.Black,
    inverseSurface = Color.Black,
    inversePrimary = Color.Black,
    surfaceTint = Color.Black,
    outlineVariant = color_outline,
    scrim = Color.Black,
)

@Composable
internal fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}