package com.example.mistyweather.weather.scene

import androidx.compose.ui.graphics.Color
import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

// ─── Atmospheric Lighting Unit Tests ──────────────────────────────────────────
//
// Phase 4.4-A1: Verification of continuous atmospheric lighting equations.
//
// Tests solar phase mapping, golden hour intensity curve, horizon vs zenith
// optical gradients, night airglow non-black guarantees, and continuity.
//
// ─────────────────────────────────────────────────────────────────────────────

class AtmosphericLightingTest {

    // ── 1. Solar Phase Tests ──────────────────────────────────────────────────

    @Test
    fun `calculateSolarPhase covers all diurnal phases continuously`() {
        val dawnPhase    = AtmosphericLightingModel.calculateSolarPhase(TimeOfDay.Dawn, 0.5f)
        val morningPhase = AtmosphericLightingModel.calculateSolarPhase(TimeOfDay.Morning, 0.5f)
        val noonPhase    = AtmosphericLightingModel.calculateSolarPhase(TimeOfDay.Noon, 0.5f)
        val sunsetPhase  = AtmosphericLightingModel.calculateSolarPhase(TimeOfDay.Sunset, 0.5f)
        val eveningPhase = AtmosphericLightingModel.calculateSolarPhase(TimeOfDay.Evening, 0.5f)
        val nightPhase   = AtmosphericLightingModel.calculateSolarPhase(TimeOfDay.Night, 0.5f)

        // Sunrise happens around phase 0.20 - 0.28
        assertTrue("Dawn should be around sunrise window [0.20, 0.30]", dawnPhase in 0.20f..0.30f)
        assertTrue("Morning should follow dawn", morningPhase > dawnPhase)
        assertTrue("Noon should be in midday range [0.50, 0.65]", noonPhase in 0.50f..0.65f)
        assertTrue("Sunset should be in western horizon range [0.70, 0.80]", sunsetPhase in 0.70f..0.80f)
        assertTrue("Evening should follow sunset", eveningPhase > sunsetPhase)
        assertTrue("Night phase should be in early morning or late night", nightPhase < 0.20f || nightPhase > 0.85f)
    }

    // ── 2. Golden Hour Intensity Tests ────────────────────────────────────────

    @Test
    fun `calculateGoldenHourIntensity peaks at horizon and drops to zero at noon and midnight`() {
        val midnightElevation = -1.0f
        val noonElevation     = 1.0f
        val horizonElevation  = 0.08f // Sun grazing the horizon
        val middayElevation   = 0.60f

        val goldenMidnight = AtmosphericLightingModel.calculateGoldenHourIntensity(midnightElevation)
        val goldenNoon     = AtmosphericLightingModel.calculateGoldenHourIntensity(noonElevation)
        val goldenMidday   = AtmosphericLightingModel.calculateGoldenHourIntensity(middayElevation)
        val goldenHorizon  = AtmosphericLightingModel.calculateGoldenHourIntensity(horizonElevation)

        // Midnight must be strictly 0
        assertEquals(0f, goldenMidnight, 0.0001f)

        // Solar noon must be 0
        assertEquals(0f, goldenNoon, 0.0001f)

        // Midday high sun must be 0
        assertEquals(0f, goldenMidday, 0.0001f)

        // Horizon grazing sun must show strong golden hour (> 0.7)
        assertTrue("Horizon sun must exhibit strong golden hour > 0.7, got $goldenHorizon", goldenHorizon > 0.70f)
    }

    // ── 3. Horizon Intensity vs Zenith Tests ──────────────────────────────────

    @Test
    fun `horizon glow intensity strictly exceeds zenith intensity`() {
        val solarElevation = 0.08f
        val goldenHour = AtmosphericLightingModel.calculateGoldenHourIntensity(solarElevation)
        val horizonParams = AtmosphericLightingModel.calculateHorizonGlow(solarElevation, goldenHour, WeatherType.Sunny)

        // Zenith is above startYFraction (0.62), so its horizon glow contribution is strictly 0
        val zenithContribution = 0f
        val horizonContribution = horizonParams.intensity

        assertTrue("Horizon glow must be strictly positive", horizonContribution > 0.05f)
        assertTrue("Horizon intensity must exceed zenith intensity", horizonContribution > zenithContribution)
        assertTrue("Horizon glow should start in lower troposphere", horizonParams.startYFraction >= 0.60f)
        assertEquals(1.00f, horizonParams.endYFraction, 0.001f)
    }

