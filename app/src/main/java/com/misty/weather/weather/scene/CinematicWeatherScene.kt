package com.misty.weather.weather.scene

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.misty.weather.core.weather.WeatherType
import com.misty.weather.theme.MistyWeatherTheme
import com.misty.weather.weather.animation.rememberAtmosphereTransition
import com.misty.weather.weather.animation.rememberWeatherMotion
import com.misty.weather.weather.atmosphere.TimeOfDay
import com.misty.weather.weather.domain.WeatherMapper
import com.misty.weather.weather.domain.WeatherRenderState
import java.time.LocalTime

// ─── Cinematic Weather Scene ─────────────────────────────────────────────────
//
// Phase 2 & 3: Full-screen animated weather environment composited with time.
//
// Layer Stack (bottom → top):
//   0. SkyLayer      — Vertical gradient sky (zenith → horizon) + star field
//   1. LightLayer    — Sun/moon corona, astronomical arc, and ambient tint
//   2. CloudLayer    — Three-tier parallax clouds with time-adjusted shadow & warmth
//   3. ParticleLayer — Rain / snow / fog with diurnal contrast adaptation
//
// Visual Pipeline:
//   Current Time ──→ TimeOfDay ──→ AmbientLighting ──→ Weather Environment
//                                                              ↓
//                                                     CinematicWeatherScene
//                                                              ↓
//                                                           Glass UI
//
// Design & Performance:
//   • Pure Canvas rendering — NO nested BackdropFilters.
//   • Single InfiniteTransition for animation clocks.
//   • Spring transition lerps all layer parameters across both weather & time changes.
//   • Scene composable ONLY handles rendering — zero business logic computation.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-screen cinematic weather background driven by [weatherType] and [timeOfDay].
 *
 * @param weatherType  The current weather condition.
 * @param timeOfDay    The temporal diurnal phase (Dawn, Morning, Noon, Sunset, Evening, Night).
 *                     Defaults to [TimeOfDay.Noon].
 * @param timeProgress Progress within the time phase [0, 1]. Defaults to 0.5.
 * @param modifier     Should be [Modifier.fillMaxSize()].
 */
/**
 * Primary frozen rendering entrypoint: receives an immutable [WeatherRenderState].
 * All data transformations happen prior to entering the rendering pipeline.
 *
 * @param state     Frozen render state containing weather condition, atmosphere, and time.
 * @param modifier  Should be [Modifier.fillMaxSize()].
 */
@Composable
fun CinematicWeatherScene(
    state: WeatherRenderState,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    // ── Atmosphere: spring-interpolated state across weather & diurnal time ────
    val env by rememberAtmosphereTransition(
        weatherType  = state.weatherType,
        timeOfDay    = state.timeOfDay,
        timeProgress = state.timeProgress,
    )

    // ── Motion Clocks: single shared infinite transition ──────────────────────
    val motion = rememberWeatherMotion()

    // ── Layer Stack ───────────────────────────────────────────────────────────
    Box(modifier = modifier) {

        // Layer 0: Sky gradient + stars
        SkyLayer(
            sky         = env.sky,
            breathAlpha = motion.ambientBreath,
            starOpacity = env.starOpacity,
            modifier    = Modifier.fillMaxSize(),
        )

        // Layer 1: Sun / moon arc & atmospheric light
        LightLayer(
            light       = env.light,
            sunPulse    = motion.sunPulse,
            weatherType = env.weatherType,
            modifier    = Modifier.fillMaxSize(),
        )

        // Layer 2: Parallax clouds with time-tinted depth
        CloudLayer(
            clouds     = env.clouds,
            cloudDrift = motion.cloudDrift,
            cloudShape = motion.cloudShape,
            modifier   = Modifier.fillMaxSize(),
        )

        // Layer 3: Rain, snow, or fog particles
        ParticleLayer(
            particles = env.particles,
            fallClock = motion.particleFall,
            fogDrift  = motion.fogDrift,
            modifier  = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Convenience overload driving [CinematicWeatherScene] from [weatherType] and [timeOfDay].
 * Internally delegates to the frozen [WeatherRenderState] entrypoint via [WeatherMapper].
 */
@Composable
fun CinematicWeatherScene(
    weatherType: WeatherType,
    timeOfDay: TimeOfDay = TimeOfDay.Noon,
    timeProgress: Float = 0.5f,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    val renderState = remember(weatherType, timeOfDay, timeProgress) {
        WeatherMapper.toRenderState(weatherType, timeOfDay, timeProgress)
    }
    CinematicWeatherScene(state = renderState, modifier = modifier)
}

/**
 * Convenience overload driving [CinematicWeatherScene] from a [LocalTime].
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CinematicWeatherScene(
    weatherType: WeatherType,
    time: LocalTime,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    val phase = TimeOfDay.from(time)
    val progress = TimeOfDay.progressWithin(time)
    CinematicWeatherScene(
        weatherType  = weatherType,
        timeOfDay    = phase,
        timeProgress = progress,
        modifier     = modifier,
    )
}

// ─── Phase 3 Previews ─────────────────────────────────────────────────────────

@Preview(name = "Sunny Morning", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewSunnyMorning() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Sunny, TimeOfDay.Morning) }
}

@Preview(name = "Sunny Noon", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewSunnyNoon() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Sunny, TimeOfDay.Noon) }
}

@Preview(name = "Sunny Sunset", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewSunnySunset() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Sunny, TimeOfDay.Sunset) }
}

@Preview(name = "Sunny Night", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewSunnyNight() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Sunny, TimeOfDay.Night) }
}

@Preview(name = "Rain Morning", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewRainMorning() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Rain, TimeOfDay.Morning) }
}

@Preview(name = "Rain Evening", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewRainEvening() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Rain, TimeOfDay.Evening) }
}

@Preview(name = "Rain Night", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewRainNight() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Rain, TimeOfDay.Night) }
}

@Preview(name = "Snow Night", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewSnowNight() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Snow, TimeOfDay.Night) }
}

@Preview(name = "Storm Night", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewStormNight() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Storm, TimeOfDay.Night) }
}
