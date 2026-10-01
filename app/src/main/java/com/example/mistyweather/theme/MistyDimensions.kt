package com.example.mistyweather.theme

import androidx.compose.ui.unit.dp

// ─── Misty Weather Spacing & Dimension Tokens ────────────────────────────────
// All UI spacing, layout insets, and icon scales must use these tokens.
// Avoid raw dp literals in composables.
// ─────────────────────────────────────────────────────────────────────────────

object MistyDimensions {

    // ── Spacing Grid (4dp / 8dp rhythmic scale) ───────────────────────────────
    val Space2  =  2.dp
    val Space4  =  4.dp
    val Space6  =  6.dp
    val Space8  =  8.dp
    val Space10 = 10.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Space20 = 20.dp
    val Space24 = 24.dp
    val Space28 = 28.dp
    val Space32 = 32.dp
    val Space40 = 40.dp
    val Space48 = 48.dp
    val Space56 = 56.dp
    val Space64 = 64.dp
    val Space72 = 72.dp
    val Space80 = 80.dp
    val Space96 = 96.dp

    // ── Layout & Screen Margins ───────────────────────────────────────────────
    val ScreenHorizontalPadding = 16.dp
    val SectionSpacing          = 24.dp
    val HeroSpacing             = 40.dp
    val ForecastSpacing         = 16.dp
    val BottomSafeArea          = 88.dp   // Clearance above floating navigation
    val TopSafeArea             = 48.dp   // Status bar breathing space

    // ── Corner Radii ──────────────────────────────────────────────────────────
    val CornerSubtle     =  8.dp
    val CornerSmall      = 12.dp
    val CornerMedium     = 16.dp
    val CornerLarge      = 22.dp
    val CornerExtraLarge = 28.dp
    val CornerModal      = 32.dp
    val CornerPill       = 999.dp

    // ── Glass Surface Properties ──────────────────────────────────────────────
    val GlassCorner      = 24.dp   // Default glass panel corner radius
    val GlassBorderWidth =  1.dp   // Subtle 1dp glass hairline border

    // ── Touch Targets & Accessibility ─────────────────────────────────────────
    val MinTouchTarget  = 48.dp    // Accessibility: minimum interactive area

    // ── Navigation ────────────────────────────────────────────────────────────
    val NavBarHeight      = 64.dp
    val NavBarBottomInset = 16.dp  // Distance from screen bottom edge
    val NavBarHorizontal  = 24.dp  // Horizontal inset from screen edges

    // ── Forecast Metrics ──────────────────────────────────────────────────────
    val ForecastCardHorizontal = 16.dp
    val ForecastItemMinWidth   = 60.dp

    // ── Icon Scales (Design Hypotheses) ───────────────────────────────────────
    val IconXS      = 16.dp
    val IconSmall   = 20.dp
    val IconMedium  = 28.dp
    val IconLarge   = 40.dp
    val IconHero    = 72.dp

    // Semantic Weather Icon Scales
    val WeatherIconCompact  = 20.dp  // Mini indicators in dense rows
    val WeatherIconList     = 26.dp  // Icon pack preview cards
    val WeatherIconForecast = 28.dp  // Hourly forecast slots (elevated from 22dp)
    val WeatherIconHero     = 72.dp  // Primary hero weather presence (64-72dp hypothesis)
    val WeatherOrbSize       = 80.dp  // Phase F: Atmospheric celestial hero orb
    val WeatherOrbAuraRadius = 48.dp  // Ambient luminous aura radius

    // ── Ambient / Glow Effects ────────────────────────────────────────────────
    val SunGlowRadius  = 120.dp    // Radius of sun radial gradient
    val CloudBlobSize  = 200.dp    // Approximate cloud blob width
}
