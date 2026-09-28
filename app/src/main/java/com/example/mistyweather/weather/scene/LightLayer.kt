package com.example.mistyweather.weather.scene

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.mistyweather.weather.model.LightEnvironment

// ─── Light Layer ─────────────────────────────────────────────────────────────
//
// Phase 2 Layer 1: Sun glow, ambient tint, and atmospheric scattering.
//
// Renders:
//   • Sun corona — large radial gradient centred at sunPosition.
//   • Sun disc   — small bright circle for the visible sun body.
//   • Ambient tint overlay — full-screen very-low-alpha rect for warm/cool mood.
//
// When sun is not visible (Cloudy, Rain, etc.), only the ambient tint draws.
// Draw cost: 1–3 Canvas ops. Negligible.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders ambient environmental light: sun glow + full-screen tint.
 *
 * @param light       Lerped [LightEnvironment] from [rememberWeatherTransition].
 * @param sunPulse    Normalised 0→1 from [WeatherMotion.sunPulse].
 *                    Drives a subtle corona radius oscillation (+8% max).
 * @param modifier    Should fill the parent.
 */
@Composable
fun LightLayer(
    light: LightEnvironment,
    sunPulse: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawLight(light, sunPulse)
    }
}

internal fun DrawScope.drawLight(light: LightEnvironment, sunPulse: Float) {
    val w = size.width
    val h = size.height

    // ── 1. Ambient tint — always rendered, very low alpha ───────────────────
    drawRect(brush = Brush.verticalGradient(
        colors = listOf(light.ambientTint, Color.Transparent),
        startY = 0f, endY = h * 0.55f,
    ))

    if (!light.sunVisible) return

    // ── 2. Sun corona ────────────────────────────────────────────────────────
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

    // ── 3. Sun disc — small bright circle ────────────────────────────────────
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

    // ── 4. Atmospheric haze column below sun ─────────────────────────────────
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
