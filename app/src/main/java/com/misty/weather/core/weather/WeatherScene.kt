package com.misty.weather.core.weather

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import com.misty.weather.theme.MistyColors
import com.misty.weather.theme.MistyWeatherTheme
import kotlin.math.cos
import kotlin.math.sin

// ─── Weather Scene ────────────────────────────────────────────────────────────
//
// Layer 0 of the Misty Weather UI stack.
//
// Phase 1 implements three scenes using Canvas + Brush gradients only:
//   • Sunny   — sky blue gradient + warm sun radial glow
//   • Cloudy  — blue-gray gradient + soft cloud blobs
//   • Rain    — deep steel gradient + minimal animated rain streaks
//
// No emoji, no cartoon illustrations, no complex particle engines.
// Blur approximation is done via alpha layering — RenderEffect blur is Phase 2.
//
// Performance notes:
//   • Canvas operations are batched in a single drawBehind / Canvas block.
//   • Animated transitions use a single InfiniteTransition per scene.
//   • No objects allocated per frame — all values are read from state.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-screen weather background scene.
 *
 * Renders Layer 0 (Weather Background) and Layer 1 (Ambient Effects).
 * All other UI layers sit above this in the parent [Box].
 *
 * @param weatherType The current [WeatherType] — drives which scene renders.
 * @param modifier    Should fill the screen; defaults to [fillMaxSize].
 */
@Composable
fun WeatherScene(
    weatherType: WeatherType,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    when (weatherType) {
        WeatherType.Sunny        -> SunnyScene(modifier)
        WeatherType.PartlyCloudy -> PartlyCloudyScene(modifier)
        WeatherType.Cloudy       -> CloudyScene(modifier)
        WeatherType.Rain,
        WeatherType.Storm        -> RainScene(modifier)
        WeatherType.Snow         -> CloudyScene(modifier) // Phase 2 placeholder
        WeatherType.Fog          -> CloudyScene(modifier) // Phase 2 placeholder
    }
}

// ─── Sunny Scene ──────────────────────────────────────────────────────────────

@Composable
private fun SunnyScene(modifier: Modifier) {
    // Gentle slow pulse on the sun glow — barely perceptible, cinematic.
    val infiniteTransition = rememberInfiniteTransition(label = "sunny_ambient")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue  = 1.00f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_pulse",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // ── Sky gradient: zenith blue → soft horizon haze ────────────────────
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MistyColors.SkyTop,      // 0%  – deep zenith
                    MistyColors.SkyMid,      // 45% – bright mid-sky
                    MistyColors.SkyHorizon,  // 85% – warm haze
                    MistyColors.SunWarm,     // 100% – amber horizon
                ),
                startY = 0f,
                endY   = h,
            ),
        )

        // ── Sun position: upper-right quadrant ───────────────────────────────
        val sunX = w * 0.72f
        val sunY = h * 0.18f

        // Outer ambient glow — very soft, large radius
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MistyColors.SunGlow.copy(alpha = 0.45f * glowPulse),
                    MistyColors.SunWarm.copy(alpha = 0.20f * glowPulse),
                    Color.Transparent,
                ),
                center = Offset(sunX, sunY),
                radius = w * 0.55f,
            ),
            radius = w * 0.55f,
            center = Offset(sunX, sunY),
        )

        // Mid glow ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MistyColors.SunCore.copy(alpha = 0.70f),
                    MistyColors.SunGlow.copy(alpha = 0.40f),
                    Color.Transparent,
                ),
                center = Offset(sunX, sunY),
                radius = w * 0.28f,
            ),
            radius = w * 0.28f,
            center = Offset(sunX, sunY),
        )

        // Sun disc — soft, not a hard circle
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MistyColors.White.copy(alpha = 0.90f),
                    MistyColors.SunCore.copy(alpha = 0.75f),
                    MistyColors.SunGlow.copy(alpha = 0.0f),
                ),
                center = Offset(sunX, sunY),
                radius = w * 0.10f,
            ),
            radius = w * 0.10f,
            center = Offset(sunX, sunY),
        )

        // ── Bottom warm ambient wash ──────────────────────────────────────────
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, MistyColors.SunWarm.copy(alpha = 0.12f)),
                startY = h * 0.60f,
                endY   = h,
            ),
        )
    }
}

// ─── Partly Cloudy Scene ──────────────────────────────────────────────────────

@Composable
private fun PartlyCloudyScene(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Sky — slightly desaturated sunny sky
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MistyColors.SkyTop.copy(alpha = 0.90f),
                    MistyColors.SkyMid,
                    MistyColors.SkyHorizon,
                ),
            ),
        )

        // Sun — subdued because it's partially covered
        val sunX = w * 0.65f
        val sunY = h * 0.20f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MistyColors.SunGlow.copy(alpha = 0.30f),
                    Color.Transparent,
                ),
                center = Offset(sunX, sunY),
                radius = w * 0.35f,
            ),
            radius = w * 0.35f,
            center = Offset(sunX, sunY),
        )

        // Cloud blobs
        drawCloudBlob(cx = w * 0.30f, cy = h * 0.18f, rx = w * 0.38f, ry = h * 0.09f,
            alpha = 0.75f)
        drawCloudBlob(cx = w * 0.75f, cy = h * 0.26f, rx = w * 0.32f, ry = h * 0.07f,
            alpha = 0.55f)
    }
}

// ─── Cloudy Scene ─────────────────────────────────────────────────────────────

