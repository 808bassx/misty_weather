package com.example.mistyweather.weather.scene

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.mistyweather.weather.animation.InterpolatedParticles
import com.example.mistyweather.weather.model.ParticleEnvironment
import com.example.mistyweather.weather.model.ParticleKind
import kotlin.math.sin
import kotlin.math.cos

// ─── Particle Layer ───────────────────────────────────────────────────────────
//
// Phase 2 Layer 3: Rain streaks, snow flakes, and fog wisps.
//
// Handles:
//   Rain — diagonal streaks with per-column phase offset and random length.
//   Snow — floating circles with horizontal drift and slow fall.
//   Fog  — large soft radial blobs drifting horizontally.
//
// During a WeatherTransition, two particle systems may be active simultaneously.
// The transition cross-fades alpha between them so particles blend cinematically.
//
// Performance:
//   • Fixed iteration count per frame (cols × rows).
//   • All values computed from clocks — no random calls per frame.
//   • Fog uses drawCircle (single call per blob), not per-particle.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders the particle effects layer (rain / snow / fog).
 *
 * @param particles    [InterpolatedParticles] from [rememberWeatherTransition].
 *                     May have both [from] and [to] systems for cross-fade.
 * @param fallClock    Normalised 0→1 fall position from [WeatherMotion.particleFall].
 * @param fogDrift     Normalised 0→1 fog horizontal drift from [WeatherMotion.fogDrift].
 * @param modifier     Should fill the parent.
 */
@Composable
fun ParticleLayer(
    particles: InterpolatedParticles?,
    fallClock: Float,
    fogDrift: Float,
    modifier: Modifier = Modifier,
) {
    if (particles == null) return
    if (particles.from == null && particles.to == null) return

    Canvas(modifier = modifier) {
        val progress = particles.progress

        // Draw "from" system fading out
        particles.from?.let { from ->
            val fromAlpha = (1f - progress).coerceIn(0f, 1f)
            if (fromAlpha > 0.01f) {
                drawParticles(from, fromAlpha, fallClock, fogDrift)
            }
        }

        // Draw "to" system fading in
        particles.to?.let { to ->
            val toAlpha = progress.coerceIn(0f, 1f)
            if (toAlpha > 0.01f) {
                drawParticles(to, toAlpha, fallClock, fogDrift)
            }
        }
    }
}

// ─── Dispatch ────────────────────────────────────────────────────────────────

private fun DrawScope.drawParticles(
    env: ParticleEnvironment,
    globalAlpha: Float,
    fallClock: Float,
    fogDrift: Float,
) {
    when (env.kind) {
        ParticleKind.Rain -> drawRain(env, globalAlpha, fallClock)
        ParticleKind.Snow -> drawSnow(env, globalAlpha, fallClock)
        ParticleKind.Fog  -> drawFog(env, globalAlpha, fogDrift)
    }
}

// ─── Rain ─────────────────────────────────────────────────────────────────────

private fun DrawScope.drawRain(env: ParticleEnvironment, alpha: Float, fallClock: Float) {
    val w = size.width
    val h = size.height

    val cols      = (18 * env.density).toInt().coerceAtLeast(6)
    val colSpacing = w / cols
    val angleRad   = Math.toRadians(env.angle.toDouble())
    val sin        = sin(angleRad).toFloat()

    for (col in 0 until cols) {
        val xBase      = col * colSpacing + colSpacing * 0.5f
        val phaseShift = (col * 0.17f + col * 0.031f) % 1f
        val effectiveT = (fallClock + phaseShift) % 1f

        val streakLength = h * (0.035f + (col % 3) * 0.010f) * env.size
        val streakAlpha  = alpha * env.color.alpha * (0.55f + (col % 5) * 0.09f)
        val dx           = streakLength * sin

        for (row in -1..3) {
            val yStart  = (effectiveT + row) * h
            val xStart  = xBase + yStart * sin * 0.12f

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        env.color.copy(alpha = 0f),
                        env.color.copy(alpha = streakAlpha),
                        env.color.copy(alpha = 0f),
                    ),
                    start = Offset(xStart, yStart),
                    end   = Offset(xStart + dx, yStart + streakLength),
                ),
                start       = Offset(xStart, yStart),
                end         = Offset(xStart + dx, yStart + streakLength),
                strokeWidth = env.size * alpha,
                cap         = StrokeCap.Round,
            )
        }
    }
}

// ─── Snow ─────────────────────────────────────────────────────────────────────

private fun DrawScope.drawSnow(env: ParticleEnvironment, alpha: Float, fallClock: Float) {
    val w = size.width
    val h = size.height

    val cols      = (12 * env.density).toInt().coerceAtLeast(4)
    val rows      = 4
    val colSpacing = w / cols

    for (col in 0 until cols) {
        for (row in 0 until rows) {
            val seed        = col * 7 + row * 13
            val phaseShift  = (seed * 0.13f) % 1f
            val effectiveT  = (fallClock * env.speed + phaseShift) % 1f

            val x = colSpacing * col + colSpacing * 0.5f +
                    sin(Math.PI * 2.0 * effectiveT + col * 1.1).toFloat() * colSpacing * 0.4f
            val y = (effectiveT + row.toFloat() / rows.toFloat()) % 1f * h

            val flakeRadius = env.size * (0.7f + (seed % 5) * 0.12f)
            val flakeAlpha  = alpha * env.color.alpha * (0.5f + (seed % 4) * 0.12f)

            drawCircle(
                color  = env.color.copy(alpha = flakeAlpha),
                radius = flakeRadius,
                center = Offset(x, y),
            )
        }
    }
}

// ─── Fog ─────────────────────────────────────────────────────────────────────

private fun DrawScope.drawFog(env: ParticleEnvironment, alpha: Float, fogDrift: Float) {
    val w = size.width
    val h = size.height

    // Fog rendered as 6 large overlapping soft blobs with slow drift
    val fogBlobs = listOf(
        Triple(0.10f, 0.30f, 0.60f),
        Triple(0.50f, 0.50f, 0.75f),
        Triple(0.85f, 0.35f, 0.55f),
        Triple(0.25f, 0.65f, 0.70f),
        Triple(0.70f, 0.70f, 0.65f),
        Triple(0.40f, 0.20f, 0.50f),
    )

    for ((cx, cy, radius) in fogBlobs) {
        val driftedX = w * cx + (fogDrift - 0.5f) * w * 0.15f
        val blobAlpha = alpha * env.color.alpha

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to env.color.copy(alpha = blobAlpha),
                    0.6f to env.color.copy(alpha = blobAlpha * 0.4f),
                    1.0f to Color.Transparent,
                ),
                center = Offset(driftedX, h * cy),
                radius = w * radius,
            ),
            radius = w * radius,
            center = Offset(driftedX, h * cy),
        )
    }
}
