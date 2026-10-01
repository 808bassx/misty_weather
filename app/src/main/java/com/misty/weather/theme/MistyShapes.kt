package com.misty.weather.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

// ─── Misty Weather Shape System ──────────────────────────────────────────────
// Unified corner radius tokens. Surfaces have intentional hierarchy:
// Substrates (Subtle) → Cards (Medium/Large) → Floating (ExtraLarge) → Modals (Modal)
// No mixing of random radius values.
// ─────────────────────────────────────────────────────────────────────────────

object MistyShapes {
    /** Subtle 8dp radius for micro badges and search field insets. */
    val Subtle     = RoundedCornerShape(MistyDimensions.CornerSubtle)

    /** Small 12dp radius for small chips and icon containers. */
    val Small      = RoundedCornerShape(MistyDimensions.CornerSmall)

    /** Medium 16dp radius for standard list item cards. */
    val Medium     = RoundedCornerShape(MistyDimensions.CornerMedium)

    /** Large 22dp radius for primary weather panels. */
    val Large      = RoundedCornerShape(MistyDimensions.CornerLarge)

    /** ExtraLarge 28dp radius for floating navigation and elevated cards. */
    val ExtraLarge = RoundedCornerShape(MistyDimensions.CornerExtraLarge)

    /** Modal 32dp radius for full dialogs and bottom sheets. */
    val Modal      = RoundedCornerShape(MistyDimensions.CornerModal)

    /** Pill 999dp radius for action pills, status tags, and bottom nav pills. */
    val Pill       = RoundedCornerShape(MistyDimensions.CornerPill)

    /** Default glass panel shape. */
    val Glass      = ExtraLarge

    /** Floating surface shape. */
    val Floating   = ExtraLarge
}

/** Material 3 Shapes configured with Misty's rounded aesthetic. */
val MistyM3Shapes = Shapes(
    extraSmall = MistyShapes.Subtle,
    small      = MistyShapes.Small,
    medium     = MistyShapes.Medium,
    large      = MistyShapes.Large,
    extraLarge = MistyShapes.ExtraLarge,
)
