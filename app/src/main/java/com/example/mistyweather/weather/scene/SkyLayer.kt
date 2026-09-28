package com.example.mistyweather.weather.scene

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.mistyweather.weather.model.SkyEnvironment

// ─── Sky Layer ────────────────────────────────────────────────────────────────
//
// Phase 2 Layer 0: Full-screen sky gradient.
//
// Renders a three-stop vertical gradient from zenith to horizon.
// No animation — the sky colours themselves animate via WeatherTransition lerp.
// A subtle luminosity micro-breath is expressed via the ambientBreath parameter.
//
// Draw cost: ONE drawRect call. Extremely cheap.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders the full-screen sky background gradient.
 *
 * @param sky           Lerped [SkyEnvironment] from [rememberWeatherTransition].
 * @param breathAlpha   Normalised 0→1 breath value from [WeatherMotion.ambientBreath].
 *                      Adds a barely-perceptible luminosity micro-pulse (+3% max).
 * @param modifier      Should fill the parent (screen-size).
 */
@Composable
fun SkyLayer(
    sky: SkyEnvironment,
    breathAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawSky(sky, breathAlpha)
    }
}

internal fun DrawScope.drawSky(sky: SkyEnvironment, breathAlpha: Float) {
    val breath = breathAlpha * 0.03f   // Max +3% luminosity pulse — barely visible

    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to sky.zenith.brighten(breath * 0.5f),
                0.45f to sky.mid.brighten(breath),
                1.00f to sky.horizon,
            ),
        ),
    )
}

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
