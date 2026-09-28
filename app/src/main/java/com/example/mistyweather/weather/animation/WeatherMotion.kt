package com.example.mistyweather.weather.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

// ─── Weather Motion ───────────────────────────────────────────────────────────
//
// Phase 2: Continuous animation clocks for each scene layer.
// All motion is driven by normalised 0→1 floats on infinite loops.
// Layers multiply these clocks by their own speed parameters from
// WeatherEnvironment — so slowing/speeding the environment is trivial.
//
// Design principles:
//   • Each clock is a single InfiniteTransition float — zero per-frame alloc.
//   • Clocks are decoupled from environment so they survive scene transitions.
//   • One shared InfiniteTransition produces all clocks — minimal recompose.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Collection of continuous animation clocks used by the cinematic weather layers.
 *
 * All values are normalised [0f, 1f) cycling continuously.
 *
 * @param cloudDrift      Drives horizontal cloud offset (slow, 40s cycle).
 * @param cloudShape      Drives cloud blob morphing (very slow, 60s reverse).
 * @param sunPulse        Drives sun-glow radius pulse (4s reverse).
 * @param particleFall    Drives particle fall position (1.2s cycle).
 * @param fogDrift        Drives fog layer horizontal oscillation (25s reverse).
 * @param ambientBreath   Drives sky luminosity micro-breath (8s reverse).
 */
data class WeatherMotion(
    val cloudDrift: Float,
    val cloudShape: Float,
    val sunPulse: Float,
    val particleFall: Float,
    val fogDrift: Float,
    val ambientBreath: Float,
)

/**
 * Remembers a [WeatherMotion] with all continuous scene clocks.
 *
 * Call once at the top of [CinematicWeatherScene] and pass values down
 * to each sub-layer. Uses a single [rememberInfiniteTransition] shared
 * across all clocks.
 */
@Composable
fun rememberWeatherMotion(): WeatherMotion {
    val transition = rememberInfiniteTransition(label = "weather_motion")

    // ── Cloud drift: 0→1 in 40 seconds, loops ──────────────────────────────
    val cloudDrift by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 40_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cloud_drift",
    )

    // ── Cloud shape morphing: 0→1→0 in 60s ─────────────────────────────────
    val cloudShape by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cloud_shape",
    )

    // ── Sun pulse: 0→1→0 in 4 seconds ──────────────────────────────────────
    val sunPulse by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 4_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sun_pulse",
    )

    // ── Particle fall: 0→1 in 1.2s ─────────────────────────────────────────
    val particleFall by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "particle_fall",
    )

    // ── Fog drift: 0→1→0 in 25s ────────────────────────────────────────────
    val fogDrift by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 25_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fog_drift",
    )

    // ── Ambient breath: 0→1→0 in 8s ────────────────────────────────────────
    val ambientBreath by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 8_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ambient_breath",
    )

    return WeatherMotion(
        cloudDrift    = cloudDrift,
        cloudShape    = cloudShape,
        sunPulse      = sunPulse,
        particleFall  = particleFall,
        fogDrift      = fogDrift,
        ambientBreath = ambientBreath,
    )
}
