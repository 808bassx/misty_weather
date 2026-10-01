package com.misty.weather.weather.atmosphere

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.misty.weather.core.weather.WeatherType
import com.misty.weather.weather.model.CloudEnvironment
import com.misty.weather.weather.model.LightEnvironment
import com.misty.weather.weather.model.ParticleEnvironment
import com.misty.weather.weather.model.SkyEnvironment
import com.misty.weather.weather.model.WeatherEnvironment
import com.misty.weather.weather.model.WeatherEnvironments
import java.time.LocalTime

// ─── Ambient Lighting ─────────────────────────────────────────────────────────
//
// Phase 3 Core: The Ambient Lighting compositor composites WeatherEnvironment
// with TimeOfDay, ColorTemperature, and SunPosition into the final atmospheric state.
//
// Composition Pipeline:
//
//   Base Weather Environment (Sunny / Cloudy / Rain / Snow / Storm / Fog)
//                 +
//   Diurnal Color Temperature (Dawn / Morning / Noon / Sunset / Evening / Night)
//                 +
//   Continuous Sun Position Arc
//                 ↓
//   AmbientAtmosphere (Final composited parameters for Sky, Light, Cloud, Particle, Glass)
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The fully composited atmospheric state for rendering a weather scene at a specific time.
 *
 * All scene layers (SkyLayer, LightLayer, CloudLayer, ParticleLayer, GlassSurface)
 * consume these values directly.
 *
 * @param weatherType   The underlying weather condition.
 * @param timeOfDay     The temporal diurnal phase.
 * @param sky           Final composited sky gradient.
 * @param light         Final light parameters (sun/moon position, corona glow, ambient tint).
 * @param clouds        Atmospheric clouds (tinted blob/shadow colours based on time).
 * @param particles     Weather particles (contrast & opacity adjusted for daytime/nighttime).
 * @param glassAmbient  Adaptive tint pushed into [GlassSurface] components.
 * @param starOpacity   Star field visibility (visible at dusk/night, attenuated by clouds).
 * @param luminosity    Overall scene illumination factor (0.5–1.0).
 * @param sunPosition   Continuous astronomical position of the sun.
 */
data class AmbientAtmosphere(
    val weatherType: WeatherType,
    val timeOfDay: TimeOfDay,
    val sky: SkyEnvironment,
    val light: LightEnvironment,
    val clouds: CloudEnvironment,
    val particles: ParticleEnvironment?,
    val glassAmbient: Color,
    val starOpacity: Float,
    val luminosity: Float,
    val sunPosition: SunPosition,
)

/**
 * Computes [AmbientAtmosphere] using a wall-clock [LocalTime].
 */
@RequiresApi(Build.VERSION_CODES.O)
fun computeAmbientAtmosphere(
    weatherType: WeatherType,
    time: LocalTime,
): AmbientAtmosphere {
    val phase = TimeOfDay.from(time)
    val progress = TimeOfDay.progressWithin(time)
    return computeAmbientAtmosphere(weatherType, phase, progress)
}

/**
 * Computes the final [AmbientAtmosphere] by compositing a [WeatherEnvironment]
 * with [ColorTemperature] and [SunPosition].
 *
 * @param weatherType  The active [WeatherType].
 * @param timeOfDay    The diurnal [TimeOfDay] phase.
 * @param timeProgress Continuous progress within the phase [0, 1]. Defaults to 0.5 (midpoint).
 */
