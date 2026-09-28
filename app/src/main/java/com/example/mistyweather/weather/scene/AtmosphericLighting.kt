package com.example.mistyweather.weather.scene

import androidx.compose.ui.graphics.Color
import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import kotlin.math.exp

// ─── Atmospheric Lighting Model ───────────────────────────────────────────────
//
// Phase 4.4-A1: Pure Mathematical Atmospheric Lighting Foundation.
//
// Computes continuous solar elevation, golden hour scattering,
// soft horizon glow, and night ambient airglow.
// Zero external libraries, zero allocations inside render loop.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Pure functions governing continuous diurnal lighting and atmospheric scattering.
 */
object AtmosphericLightingModel {

    /**
     * Computes a continuous solar phase ∈ [0.0, 1.0).
     *
     * Canonical cycle:
     *   0.00 = Midnight (~00:00)
     *   0.25 = Sunrise (~06:00)
     *   0.50 = Solar Noon (~12:00)
     *   0.75 = Sunset (~18:00)
     *   1.00 = Midnight (~24:00)
     */
    fun calculateSolarPhase(timeOfDay: TimeOfDay, timeProgress: Float): Float {
        val clampedProg = timeProgress.coerceIn(0f, 1f)
        val (start, end) = when (timeOfDay) {
            TimeOfDay.Dawn    -> 0.20f to 0.28f  // 05:00 - 06:30
            TimeOfDay.Morning -> 0.28f to 0.46f  // 06:30 - 11:00
            TimeOfDay.Noon    -> 0.46f to 0.72f  // 11:00 - 17:30
            TimeOfDay.Sunset  -> 0.72f to 0.79f  // 17:30 - 19:00
            TimeOfDay.Evening -> 0.79f to 0.88f  // 19:00 - 21:00
            TimeOfDay.Night   -> {
                // Night interval: 21:00 (0.88) -> 24:00 (1.00/0.00) -> 05:00 (0.20)
                if (clampedProg < 0.375f) {
                    val p = clampedProg / 0.375f
                    return (0.88f + p * 0.12f).coerceIn(0f, 1f)
                } else {
                    val p = (clampedProg - 0.375f) / 0.625f
                    return (p * 0.20f).coerceIn(0f, 1f)
                }
            }
        }
        return (start + (end - start) * clampedProg).coerceIn(0f, 1f)
    }

    /**
     * Calculates the continuous solar elevation [-1.0, +1.0] from a normalized [solarPhase].
     *
     *   +1.0 = Zenith (Noon)
     *    0.0 = Horizon (Sunrise / Sunset)
     *   -1.0 = Nadir (Midnight)
     */
    fun calculateSolarElevation(solarPhase: Float): Float {
        return kotlin.math.sin((solarPhase - 0.25f) * 2.0 * Math.PI).toFloat().coerceIn(-1f, 1f)
    }

    /**
     * Derives solar elevation directly from normalized screen sun coordinates and daytime flag.
     * Normalized y: 0.08 at noon, 0.92 at horizon.
     */
    fun calculateElevationFromSunY(sunY: Float, isDaytime: Boolean): Float {
        if (!isDaytime) return -0.6f
        val clampedY = sunY.coerceIn(0.08f, 1.0f)
        return ((0.92f - clampedY) / 0.84f).coerceIn(-0.2f, 1f)
    }

    /**
     * Computes the continuous Golden Hour intensity ∈ [0.0, 1.0].
     *
     * Characteristics:
     *   • Peaks around solar elevation ~ +0.08 (grazing the horizon).
     *   • Approaches 0.0 at solar noon (elevation = +1.0).
     *   • Strictly 0.0 at midnight (elevation = -1.0).
     *   • Smooth continuous Gaussian decay.
     */
    fun calculateGoldenHourIntensity(solarElevation: Float): Float {
        if (solarElevation < -0.20f || solarElevation > 0.48f) return 0f
        val peak = 0.08f
        val spread = 0.18f
        val delta = (solarElevation - peak) / spread
        val gaussian = exp(-0.5f * delta * delta)
        val window = smoothstep(-0.20f, -0.02f, solarElevation) * (1f - smoothstep(0.30f, 0.48f, solarElevation))
        return (gaussian * window).coerceIn(0f, 1f)
    }

    /**
     * Computes parameters for the soft horizon atmospheric glow.
     */
    fun calculateHorizonGlow(
        solarElevation: Float,
        goldenHour: Float,
        weatherType: WeatherType = WeatherType.Sunny,
    ): HorizonGlowParams {
        val isDay = solarElevation > 0f

        val baseColor = when {
            goldenHour > 0.05f -> {
                // Warm sunset/sunrise scattering: golden amber to soft coral
                val warmGold = Color(0xFFFF9E44)
                val softPeach = Color(0xFFFFB366)
                lerp(softPeach, warmGold, goldenHour)
            }
            solarElevation > 0.35f -> {
                // High sun daytime: bright soft aerial perspective mist
                Color(0xFFE2ECFA)
            }
            solarElevation > -0.10f -> {
                // Blue hour twilight: soft dusk mauve / twilight indigo
                Color(0xFF6B5B95)
            }
            else -> {
                // Night ambient: deep starry navy glow, never #000000
                Color(0xFF162544)
            }
        }

        val rawIntensity = when {
            goldenHour > 0.05f -> 0.14f + goldenHour * 0.20f
            isDay -> 0.08f + (solarElevation.coerceIn(0f, 1f)) * 0.04f
            solarElevation > -0.10f -> 0.10f
            else -> 0.06f // Night ambient > 0
        }

        // Weather modulation (Prompt Section 12)
        val weatherFactor = when (weatherType) {
            WeatherType.Sunny -> 1.0f
            WeatherType.PartlyCloudy -> 0.90f
            WeatherType.Rain -> 0.70f
            WeatherType.Snow -> 0.85f
            WeatherType.Cloudy -> 0.50f
            WeatherType.Fog -> 0.60f
            WeatherType.Storm -> 0.35f
        }

        val finalIntensity = (rawIntensity * weatherFactor).coerceIn(0.02f, 0.36f)

        return HorizonGlowParams(
            color          = baseColor.copy(alpha = finalIntensity),
            intensity      = finalIntensity,
            startYFraction = 0.62f,
            endYFraction   = 1.00f,
        )
    }

    /**
     * Computes the night ambient airglow parameter to guarantee night is never #000000.
     */
    fun calculateNightAmbient(solarElevation: Float): NightAmbientParams {
        val nightWeight = (1f - smoothstep(-0.25f, 0.05f, solarElevation)).coerceIn(0f, 1f)
        val ambientColor = Color(0xFF0D1B2A) // Rich deep midnight navy, never pure black
        val ambientIntensity = 0.06f * nightWeight
        return NightAmbientParams(
            color  = ambientColor.copy(alpha = ambientIntensity),
            weight = nightWeight,
        )
    }

    private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun lerp(start: Color, stop: Color, fraction: Float): Color {
        val f = fraction.coerceIn(0f, 1f)
        return Color(
            red   = start.red + (stop.red - start.red) * f,
            green = start.green + (stop.green - start.green) * f,
            blue  = start.blue + (stop.blue - start.blue) * f,
            alpha = start.alpha + (stop.alpha - start.alpha) * f,
        )
    }
}

/**
 * Parameters for the Horizon Atmospheric Glow band.
 */
data class HorizonGlowParams(
    val color: Color,
    val intensity: Float,
    val startYFraction: Float,
    val endYFraction: Float,
)

/**
 * Parameters for the Night Ambient Airglow floor.
 */
data class NightAmbientParams(
    val color: Color,
    val weight: Float,
)
