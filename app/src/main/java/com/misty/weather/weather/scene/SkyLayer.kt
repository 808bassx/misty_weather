package com.misty.weather.weather.scene

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.misty.weather.weather.model.SkyEnvironment

// ─── Sky Layer ────────────────────────────────────────────────────────────────
//
// Phase 2 & 3 Layer 0: Full-screen sky gradient with diurnal star field.
//
// Renders:
//   • Three-stop vertical gradient from zenith to horizon.
//   • Subtle ambient luminosity micro-pulse.
//   • Night / Dusk star field when [starOpacity] > 0.
//
// Draw cost: 1 drawRect + tiny drawCircle ops for stars. Zero allocations.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders the full-screen sky background gradient.
 *
 * @param sky           Lerped [SkyEnvironment] from weather/atmosphere.
 * @param breathAlpha   Normalised 0→1 breath value from [WeatherMotion.ambientBreath].
 * @param starOpacity   Star field opacity (0 in daytime, fades in at dusk/night).
 * @param modifier      Should fill the parent (screen-size).
 */
@Composable
fun SkyLayer(
    sky: SkyEnvironment,
    breathAlpha: Float,
    starOpacity: Float = 0f,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawSky(sky, breathAlpha, starOpacity)
    }
}

internal fun DrawScope.drawSky(sky: SkyEnvironment, breathAlpha: Float, starOpacity: Float = 0f) {
    val breath = breathAlpha * 0.03f   // Max +3% luminosity pulse

    // Atmospheric multi-stop sky gradient: Zenith -> Mid-upper -> Mid -> Pre-horizon -> Horizon
    // Forms continuous vertical optical depth without sharp bands
    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to sky.zenith.brighten(breath * 0.5f),
                0.35f to sky.zenith.brighten(breath * 0.7f),
                0.65f to sky.mid.brighten(breath),
                0.86f to sky.horizon.copy(alpha = 0.95f),
                1.00f to sky.horizon,
            ),
        ),
    )

    // Subtle star field for evening / night / dawn
    if (starOpacity > 0.05f) {
        drawStars(starOpacity)
    }
}

private fun DrawScope.drawStars(starOpacity: Float) {
    val w = size.width
    val h = size.height

    for ((index, pos) in staticStarLocations.withIndex()) {
        val sx = pos.x * w
        val sy = pos.y * h
        // Varied radii & alphas for organic astronomical feel
        val starRadius = 1.0f + (index % 3) * 0.5f
        val individualAlpha = (0.50f + (index % 4) * 0.15f) * starOpacity

        drawCircle(
            color  = Color.White.copy(alpha = individualAlpha),
            radius = starRadius,
            center = Offset(sx, sy),
        )
    }
}

/** Pre-computed static star positions (normalized screen coordinates). */
private val staticStarLocations = listOf(
    Offset(0.12f, 0.07f),
    Offset(0.28f, 0.12f),
    Offset(0.44f, 0.05f),
    Offset(0.68f, 0.09f),
    Offset(0.85f, 0.06f),
    Offset(0.92f, 0.16f),
    Offset(0.18f, 0.20f),
    Offset(0.35f, 0.25f),
    Offset(0.55f, 0.15f),
    Offset(0.78f, 0.22f),
    Offset(0.08f, 0.28f),
    Offset(0.48f, 0.30f),
    Offset(0.62f, 0.26f),
    Offset(0.88f, 0.32f),
    Offset(0.24f, 0.35f),
    Offset(0.72f, 0.38f),
)

/** Adds a small luminosity lift to a colour without changing hue. */
private fun Color.brighten(amount: Float): Color {
    if (amount <= 0f) return this
    return Color(
        red   = (red   + amount).coerceAtMost(1f),
        green = (green + amount).coerceAtMost(1f),
        blue  = (blue  + amount).coerceAtMost(1f),
        alpha = alpha,
    )
}