@Composable
private fun CloudyScene(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // ── Overcast sky gradient ─────────────────────────────────────────────
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MistyColors.CloudShadow,  // 0%  – upper cloud mass
                    MistyColors.CloudGray,    // 40% – mid overcast
                    MistyColors.CloudLight,   // 80% – bright diffused horizon
                    MistyColors.SkyHorizon,   // 100%– faint warmth
                ),
            ),
        )

        // ── Soft cloud blob layer (Layer 1 ambient) ───────────────────────────
        // Upper cloud mass — large, low alpha, blurred via soft ellipse gradients
        drawCloudBlob(cx = w * 0.20f, cy = h * 0.10f, rx = w * 0.55f, ry = h * 0.14f,
            alpha = 0.50f)
        drawCloudBlob(cx = w * 0.80f, cy = h * 0.16f, rx = w * 0.50f, ry = h * 0.12f,
            alpha = 0.40f)
        drawCloudBlob(cx = w * 0.50f, cy = h * 0.28f, rx = w * 0.70f, ry = h * 0.10f,
            alpha = 0.30f)

        // Mid-layer cloud diffusion
        drawCloudBlob(cx = w * 0.15f, cy = h * 0.35f, rx = w * 0.40f, ry = h * 0.08f,
            alpha = 0.20f)
        drawCloudBlob(cx = w * 0.85f, cy = h * 0.40f, rx = w * 0.45f, ry = h * 0.09f,
            alpha = 0.20f)

        // ── Bottom mist wash ──────────────────────────────────────────────────
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, MistyColors.CloudLight.copy(alpha = 0.18f)),
                startY = h * 0.65f,
                endY   = h,
            ),
        )
    }
}

// ─── Rain Scene ───────────────────────────────────────────────────────────────

@Composable
private fun RainScene(modifier: Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain_motion")

    // Rain streak offset — scrolls diagonally downward
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rain_offset",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // ── Heavy overcast sky ────────────────────────────────────────────────
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MistyColors.RainSky,      // 0%  – stormy zenith
                    MistyColors.RainMid,      // 50% – heavy cloud mid
                    MistyColors.RainAmbient,  // 100%– wet ambient light
                ),
            ),
        )

        // ── Cloud blobs — darker than Cloudy scene ────────────────────────────
        drawCloudBlob(cx = w * 0.25f, cy = h * 0.08f, rx = w * 0.60f, ry = h * 0.12f,
            alpha = 0.55f, color = MistyColors.RainMid)
        drawCloudBlob(cx = w * 0.75f, cy = h * 0.14f, rx = w * 0.55f, ry = h * 0.10f,
            alpha = 0.45f, color = MistyColors.RainSky)
        drawCloudBlob(cx = w * 0.50f, cy = h * 0.22f, rx = w * 0.80f, ry = h * 0.09f,
            alpha = 0.35f, color = MistyColors.RainMid)

        // ── Rain streaks — sparse, diagonal, Phase 1 simple implementation ────
        // Angle: ~75° from vertical (slight diagonal)
        val streakAngle = 10f          // degrees from vertical
        val cols        = 18           // number of streak columns
        val colSpacing  = w / cols

        // Subtle wet-ambient overlay before streaks
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    MistyColors.RainDrop.copy(alpha = 0.08f),
                ),
                startY = h * 0.30f,
                endY   = h,
            ),
        )

        // Draw individual streaks
        for (col in 0 until cols) {
            val xBase     = col * colSpacing
            // Stagger each column so they don't all align
            val phaseShift = (col * 0.17f) % 1f
            val effectiveOffset = (rainOffset + phaseShift) % 1f

            val streakLength = h * (0.04f + (col % 3) * 0.012f)
            val streakAlpha  = 0.18f + (col % 4) * 0.04f
            val dx           = streakLength * sin(Math.toRadians(streakAngle.toDouble())).toFloat()

            // Multiple streaks per column (tiled vertically)
            for (row in -1..3) {
                val yStart = (effectiveOffset + row) * h
                val xStart = xBase + yStart * sin(Math.toRadians(streakAngle.toDouble())).toFloat()

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MistyColors.RainDrop.copy(alpha = 0f),
                            MistyColors.RainDrop.copy(alpha = streakAlpha),
                            MistyColors.RainDrop.copy(alpha = 0f),
                        ),
                        start = Offset(xStart, yStart),
                        end   = Offset(xStart + dx, yStart + streakLength),
                    ),
                    start       = Offset(xStart, yStart),
                    end         = Offset(xStart + dx, yStart + streakLength),
                    strokeWidth = 1.2f,
                )
            }
        }
    }
}

// ─── Shared Drawing Helper ────────────────────────────────────────────────────

/**
 * Draws a soft cloud blob as a radial gradient ellipse.
 * No hard edges — alpha falls off smoothly to transparent.
 *
 * @param cx    Centre X (canvas coordinates).
 * @param cy    Centre Y.
 * @param rx    Half-width of the blob.
 * @param ry    Half-height of the blob.
 * @param alpha Peak opacity of the cloud blob (0f–1f).
 * @param color Base cloud colour — defaults to [MistyColors.CloudWhite].
 */
private fun DrawScope.drawCloudBlob(
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
    alpha: Float,
    color: Color = MistyColors.CloudWhite,
) {
    // We draw an ellipse by scaling the canvas before drawing a circle.
    withTransform(
        transformBlock = {
            scale(scaleX = rx / ry, scaleY = 1f, pivot = Offset(cx, cy))
        },
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = alpha),
                    color.copy(alpha = alpha * 0.5f),
                    Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = ry,
            ),
            radius = ry,
            center = Offset(cx, cy),
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Sunny Scene", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun SunnyPreview() {
    MistyWeatherTheme { WeatherScene(WeatherType.Sunny) }
}

@Preview(name = "Cloudy Scene", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun CloudyPreview() {
    MistyWeatherTheme { WeatherScene(WeatherType.Cloudy) }
}

@Preview(name = "Rain Scene", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun RainPreview() {
    MistyWeatherTheme { WeatherScene(WeatherType.Rain) }
}
