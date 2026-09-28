package com.example.mistyweather.weather.domain

import com.example.mistyweather.core.weather.ForecastSlot
import com.example.mistyweather.core.weather.WeatherPreview
import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import com.example.mistyweather.weather.atmosphere.computeAmbientAtmosphere

// ─── Weather Mapper ───────────────────────────────────────────────────────────
//
// Phase 3.6: Architectural firewall between data/domain models and rendering.
//
// Ensures zero HTTP/DTO/Database fields ever leak into Canvas layers.
//
// ─────────────────────────────────────────────────────────────────────────────

object WeatherMapper {

    /**
     * Maps a [WeatherType] and [TimeOfDay] into a frozen [WeatherRenderState].
     */
    fun toRenderState(
        weatherType: WeatherType,
        timeOfDay: TimeOfDay = TimeOfDay.Noon,
        timeProgress: Float = 0.5f,
    ): WeatherRenderState {
        val atmosphere = computeAmbientAtmosphere(weatherType, timeOfDay, timeProgress)
        return WeatherRenderState(
            weatherType  = weatherType,
            atmosphere   = atmosphere,
            timeOfDay    = timeOfDay,
            timeProgress = timeProgress,
        )
    }

    /**
     * Maps a domain [WeatherState] into a frozen [WeatherRenderState].
     */
    fun toRenderState(
        weatherState: WeatherState,
        timeOfDay: TimeOfDay = TimeOfDay.Noon,
        timeProgress: Float = 0.5f,
    ): WeatherRenderState = toRenderState(
        weatherType  = weatherState.weatherType,
        timeOfDay    = timeOfDay,
        timeProgress = timeProgress,
    )

    /**
     * Converts a domain [WeatherState] into UI [WeatherPreview] for legacy/home screen compatibility.
     */
    fun toWeatherPreview(
        weatherState: WeatherState,
        timeOfDay: TimeOfDay? = null,
    ): WeatherPreview = WeatherPreview(
        city        = weatherState.cityName,
        cityLatin   = weatherState.cityNameLatin,
        temperature = weatherState.temperature,
        condition   = weatherState.condition.displayName,
        feelsLike   = weatherState.feelsLike,
        humidity    = weatherState.humidity,
        weatherType = weatherState.weatherType,
        timeOfDay   = timeOfDay,
    )

    /**
     * Maps a list of domain [DomainForecastSlot] into UI [ForecastSlot]s.
     */
    fun toForecastSlots(domainSlots: List<DomainForecastSlot>): List<ForecastSlot> =
        domainSlots.map { slot ->
            ForecastSlot(
                label       = slot.timeLabel,
                temperature = slot.temperature,
                weatherType = slot.weatherType,
            )
        }
}
