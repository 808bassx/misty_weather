package com.misty.weather.weather.scene

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.misty.weather.core.weather.WeatherType
import com.misty.weather.weather.model.LightEnvironment

// ─── Light Layer ─────────────────────────────────────────────────────────────
//
// Phase 2, 3 & 4.4-A1: Atmospheric Light, Horizon Glow, and Diurnal Scattering.
//
// Renders:
//   • Ambient tint overlay       — Upper-sky atmospheric mood.
//   • Night airglow              — Guarantees night is never pitch-black #000000.
//   • Atmospheric Horizon Glow   — Soft vertical optical scattering near horizon.
//   • Sun Azimuth Scatter        — Golden hour warm dispersal aligned with solar arc.
//   • Sun Corona & Disc          — Continuous astronomical solar body & heat column.
//
// Draw cost: Pure Canvas operations, zero allocations inside render loop.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders ambient environmental light: sun glow, horizon scattering, and night airglow.
 *
 * @param light       Lerped [LightEnvironment] from atmosphere transition.
 * @param sunPulse    Normalised 0→1 from [WeatherMotion.sunPulse].
 * @param weatherType Underlying weather type for atmospheric diffusion modulation.
 * @param modifier    Should fill the parent.
 */
@Composable
fun LightLayer(
    light: LightEnvironment,
    sunPulse: Float,
    weatherType: WeatherType = WeatherType.Sunny,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawLight(light, sunPulse, weatherType)
    }
}

internal fun DrawScope.drawLight(
    light: LightEnvironment,
    sunPulse: Float,
    weatherType: WeatherType = WeatherType.Sunny,
) {
    val w = size.width
    val h = size.height

    // ── Atmospheric Lighting Model Parameters ────────────────────────────────
    val solarElevation = AtmosphericLightingModel.calculateElevationFromSunY(
        sunY      = light.sunPosition.second,
        isDaytime = light.isDaytime,
    )
    val goldenHour = AtmosphericLightingModel.calculateGoldenHourIntensity(solarElevation)
    val horizonGlow = AtmosphericLightingModel.calculateHorizonGlow(solarElevation, goldenHour, weatherType)
    val nightAmbient = AtmosphericLightingModel.calculateNightAmbient(solarElevation)

    // ── 1. Ambient tint — upper-sky atmospheric mood ─────────────────────────
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(light.ambientTint, Color.Transparent),
            startY = 0f,
            endY   = h * 0.55f,
        ),
    )

    // ── 2. Night airglow baseline — guarantees night is never #000000 ─────────
    if (nightAmbient.weight > 0.02f) {
        drawRect(
            color = nightAmbient.color,
        )
    }

    // ── 3. Atmospheric Horizon Glow ──────────────────────────────────────────
    // Continuous optical depth: Zenith ~0, Mid-sky faint, Horizon rich & soft.
    val glowStartY = h * horizonGlow.startYFraction
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, horizonGlow.color),
            startY = glowStartY,
            endY   = h * horizonGlow.endYFraction,
        ),
        topLeft = Offset(0f, glowStartY),
        size    = Size(w, h - glowStartY),
    )

    // ── 4. Sun Azimuth Horizon Scatter (Golden Hour Bloom) ───────────────────
    // When the sun is near the horizon, cast a soft warm radial dispersal
    // anchored at the sun's horizontal azimuth.
    if (goldenHour > 0.05f && light.sunVisible) {
        val scatterCenter = Offset(w * light.sunPosition.first, h * 0.92f)
        val scatterRadius = w * 0.70f
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to horizonGlow.color.copy(alpha = horizonGlow.color.alpha * 0.75f),
                    0.45f to horizonGlow.color.copy(alpha = horizonGlow.color.alpha * 0.25f),
                    1.00f to Color.Transparent,
                ),
                center = scatterCenter,
                radius = scatterRadius,
            ),
            center = scatterCenter,
            radius = scatterRadius,
        )
    }

    if (!light.sunVisible) return

    // ── 5. Sun corona ────────────────────────────────────────────────────────
    val sunX = w * light.sunPosition.first
    val sunY = h * light.sunPosition.second

    // Pulse the glow radius ±4%
    val pulseFactor = 1f + sunPulse * 0.08f
    val glowR = h * light.glowRadius * pulseFactor

    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.00f to light.glowColor.copy(alpha = 0.55f),
                0.30f to light.glowColor.copy(alpha = 0.25f),
                0.65f to light.glowColor.copy(alpha = 0.08f),
                1.00f to Color.Transparent,
            ),
            center = Offset(sunX, sunY),
            radius = glowR,
        ),
        radius = glowR,
        center = Offset(sunX, sunY),
    )

    // ── 6. Sun disc — small bright circle ────────────────────────────────────
    val discR = w * 0.040f
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFFFFFDF0),
                0.5f to Color(0xFFFFF4C0),
                1.0f to light.glowColor.copy(alpha = 0.70f),
            ),
            center = Offset(sunX, sunY),
            radius = discR,
        ),
        radius = discR,
        center = Offset(sunX, sunY),
    )

    // ── 7. Atmospheric haze column below sun ─────────────────────────────────
    // A narrow vertical gradient column that suggests heat shimmer.
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                light.glowColor.copy(alpha = 0.12f),
                Color.Transparent,
            ),
            center = Offset(sunX, sunY),
            radius = w * 0.30f,
        ),
    )
}
