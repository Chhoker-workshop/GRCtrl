package com.vanguard.grc.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val DarkScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = Color(0xFF05121F),
    primaryContainer = Navy600,
    onPrimaryContainer = Blue300,
    secondary = Gold500,
    onSecondary = Color(0xFF1A1408),
    secondaryContainer = Gold900,
    onSecondaryContainer = Gold300,
    tertiary = Navy600,
    onTertiary = White,
    background = Navy850,
    onBackground = TextHigh,
    surface = Navy800,
    onSurface = TextHigh,
    surfaceVariant = Graphite700,
    onSurfaceVariant = TextMid,
    surfaceContainer = Graphite800,
    surfaceContainerHigh = Graphite700,
    surfaceContainerHighest = Graphite600,
    surfaceContainerLow = Navy700,
    surfaceContainerLowest = Navy900,
    outline = Graphite500,
    outlineVariant = Graphite600,
    error = RiskCritical,
    onError = Color.White,
    errorContainer = Color(0xFF3A1A19),
    onErrorContainer = Color(0xFFF3C3C1),
    scrim = Color(0xCC04070D)
)

private val LightScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E8FA),
    onPrimaryContainer = Blue900,
    secondary = Gold600,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF6EBD2),
    onSecondaryContainer = Color(0xFF3A2D12),
    tertiary = Navy700,
    onTertiary = Color.White,
    background = Paper50,
    onBackground = Ink900,
    surface = Paper0,
    onSurface = Ink900,
    surfaceVariant = Paper100,
    onSurfaceVariant = Ink500,
    surfaceContainer = Paper100,
    surfaceContainerHigh = Paper200,
    surfaceContainerHighest = Paper200,
    surfaceContainerLow = Paper50,
    surfaceContainerLowest = Paper0,
    outline = Color(0xFFB9C4D3),
    outlineVariant = Paper200,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    scrim = Color(0x99000000)
)

/**
 * User-adjustable text scaling. Persisted by the settings layer and applied on
 * top of the base typography so the whole platform remains readable for
 * long-form study sessions.
 */
val LocalTextScale = staticCompositionLocalOf { 1.0f }

@Composable
fun GrcVanguardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    textScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = scheme.background.toArgb()
            window.navigationBarColor = scheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    @Suppress("UNUSED_EXPRESSION")
    LocalContext.current

    val scaled = scaledTypography(VanguardTypography, textScale)

    CompositionLocalProvider(LocalTextScale provides textScale) {
        MaterialTheme(
            colorScheme = scheme,
            typography = scaled,
            shapes = VanguardShapes,
            content = content
        )
    }
}

/**
 * Applies a multiplier to the base typography. Sizes and line heights are
 * scaled together so that layout rhythm is preserved at every scale factor.
 */
fun scaledTypography(base: Typography, scale: Float): Typography {
    if (scale == 1.0f) return base
    fun TextStyle.scaled() = copy(
        fontSize = fontSize * scale,
        lineHeight = lineHeight * scale
    )
    return Typography(
        displayLarge = base.displayLarge.scaled(),
        displayMedium = base.displayMedium.scaled(),
        displaySmall = base.displaySmall.scaled(),
        headlineLarge = base.headlineLarge.scaled(),
        headlineMedium = base.headlineMedium.scaled(),
        headlineSmall = base.headlineSmall.scaled(),
        titleLarge = base.titleLarge.scaled(),
        titleMedium = base.titleMedium.scaled(),
        titleSmall = base.titleSmall.scaled(),
        bodyLarge = base.bodyLarge.scaled(),
        bodyMedium = base.bodyMedium.scaled(),
        bodySmall = base.bodySmall.scaled(),
        labelLarge = base.labelLarge.scaled(),
        labelMedium = base.labelMedium.scaled(),
        labelSmall = base.labelSmall.scaled()
    )
}
