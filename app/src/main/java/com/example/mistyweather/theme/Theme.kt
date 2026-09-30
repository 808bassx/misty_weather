package com.example.mistyweather.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

// ─── Misty Weather Color Schemes ─────────────────────────────────────────────

private val MistyLightColorScheme = lightColorScheme(
    primary          = MistyColors.LightPrimary,
    onPrimary        = MistyColors.LightOnPrimary,
    background       = MistyColors.LightBackground,
    onBackground     = MistyColors.LightOnBackground,
    surface          = MistyColors.LightSurface,
    onSurface        = MistyColors.LightOnSurface,
    surfaceVariant   = MistyColors.MistBlue,
    onSurfaceVariant = MistyColors.TextSecondary,
    outline          = MistyColors.GlassBorderSubtle,
)

private val MistyDarkColorScheme = darkColorScheme(
    primary          = MistyColors.DarkPrimary,
    onPrimary        = MistyColors.DarkOnPrimary,
    background       = MistyColors.DarkBackground,
    onBackground     = MistyColors.DarkOnBackground,
    surface          = MistyColors.DarkSurface,
    onSurface        = MistyColors.DarkOnSurface,
    surfaceVariant   = MistyColors.IndigoGray,
    onSurfaceVariant = MistyColors.TextSecondaryDark,
    outline          = MistyColors.GlassBorderSubtle,
)

// ─── Composition Locals for Design Tokens ─────────────────────────────────────

internal val LocalMistyDarkTheme = staticCompositionLocalOf { false }

val LocalMistyColors = staticCompositionLocalOf { MistyLightPalette }

/** Returns true when the current Misty theme is in dark mode. */
val isMistyDark: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalMistyDarkTheme.current

// ─── Unified Semantic Token Accessors ─────────────────────────────────────────

object MistyTheme {
    val colors: MistyColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalMistyColors.current

    val typography: MistyType
        get() = MistyType

    val dimensions: MistyDimensions
        get() = MistyDimensions

    val shapes: MistyShapes
        get() = MistyShapes

    val motion: MistyMotion
        get() = MistyMotion
}

object MistyTokens {
    val colors: MistyColorPalette
        @Composable
        @ReadOnlyComposable
        get() = MistyTheme.colors

    val typography: MistyType
        get() = MistyTheme.typography

    val dimensions: MistyDimensions
        get() = MistyTheme.dimensions

    val shapes: MistyShapes
        get() = MistyTheme.shapes

    val motion: MistyMotion
        get() = MistyTheme.motion
}

// ─── MistyWeatherTheme ────────────────────────────────────────────────────────

/**
 * The single theme wrapper for the entire Misty Weather application.
 *
 * - Applies Misty's [MistyLightColorScheme] / [MistyDarkColorScheme].
 * - On Android 12+ (API 31+), falls back to dynamic color only when
 *   [dynamicColor] is true — but Misty's own palette is the default.
 * - Provides [LocalMistyDarkTheme], [LocalMistyColors], and [LocalMistyReduceMotion]
 *   so all composables and glass surfaces self-adapt.
 * - Uses [MistyM3Typography] and [MistyM3Shapes] throughout.
 */
@Composable
fun MistyWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MistyDarkColorScheme
        else      -> MistyLightColorScheme
    }

    val palette = if (darkTheme) MistyDarkPalette else MistyLightPalette

    CompositionLocalProvider(
        LocalMistyDarkTheme provides darkTheme,
        LocalMistyColors provides palette,
        LocalMistyReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = MistyM3Typography,
            shapes      = MistyM3Shapes,
            content     = content,
        )
    }
}
