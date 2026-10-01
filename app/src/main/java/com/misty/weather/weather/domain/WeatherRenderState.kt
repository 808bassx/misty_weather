package com.misty.weather.weather.domain

import com.misty.weather.core.weather.WeatherType
import com.misty.weather.weather.atmosphere.AmbientAtmosphere
import com.misty.weather.weather.atmosphere.TimeOfDay

// ─── Weather Render State ─────────────────────────────────────────────────────
//
// Phase 3.6: Frozen rendering contract for CinematicWeatherScene.
//
// All data transformations, DTO mappings, and time calculations terminate
// in this immutable contract. The rendering engine consumes ONLY this struct.
//
// Pipeline:
//   Repository → Domain WeatherState → WeatherMapper → WeatherRenderState → CinematicWeatherScene
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Frozen rendering state consumed by [CinematicWeatherScene].
 *
 * @param weatherType   The underlying weather condition.
 * @param atmosphere    The fully blended atmospheric parameters (sky, sun, clouds, particles, glass).
 * @param timeOfDay     Diurnal temporal phase.
 * @param timeProgress  Continuous progress within the diurnal phase [0, 1].
 */
data class WeatherRenderState(
    val weatherType: WeatherType,
    val atmosphere: AmbientAtmosphere,
    val timeOfDay: TimeOfDay,
    val timeProgress: Float = 0.5f,
)