fun computeAmbientAtmosphere(
    weatherType: WeatherType,
    timeOfDay: TimeOfDay = TimeOfDay.Noon,
    timeProgress: Float = 0.5f,
): AmbientAtmosphere {
    val weatherEnv = WeatherEnvironments.forType(weatherType)
    val colorTemp  = blendedColorTemperature(timeOfDay, timeProgress)
    val sunPos     = computeSunPosition(timeOfDay, timeProgress)

    // ── 1. Sky Blending ──────────────────────────────────────────────────────
    val tintStrength = when (timeOfDay) {
        TimeOfDay.Noon   -> 0.00f   // Pure weather palette
        TimeOfDay.Morning -> 0.15f
        TimeOfDay.Sunset -> 0.45f  // Rich golden hour wash
        TimeOfDay.Dawn   -> 0.40f  // Peach horizon wash
        TimeOfDay.Evening -> 0.38f  // Violet dusk wash
        TimeOfDay.Night  -> 0.55f  // Deep navy overlay
    }

    val finalSky = SkyEnvironment(
        zenith  = tintAndDarken(weatherEnv.sky.zenith, colorTemp.skyTint, tintStrength, colorTemp.luminosity),
        mid     = tintAndDarken(weatherEnv.sky.mid, colorTemp.skyTint, tintStrength * 0.8f, colorTemp.luminosity),
        horizon = tintAndDarken(weatherEnv.sky.horizon, colorTemp.skyTint, tintStrength * 0.5f, colorTemp.luminosity),
    )

    // ── 2. Light Blending ────────────────────────────────────────────────────
    val weatherAllowsSun = weatherEnv.light.sunVisible
    val timeAllowsSun    = sunPos.visible

    val finalLight = LightEnvironment(
        sunVisible   = weatherAllowsSun && timeAllowsSun,
        sunPosition  = sunPos.position.x to sunPos.position.y,
        glowColor    = lerp(
            weatherEnv.light.glowColor,
            colorTemp.lightColor,
            if (weatherAllowsSun) 0.65f else 0.25f,
        ),
        glowRadius   = sunPos.glowRadius * colorTemp.luminosity,
        ambientTint  = lerp(
            weatherEnv.light.ambientTint,
            colorTemp.skyTint.copy(alpha = colorTemp.skyTint.alpha * 0.6f),
            0.50f,
        ),
        isDaytime    = timeOfDay != TimeOfDay.Night && timeOfDay != TimeOfDay.Evening,
    )

    // ── 3. Cloud Tinting & Shadow Depth ──────────────────────────────────────
    // Clouds absorb sunset warmth, dawn gold, and night darkness
    val finalClouds = when (timeOfDay) {
        TimeOfDay.Sunset -> weatherEnv.clouds.copy(
            blobColor   = lerp(weatherEnv.clouds.blobColor, Color(0xFFFFF0E2), 0.35f),
            shadowColor = lerp(weatherEnv.clouds.shadowColor, colorTemp.shadowColor, 0.45f),
        )
        TimeOfDay.Dawn -> weatherEnv.clouds.copy(
            blobColor   = lerp(weatherEnv.clouds.blobColor, Color(0xFFFDECE0), 0.30f),
            shadowColor = lerp(weatherEnv.clouds.shadowColor, colorTemp.shadowColor, 0.35f),
        )
        TimeOfDay.Evening -> weatherEnv.clouds.copy(
            blobColor   = lerp(weatherEnv.clouds.blobColor, Color(0xFF887898), 0.35f),
            shadowColor = lerp(weatherEnv.clouds.shadowColor, colorTemp.shadowColor, 0.50f),
        )
        TimeOfDay.Night -> weatherEnv.clouds.copy(
            blobColor   = lerp(weatherEnv.clouds.blobColor, Color(0xFF38465C), 0.55f),
            shadowColor = lerp(weatherEnv.clouds.shadowColor, colorTemp.shadowColor, 0.60f),
        )
        else -> weatherEnv.clouds
    }

    // ── 4. Particle Contrast & Day/Night Adjustment ──────────────────────────
    // Night particles are dimmer and cooler; sunset particles pick up amber rim
    val finalParticles = weatherEnv.particles?.let { p ->
        when (timeOfDay) {
            TimeOfDay.Night -> p.copy(
                color = lerp(p.color, Color(0x356885A0), 0.45f),
            )
            TimeOfDay.Sunset -> p.copy(
                color = lerp(p.color, Color(0x50D0A888), 0.30f),
            )
            TimeOfDay.Evening -> p.copy(
                color = lerp(p.color, Color(0x409088A8), 0.35f),
            )
            else -> p
        }
    }

    // ── 5. Glass Adaptive Tint ───────────────────────────────────────────────
    // Glass surface picks up environmental ambient reflection
    val finalGlass = lerp(weatherEnv.glassAmbient, colorTemp.glassAmbient, 0.50f)

    // ── 6. Star Opacity with Cloud Attenuation ───────────────────────────────
    // Dense clouds obscure the star field
    val cloudAttenuation = (1f - weatherEnv.clouds.density).coerceIn(0f, 1f)
    val finalStars = colorTemp.starOpacity * cloudAttenuation

    return AmbientAtmosphere(
        weatherType  = weatherType,
        timeOfDay    = timeOfDay,
        sky          = finalSky,
        light        = finalLight,
        clouds       = finalClouds,
        particles    = finalParticles,
        glassAmbient = finalGlass,
        starOpacity  = finalStars,
        luminosity   = colorTemp.luminosity,
        sunPosition  = sunPos,
    )
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

/**
 * Returns a [ColorTemperature] blended smoothly toward the adjacent phase.
 */
private fun blendedColorTemperature(timeOfDay: TimeOfDay, progress: Float): ColorTemperature {
    val current = ColorTemperatures.forTimeOfDay(timeOfDay)
    val next    = ColorTemperatures.forTimeOfDay(with(TimeOfDay.Companion) { timeOfDay.next() })
    // Blend in the next phase only during the last 30% of the current phase
    val blendT  = ((progress - 0.70f) / 0.30f).coerceIn(0f, 1f)
    return if (blendT <= 0f) current
    else ColorTemperatures.lerp(current, next, blendT)
}

/**
 * Tints a base color towards an overlay tint and scales its brightness by luminosity.
 */
private fun tintAndDarken(base: Color, tint: Color, strength: Float, luminosity: Float): Color {
    val tinted = if (strength <= 0f) base
    else {
        val effectiveT = (tint.alpha * strength).coerceIn(0f, 1f)
        if (effectiveT <= 0f) base
        else lerp(base, tint.copy(alpha = 1f), effectiveT)
    }

    return if (luminosity >= 0.99f) tinted
    else {
        // Gently pull towards midnight navy as luminosity drops
        val nightAnchor = Color(0xFF0E182A)
        lerp(nightAnchor, tinted, luminosity)
    }
}
