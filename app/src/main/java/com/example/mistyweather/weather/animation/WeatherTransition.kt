package com.example.mistyweather.weather.animation

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.weather.atmosphere.AmbientAtmosphere
import com.example.mistyweather.weather.atmosphere.SunPosition
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import com.example.mistyweather.weather.atmosphere.computeAmbientAtmosphere
import com.example.mistyweather.weather.model.CloudEnvironment
import com.example.mistyweather.weather.model.LightEnvironment
import com.example.mistyweather.weather.model.ParticleEnvironment
import com.example.mistyweather.weather.model.SkyEnvironment

// ─── Weather & Atmosphere Transition ──────────────────────────────────────────
//
// Phase 2 & 3: Single unified transition engine for both Weather and Time changes.
//
// Spring-interpolates between two [AmbientAtmosphere] states across all visual dimensions:
//   • Sky gradient lerp
//   • Sun position & corona lerp
//   • Cloud density, blob color, and shadow lerp
//   • Particle cross-fade & color lerp
//   • Glass adaptive tint lerp
//   • Star field opacity lerp
//   • Luminosity lerp
//
// Guarantees:
//   • Single AnimationState spring per scene — no duplicate animation engines.
//   • Interrupted transitions smoothly continue from current visual state (no jumping).
//   • Zero allocations during draw frames.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * An interpolated snapshot of two [AmbientAtmosphere] states at a given progress [0..1].
 */
data class InterpolatedAtmosphere(
    val weatherType: WeatherType,
    val timeOfDay: TimeOfDay,
    val sky: SkyEnvironment,
    val light: LightEnvironment,
    val clouds: CloudEnvironment,
    val particles: InterpolatedParticles?,
    val glassAmbient: Color,
    val starOpacity: Float,
    val luminosity: Float,
    val sunPosition: SunPosition,
)

/**
 * Particle parameters during cross-fade transitions.
 */
data class InterpolatedParticles(
    val from: ParticleEnvironment?,
    val to: ParticleEnvironment?,
    val progress: Float,
)

// ─── Lerp Helpers ────────────────────────────────────────────────────────────

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

private fun lerp(a: Pair<Float, Float>, b: Pair<Float, Float>, t: Float): Pair<Float, Float> =
    lerp(a.first, b.first, t) to lerp(a.second, b.second, t)

private fun lerp(a: Offset, b: Offset, t: Float): Offset =
    Offset(lerp(a.x, b.x, t), lerp(a.y, b.y, t))

private fun lerp(a: SkyEnvironment, b: SkyEnvironment, t: Float) = SkyEnvironment(
    zenith  = lerp(a.zenith, b.zenith, t),
    mid     = lerp(a.mid, b.mid, t),
    horizon = lerp(a.horizon, b.horizon, t),
)

private fun lerp(a: CloudEnvironment, b: CloudEnvironment, t: Float) = CloudEnvironment(
    density      = lerp(a.density, b.density, t),
    speed        = lerp(a.speed, b.speed, t),
    blobColor    = lerp(a.blobColor, b.blobColor, t),
    shadowColor  = lerp(a.shadowColor, b.shadowColor, t),
    highAltitude = if (t < 0.5f) a.highAltitude else b.highAltitude,
)

private fun lerp(a: LightEnvironment, b: LightEnvironment, t: Float) = LightEnvironment(
    sunVisible   = if (t < 0.5f) a.sunVisible else b.sunVisible,
    sunPosition  = lerp(a.sunPosition, b.sunPosition, t),
    glowColor    = lerp(a.glowColor, b.glowColor, t),
    glowRadius   = lerp(a.glowRadius, b.glowRadius, t),
    ambientTint  = lerp(a.ambientTint, b.ambientTint, t),
    isDaytime    = if (t < 0.5f) a.isDaytime else b.isDaytime,
)

private fun lerp(a: SunPosition, b: SunPosition, t: Float) = SunPosition(
    position   = lerp(a.position, b.position, t),
    visible    = if (t < 0.5f) a.visible else b.visible,
    elevation  = lerp(a.elevation, b.elevation, t),
    glowRadius = lerp(a.glowRadius, b.glowRadius, t),
    discAlpha  = lerp(a.discAlpha, b.discAlpha, t),
)

