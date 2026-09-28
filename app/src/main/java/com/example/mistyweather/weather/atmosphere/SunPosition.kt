package com.example.mistyweather.weather.atmosphere

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import java.time.LocalTime
import kotlin.math.sin

// ─── Sun Position ─────────────────────────────────────────────────────────────
//
// Phase 3: Computes the sun's screen position and angular parameters
// as a smooth continuous function of time.
//
// Role: ONLY responsible for the sun's visual position and geometric parameters.
// Input: TimeOfDay + progress (or LocalTime).
// Output: x, y, elevation, glowRadius, discAlpha, visible.
//
// Continuous Trajectory (east → zenith → west):
//   Dawn(E)  ──→  Morning  ──→  Noon(zenith)  ──→  Sunset  ──→  Evening(W)
//
// Normalized Screen Coordinates:
//   x: 0.05 (east/left at dawn) ──→ 0.50 (centre at noon) ──→ 0.95 (west/right at sunset)
//   y: 0.92 (horizon at dawn)   ──→ 0.08 (high at noon)    ──→ 0.92 (horizon at sunset)
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Normalised sun position and visual rendering parameters.
 *
 * @param position    Normalised screen position (x ∈ [0,1], y ∈ [0,1]). y=0 is top.
 * @param visible     True when sun is above horizon (Dawn through Evening).
 * @param elevation   Angular elevation 0–1 (0=horizon, 1=zenith). Drives glow & rays.
 * @param glowRadius  Normalised glow radius as fraction of screen height.
 * @param discAlpha   Opacity of the sun disc — fades gracefully near horizon.
 */
data class SunPosition(
    val position: Offset,
    val visible: Boolean,
    val elevation: Float,
    val glowRadius: Float,
    val discAlpha: Float,
)

/**
 * Computes [SunPosition] from a [LocalTime] continuously.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun computeSunPosition(time: LocalTime): SunPosition {
    val phase = TimeOfDay.from(time)
    val progress = TimeOfDay.progressWithin(time)
    return computeSunPosition(phase, progress)
}

/**
 * Computes the sun's [SunPosition] for any [TimeOfDay] and intra-phase progress.
 *
 * Continuous arc: `y = 0.92 − sin(π·arcT) · 0.84`
 * The arc parameter `arcT` spans seamlessly across phases [0, 1].
 *
 * @param timeOfDay  The current [TimeOfDay] phase.
 * @param progress   Intra-phase progress [0,1] from [TimeOfDay.progressWithin].
 */
fun computeSunPosition(timeOfDay: TimeOfDay, progress: Float = 0.5f): SunPosition {
    // Map each TimeOfDay phase to contiguous intervals along the daytime arc t ∈ [0, 1].
    // t=0: eastern horizon (dawn start), t=0.5: zenith (midday), t=1.0: western horizon.
    val arcT = when (timeOfDay) {
        TimeOfDay.Dawn    -> lerp(0.00f, 0.15f, progress)
        TimeOfDay.Morning -> lerp(0.15f, 0.35f, progress)
        TimeOfDay.Noon    -> lerp(0.35f, 0.65f, progress)
        TimeOfDay.Sunset  -> lerp(0.65f, 0.85f, progress)
        TimeOfDay.Evening -> lerp(0.85f, 0.95f, progress)
        TimeOfDay.Night   -> 1.0f   // Sun below horizon
    }

    // x: linear continuous sweep east (0.05) → west (0.95)
    val x = 0.05f + arcT * 0.90f

    // y: smooth sin arc peaking at arcT=0.5 (noon at y=0.08), horizon at arcT=0,1 (y=0.92)
    val sinArc = sin(Math.PI * arcT).toFloat().coerceIn(0f, 1f)
    val y      = 0.92f - sinArc * 0.84f

    // Elevation: 0 at horizon, 1 at zenith
    val elevation = sinArc

    // Glow radius: broader near horizon (atmospheric scattering), tighter at zenith
    val glowRadius = 0.28f + (1f - elevation) * 0.25f

    // Disc alpha: full during the day, smoothly fades at dusk/dawn, 0 at night
    val discAlpha = when (timeOfDay) {
        TimeOfDay.Night   -> 0f
        TimeOfDay.Dawn    -> (progress * 2.5f).coerceAtMost(1f) * 0.85f
        TimeOfDay.Evening -> ((1f - progress) * 2.5f).coerceAtMost(1f) * 0.70f
        else              -> (0.85f + elevation * 0.15f).coerceAtMost(1f)
    }

    return SunPosition(
        position   = Offset(x, y),
        visible    = timeOfDay != TimeOfDay.Night,
        elevation  = elevation,
        glowRadius = glowRadius,
        discAlpha  = discAlpha,
    )
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
