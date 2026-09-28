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
    outline          = MistyColors.GlassBorder,
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
    outline          = MistyColors.GlassBorder,
)

// ─── Composition Local for dark-mode awareness in glass components ────────────

internal val LocalMistyDarkTheme = staticCompositionLocalOf { false }

/** Returns true when the current Misty theme is in dark mode. */
val isMistyDark: Boolean
    @Composable get() = LocalMistyDarkTheme.current

// ─── MistyWeatherTheme ────────────────────────────────────────────────────────

/**
 * The single theme wrapper for the entire Misty Weather app.
 *
 * - Applies Misty's [MistyLightColorScheme] / [MistyDarkColorScheme].
 * - On Android 12+ (API 31+), falls back to dynamic color only when
 *   [dynamicColor] is true — but Misty's own palette is the default.
 * - Provides [LocalMistyDarkTheme] so glass surfaces can self-adapt.
 * - Uses [MistyM3Typography] and [MistyM3Shapes] throughout.
 */
@Composable
fun MistyWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled by default — Misty has its own intentional palette.
    dynamicColor: Boolean = false,
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

    CompositionLocalProvider(LocalMistyDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = MistyM3Typography,
            shapes      = MistyM3Shapes,
            content     = content,
        )
    }
}

/** Convenience alias so call-sites can use either name. */
typealias MistyTheme = Unit // structural alias — use MistyWeatherTheme { }
