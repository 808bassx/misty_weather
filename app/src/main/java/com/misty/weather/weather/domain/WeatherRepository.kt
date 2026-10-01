package com.misty.weather.weather.domain

import kotlinx.coroutines.flow.Flow

// ─── Weather Repository ───────────────────────────────────────────────────────
//
// Phase 3.6 Domain Layer: Abstract repository boundary for weather data retrieval.
//
// Phase 4 will supply RemoteWeatherRepository without affecting downstream UI.
//
// ─────────────────────────────────────────────────────────────────────────────

interface WeatherRepository {
    /**
     * Continuous reactive stream of [WeatherState] for a given location.
     */
    fun getWeather(cityId: String = "tokyo"): Flow<WeatherState>

    /**
     * Refreshes weather state from the underlying data source.
     */
    suspend fun refreshWeather(cityId: String = "tokyo")

    /**
     * Continuous reactive stream of [WeatherState] for explicit geographic coordinates.
     * Introduced in Phase 4.3 for dynamic location and multi-city support.
     */
    fun getWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        cityName: String = "当前位置",
        cityNameLatin: String = "Current Location",
    ): Flow<WeatherState> = getWeather("tokyo")

    /**
     * Refreshes weather state for explicit geographic coordinates.
     */
    suspend fun refreshWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        cityName: String = "当前位置",
        cityNameLatin: String = "Current Location",
    ) {
        refreshWeather("tokyo")
    }
}
