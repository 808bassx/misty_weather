package com.example.mistyweather.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

// ─── Misty Weather Shape System ──────────────────────────────────────────────
// Unified corner radius tokens. Glass surfaces default to Large/ExtraLarge.
// No mixing of small Material defaults with large Misty glass panels.
// ─────────────────────────────────────────────────────────────────────────────

object MistyShapes {
    val Small      = RoundedCornerShape(MistyDimensions.CornerSmall)
    val Medium     = RoundedCornerShape(MistyDimensions.CornerMedium)
    val Large      = RoundedCornerShape(MistyDimensions.CornerLarge)
    val ExtraLarge = RoundedCornerShape(MistyDimensions.CornerExtraLarge)
    val Pill       = RoundedCornerShape(MistyDimensions.CornerPill)

    /** Glass panels default to ExtraLarge (28dp) for a refined, airy look. */
    val Glass      = ExtraLarge
}

/** Material 3 Shapes configured with Misty's rounded aesthetic. */
val MistyM3Shapes = Shapes(
    extraSmall = MistyShapes.Small,
    small      = MistyShapes.Small,
    medium     = MistyShapes.Medium,
    large      = MistyShapes.Large,
    extraLarge = MistyShapes.ExtraLarge,
)
