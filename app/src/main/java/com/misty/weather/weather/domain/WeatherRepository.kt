package com.misty.weather.weather.domain

import kotlinx.coroutines.flow.Flow

// ─── Weather Repository ───────────────────────────────────────────────────────
//
// Phase 3.6 Domain Layer: Abstract repository boundary for weather data retrieval.
//
// Phase 4.5: Coordinates-only contract.
// A repository resolves weather for geographic coordinates and nothing else.
// Turning a [com.misty.weather.weather.city.City] into coordinates is the domain
// layer's job (see GetCurrentWeatherUseCase) — keeping it here previously forced
// every implementation to carry its own city table and silently fall back to Tokyo.
//
// ─────────────────────────────────────────────────────────────────────────────

interface WeatherRepository {

    /**
     * Continuous reactive stream of [WeatherState] for explicit geographic coordinates.
     *
     * @param latitude      Decimal latitude.
     * @param longitude     Decimal longitude.
     * @param cityName      Localized display name to label the result with.
     * @param cityNameLatin Romanized display name.
     */
    fun getWeather(
        latitude: Double,
        longitude: Double,
        cityName: String,
        cityNameLatin: String,
    ): Flow<WeatherState>

    /**
     * Refreshes weather state for explicit geographic coordinates.
     */
    suspend fun refreshWeather(
        latitude: Double,
        longitude: Double,
        cityName: String,
        cityNameLatin: String,
    )
}
