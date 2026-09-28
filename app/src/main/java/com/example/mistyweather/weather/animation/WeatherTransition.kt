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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.mistyweather.weather.model.CloudEnvironment
import com.example.mistyweather.weather.model.LightEnvironment
import com.example.mistyweather.weather.model.ParticleEnvironment
import com.example.mistyweather.weather.model.SkyEnvironment
import com.example.mistyweather.weather.model.WeatherEnvironment
import com.example.mistyweather.weather.model.WeatherEnvironments
import com.example.mistyweather.core.weather.WeatherType

// ─── Weather Transition ───────────────────────────────────────────────────────
//
// Phase 2: Animates between WeatherEnvironments using a single float progress
// value (0f = from, 1f = to). All layer colours and numeric parameters lerp
// smoothly, so switching weather types feels cinematic instead of a cut.
//
// Design:
//   • One spring animation drives progress 0→1 when weatherType changes.
//   • All visual values are derived by lerping from→to at current progress.
//   • On subsequent changes the lerp snaps from current state (not origin),
//     so rapid changes never look "reset".
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * An interpolated snapshot of two [WeatherEnvironment]s at a given [progress].
 * All fields are pre-computed lerp results, ready to draw with no allocation.
 */
data class InterpolatedEnvironment(
    val sky: SkyEnvironment,
    val clouds: CloudEnvironment,
    val light: LightEnvironment,
    val particles: InterpolatedParticles?,
    val glassAmbient: Color,
)

/**
 * Particle parameters that are valid during a transition.
 * We carry both [from] and [to] so the particle layer can cross-fade alpha.
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

private fun interpolate(
    from: WeatherEnvironment,
    to: WeatherEnvironment,
    progress: Float,
): InterpolatedEnvironment = InterpolatedEnvironment(
    sky          = lerp(from.sky, to.sky, progress),
    clouds       = lerp(from.clouds, to.clouds, progress),
    light        = lerp(from.light, to.light, progress),
    particles    = InterpolatedParticles(from.particles, to.particles, progress),
    glassAmbient = lerp(from.glassAmbient, to.glassAmbient, progress),
)

// ─── Composable Hook ─────────────────────────────────────────────────────────

/**
 * Returns a [State] holding the current [InterpolatedEnvironment] for [weatherType].
 *
 * When [weatherType] changes, the returned state animates from the previously
 * rendered environment to the new one using a gentle spring.
 *
 * Usage:
 * ```kotlin
 * val env by rememberWeatherTransition(weatherType)
 * SkyLayer(env.sky)
 * CloudLayer(env.clouds)
 * LightLayer(env.light)
 * ParticleLayer(env.particles)
 * ```
 */
@Composable
fun rememberWeatherTransition(weatherType: WeatherType): State<InterpolatedEnvironment> {
    val targetEnv = WeatherEnvironments.forType(weatherType)

    // Track the "from" environment — updated every time a new transition begins.
    var fromEnv by remember { mutableStateOf(targetEnv) }
    var toEnv   by remember { mutableStateOf(targetEnv) }

    // The interpolated state we expose.
    val interpolated = remember {
        mutableStateOf(
            interpolate(targetEnv, targetEnv, 1f)
        )
    }

    LaunchedEffect(weatherType) {
        // Capture current interpolated state as the new "from" to avoid jumps.
        fromEnv = interpolated.value.let { cur ->
            WeatherEnvironment(
                weatherType  = fromEnv.weatherType,
                sky          = cur.sky,
                clouds       = cur.clouds,
                light        = cur.light,
                particles    = cur.particles?.run {
                    // Materialise a blended particle env from current transition state
                    val fP = from; val tP = to; val p = progress
                    when {
                        fP == null && tP == null -> null
                        fP == null -> tP?.copy(density = tP.density * p)
                        tP == null -> fP.copy(density = fP.density * (1f - p))
                        else -> fP.copy(
                            density = lerp(fP.density, tP.density, p),
                            speed   = lerp(fP.speed,   tP.speed,   p),
                            angle   = lerp(fP.angle,   tP.angle,   p),
                        )
                    }
                },
                glassAmbient = cur.glassAmbient,
            )
        }
        toEnv = targetEnv

        // Animate progress 0 → 1
        val animState = AnimationState(0f)
        animState.animateTo(
            targetValue    = 1f,
            animationSpec  = spring(
                stiffness    = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioNoBouncy,
            ),
        ) {
            interpolated.value = interpolate(fromEnv, toEnv, value)
        }
    }

    return interpolated
}
