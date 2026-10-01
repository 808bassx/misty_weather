package com.misty.weather.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying the 5-tier Liquid Glass hierarchy, elevation monotonicity,
 * and alpha parameter bounds.
 */
class MistyGlassTokensTest {

    @Test
    fun glassLevelFillAlphaIsOrdered() {
        // Tiers: None (0%) < Subtle (8%) < Regular (18%) < Interactive (26%) < Floating (36%) < Modal (44%)
        assertTrue(GlassLevel.None.fillAlpha < GlassLevel.Subtle.fillAlpha)
        assertTrue(GlassLevel.Subtle.fillAlpha < GlassLevel.Regular.fillAlpha)
        assertTrue(GlassLevel.Regular.fillAlpha < GlassLevel.Interactive.fillAlpha)
        assertTrue(GlassLevel.Interactive.fillAlpha < GlassLevel.Floating.fillAlpha)
        assertTrue(GlassLevel.Floating.fillAlpha < GlassLevel.Modal.fillAlpha)

        // None must be zero
        assertEquals(0f, GlassLevel.None.fillAlpha, 0.001f)
    }

    @Test
    fun glassLevelElevationIsOrdered() {
        assertTrue(GlassLevel.None.elevationDp <= GlassLevel.Subtle.elevationDp)
        assertTrue(GlassLevel.Subtle.elevationDp < GlassLevel.Regular.elevationDp)
        assertTrue(GlassLevel.Regular.elevationDp < GlassLevel.Interactive.elevationDp)
        assertTrue(GlassLevel.Interactive.elevationDp < GlassLevel.Floating.elevationDp)
        assertTrue(GlassLevel.Floating.elevationDp < GlassLevel.Modal.elevationDp)
    }

    @Test
    fun glassLevelParametersAreWithinValidBounds() {
        GlassLevel.entries.forEach { level ->
            assertTrue("${level.name} fillAlpha in [0..1]", level.fillAlpha in 0f..1f)
            assertTrue("${level.name} highlightAlpha in [0..1]", level.highlightAlpha in 0f..1f)
            assertTrue("${level.name} borderAlpha in [0..1]", level.borderAlpha in 0f..1f)
            assertTrue("${level.name} elevation >= 0", level.elevationDp >= 0f)
        }
    }

    @Test
    fun legacyAliasesMatchExpectedTiers() {
        assertEquals(GlassLevel.Subtle.fillAlpha, GlassLevel.UltraThin.fillAlpha, 0.001f)
        assertEquals(GlassLevel.Regular.fillAlpha, GlassLevel.Thin.fillAlpha, 0.001f)
        assertEquals(GlassLevel.Floating.fillAlpha, GlassLevel.Thick.fillAlpha, 0.001f)
    }
}