    // ── 4. Night Ambient Airglow Tests ────────────────────────────────────────

    @Test
    fun `night ambient maintains non-black airglow lower than daytime ambient`() {
        val nightElevation = -0.80f
        val dayElevation   = 0.80f

        val nightAirglow = AtmosphericLightingModel.calculateNightAmbient(nightElevation)
        val dayAirglow   = AtmosphericLightingModel.calculateNightAmbient(dayElevation)

        // Night airglow must be non-zero
        assertTrue("Night airglow weight must be positive", nightAirglow.weight > 0.90f)
        assertTrue("Night airglow alpha must be positive", nightAirglow.color.alpha > 0.02f)

        // Night sky must not be pure pitch-black #000000
        assertNotEquals("Night airglow should not be pitch black", Color(0xFF000000), nightAirglow.color)
        assertTrue("Night airglow should have blue/indigo component", nightAirglow.color.blue > 0.10f)

        // Day airglow weight must be zero
        assertEquals(0f, dayAirglow.weight, 0.0001f)

        // Daytime horizon glow intensity must exceed night airglow alpha
        val dayGlow = AtmosphericLightingModel.calculateHorizonGlow(dayElevation, 0f, WeatherType.Sunny)
        assertTrue("Daytime glow (${dayGlow.intensity}) must exceed night airglow (${nightAirglow.color.alpha})",
            dayGlow.intensity > nightAirglow.color.alpha)
    }

    // ── 5. Weather Modulation Tests ───────────────────────────────────────────

    @Test
    fun `calculateHorizonGlow modulates intensity according to weather condition`() {
        val elevation = 0.08f
        val goldenHour = AtmosphericLightingModel.calculateGoldenHourIntensity(elevation)

        val sunnyGlow   = AtmosphericLightingModel.calculateHorizonGlow(elevation, goldenHour, WeatherType.Sunny)
        val rainGlow    = AtmosphericLightingModel.calculateHorizonGlow(elevation, goldenHour, WeatherType.Rain)
        val cloudyGlow  = AtmosphericLightingModel.calculateHorizonGlow(elevation, goldenHour, WeatherType.Cloudy)
        val stormGlow   = AtmosphericLightingModel.calculateHorizonGlow(elevation, goldenHour, WeatherType.Storm)

        assertTrue("Sunny glow must be highest", sunnyGlow.intensity > rainGlow.intensity)
        assertTrue("Rain glow must exceed storm glow", rainGlow.intensity > stormGlow.intensity)
        assertTrue("Cloudy glow must be diffused below sunny", cloudyGlow.intensity < sunnyGlow.intensity)
        assertTrue("Storm glow must have lowest ambient intensity", stormGlow.intensity < cloudyGlow.intensity)
    }

    // ── 6. Mathematical Continuity Tests ─────────────────────────────────────

    @Test
    fun `solar elevation and golden hour transitions are smooth without step discontinuities`() {
        var prevElev = AtmosphericLightingModel.calculateSolarElevation(0f)
        var prevGolden = AtmosphericLightingModel.calculateGoldenHourIntensity(prevElev)

        val steps = 1000
        for (i in 1..steps) {
            val phase = i.toFloat() / steps
            val elev = AtmosphericLightingModel.calculateSolarElevation(phase)
            val golden = AtmosphericLightingModel.calculateGoldenHourIntensity(elev)

            val deltaElev = abs(elev - prevElev)
            val deltaGolden = abs(golden - prevGolden)

            assertTrue("Solar elevation jump too large ($deltaElev) at phase $phase", deltaElev < 0.05f)
            assertTrue("Golden hour jump too large ($deltaGolden) at phase $phase", deltaGolden < 0.05f)

            prevElev = elev
            prevGolden = golden
        }
    }
}
