package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class SemanticColors(
    val statusGreenBg: Color,
    val statusGreenText: Color,
    val statusAmberBg: Color,
    val statusAmberText: Color,
    val statusRedBg: Color,
    val statusRedText: Color
)

private val LightSemanticColors = SemanticColors(
    statusGreenBg = StatusGreenBgLight,
    statusGreenText = StatusGreenTextLight,
    statusAmberBg = StatusAmberBgLight,
    statusAmberText = StatusAmberTextLight,
    statusRedBg = StatusRedBgLight,
    statusRedText = StatusRedTextLight
)

private val DarkSemanticColors = SemanticColors(
    statusGreenBg = StatusGreenBgDark,
    statusGreenText = StatusGreenTextDark,
    statusAmberBg = StatusAmberBgDark,
    statusAmberText = StatusAmberTextDark,
    statusRedBg = StatusRedBgDark,
    statusRedText = StatusRedTextDark
)

val LocalSemanticColors = staticCompositionLocalOf { LightSemanticColors }

private val LightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestOnPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = AmberOnSecondaryContainer,
    tertiary = CacaoTertiary,
    onTertiary = CacaoOnTertiary,
    tertiaryContainer = CacaoTertiaryContainer,
    onTertiaryContainer = CacaoOnTertiaryContainer,
    error = TerracottaError,
    onError = TerracottaOnError,
    errorContainer = TerracottaErrorContainer,
    onErrorContainer = TerracottaOnErrorContainer,
    background = LinenBackground,
    onBackground = EspressoOnBackground,
    surface = CreamSurface,
    onSurface = EspressoOnSurface,
    surfaceVariant = SandSurfaceVariant,
    onSurfaceVariant = UmberOnSurfaceVariant,
    outline = StoneOutline,
    outlineVariant = SubtleOutlineVariant,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkForestPrimary,
    onPrimary = DarkForestOnPrimary,
    primaryContainer = DarkForestPrimaryContainer,
    onPrimaryContainer = DarkForestOnPrimaryContainer,
    secondary = DarkAmberSecondary,
    onSecondary = DarkAmberOnSecondary,
    secondaryContainer = DarkAmberSecondaryContainer,
    onSecondaryContainer = DarkAmberOnSecondaryContainer,
    tertiary = DarkCacaoTertiary,
    onTertiary = DarkCacaoOnTertiary,
    tertiaryContainer = DarkCacaoTertiaryContainer,
    onTertiaryContainer = DarkCacaoOnTertiaryContainer,
    error = DarkTerracottaError,
    onError = DarkTerracottaOnError,
    errorContainer = DarkTerracottaErrorContainer,
    onErrorContainer = DarkTerracottaOnErrorContainer,
    background = DarkEspressoBackground,
    onBackground = DarkLinenOnBackground,
    surface = DarkEspressoSurface,
    onSurface = DarkLinenOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest
)

val TerroirShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TerroirStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors

    CompositionLocalProvider(LocalSemanticColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TerroirTypography,
            shapes = TerroirShapes,
            content = content
        )
    }
}
