package com.example.mistyweather.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Misty Weather Typography ────────────────────────────────────────────────
// Apple-like information hierarchy: Display → Headline → Title → Body → Label → Caption
// Temperature Display: ~80sp Light with negative letter-spacing — airy, cinematic.
// No ultra-bold weights. Light/Normal weights preferred for an airy aesthetic.
// ─────────────────────────────────────────────────────────────────────────────

/** Extended Misty-specific text styles beyond the Material 3 scale. */
object MistyType {

    /** Primary temperature display: "23°" — large, light, cinematic. */
    val TemperatureDisplay = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Light,
        fontSize      = 84.sp,
        lineHeight    = 84.sp,
        letterSpacing = (-2).sp,
    )

    /** Secondary temperature or large numeric value. */
    val TemperatureLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Light,
        fontSize      = 56.sp,
        lineHeight    = 60.sp,
        letterSpacing = (-1).sp,
    )

    /** City / Location name — prominent but not shouting. */
    val LocationDisplay = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 28.sp,
        lineHeight    = 34.sp,
        letterSpacing = (-0.3).sp,
    )

    /** Weather condition: "多云", "Sunny" */
    val ConditionLabel = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    )

    /** Small detail text: "体感 22°", "湿度 71%" */
    val DetailLabel = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.2.sp,
    )

    /** Forecast time label: "16:00", "现在" */
    val ForecastTime = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.1.sp,
    )

    /** Forecast temperature: "23°" in small card */
    val ForecastTemp = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Light,
        fontSize      = 16.sp,
        lineHeight    = 20.sp,
        letterSpacing = (-0.2).sp,
    )

    /** Navigation label */
    val NavLabel = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.3.sp,
    )

    /** Section header in glass cards */
    val SectionHeader = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.4.sp,
    )
}

/** Material 3 Typography using Misty's scale. */
val MistyM3Typography = Typography(
    displayLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Light,
        fontSize      = 57.sp,
        lineHeight    = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Light,
        fontSize      = 45.sp,
        lineHeight    = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Medium,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// Keep legacy alias so existing template code compiles unmodified.
val Typography = MistyM3Typography
