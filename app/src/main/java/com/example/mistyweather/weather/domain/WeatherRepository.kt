package com.example.mistyweather.weather.domain

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
}