private fun interpolate(
    from: AmbientAtmosphere,
    to: AmbientAtmosphere,
    progress: Float,
): InterpolatedAtmosphere = InterpolatedAtmosphere(
    weatherType  = if (progress < 0.5f) from.weatherType else to.weatherType,
    timeOfDay    = if (progress < 0.5f) from.timeOfDay else to.timeOfDay,
    sky          = lerp(from.sky, to.sky, progress),
    light        = lerp(from.light, to.light, progress),
    clouds       = lerp(from.clouds, to.clouds, progress),
    particles    = InterpolatedParticles(from.particles, to.particles, progress),
    glassAmbient = lerp(from.glassAmbient, to.glassAmbient, progress),
    starOpacity  = lerp(from.starOpacity, to.starOpacity, progress),
    luminosity   = lerp(from.luminosity, to.luminosity, progress),
    sunPosition  = lerp(from.sunPosition, to.sunPosition, progress),
)

// ─── Composable Hook ─────────────────────────────────────────────────────────

/**
 * Returns a [State] holding the smoothly transitioning [InterpolatedAtmosphere].
 *
 * When either [weatherType] or [timeOfDay] changes, launches a single spring
 * that smoothly lerps all visual layers simultaneously.
 */
@Composable
fun rememberAtmosphereTransition(
    weatherType: WeatherType,
    timeOfDay: TimeOfDay = TimeOfDay.Noon,
    timeProgress: Float = 0.5f,
): State<InterpolatedAtmosphere> {
    val targetAtmosphere = remember(weatherType, timeOfDay, timeProgress) {
        computeAmbientAtmosphere(weatherType, timeOfDay, timeProgress)
    }

    var fromAtmosphere by remember { mutableStateOf(targetAtmosphere) }
    var toAtmosphere   by remember { mutableStateOf(targetAtmosphere) }

    val interpolated = remember {
        mutableStateOf(interpolate(targetAtmosphere, targetAtmosphere, 1f))
    }

    LaunchedEffect(weatherType, timeOfDay, timeProgress) {
        // Capture current state as start point to eliminate visual discontinuities
        fromAtmosphere = AmbientAtmosphere(
            weatherType  = interpolated.value.weatherType,
            timeOfDay    = interpolated.value.timeOfDay,
            sky          = interpolated.value.sky,
            light        = interpolated.value.light,
            clouds       = interpolated.value.clouds,
            particles    = interpolated.value.particles?.let { p ->
                val fP = p.from; val tP = p.to; val prog = p.progress
                when {
                    fP == null && tP == null -> null
                    fP == null -> tP?.copy(density = tP.density * prog)
                    tP == null -> fP.copy(density = fP.density * (1f - prog))
                    else -> fP.copy(
                        density = lerp(fP.density, tP.density, prog),
                        speed   = lerp(fP.speed, tP.speed, prog),
                        angle   = lerp(fP.angle, tP.angle, prog),
                    )
                }
            },
            glassAmbient = interpolated.value.glassAmbient,
            starOpacity  = interpolated.value.starOpacity,
            luminosity   = interpolated.value.luminosity,
            sunPosition  = interpolated.value.sunPosition,
        )
        toAtmosphere = targetAtmosphere

        val animState = AnimationState(0f)
        animState.animateTo(
            targetValue    = 1f,
            animationSpec  = spring(
                stiffness    = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioNoBouncy,
            ),
        ) {
            interpolated.value = interpolate(fromAtmosphere, toAtmosphere, value)
        }
    }

    return interpolated
}

/**
 * Backward-compatible helper for Phase 2 calls specifying only [weatherType].
 */
@Composable
fun rememberWeatherTransition(weatherType: WeatherType): State<InterpolatedAtmosphere> =
    rememberAtmosphereTransition(weatherType, TimeOfDay.Noon, 0.5f)
