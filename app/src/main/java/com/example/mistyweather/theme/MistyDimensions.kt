package com.example.mistyweather.theme

import androidx.compose.ui.unit.dp

// ─── Misty Weather Spacing & Dimension Tokens ────────────────────────────────
// All UI spacing must use these tokens. Avoid raw dp literals in composables.
// ─────────────────────────────────────────────────────────────────────────────

object MistyDimensions {

    // ── Spacing ───────────────────────────────────────────────────────────────
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

    // ── Corner Radii ─────────────────────────────────────────────────────────
    val CornerSmall      = 12.dp
    val CornerMedium     = 16.dp
    val CornerLarge      = 22.dp
    val CornerExtraLarge = 28.dp
    val CornerPill       = 999.dp

    // ── Glass Surface ─────────────────────────────────────────────────────────
    val GlassCorner      = 22.dp   // Default glass panel corner
    val GlassBorderWidth =  1.dp   // Subtle 1dp glass border

    // ── Touch Targets ─────────────────────────────────────────────────────────
    val MinTouchTarget  = 48.dp    // Accessibility: minimum interactive area

    // ── Navigation ────────────────────────────────────────────────────────────
    val NavBarHeight      = 64.dp
    val NavBarBottomInset = 16.dp  // Distance from screen bottom edge
    val NavBarHorizontal  = 24.dp  // Horizontal inset from screen edges

    // ── Forecast Card ─────────────────────────────────────────────────────────
    val ForecastCardHorizontal = 16.dp
    val ForecastItemMinWidth   = 60.dp

    // ── Ambient / Glow Effects ────────────────────────────────────────────────
    val SunGlowRadius  = 120.dp    // Radius of sun radial gradient
    val CloudBlobSize  = 200.dp    // Approximate cloud blob width
}
