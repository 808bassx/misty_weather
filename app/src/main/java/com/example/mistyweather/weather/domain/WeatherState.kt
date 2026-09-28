package com.example.mistyweather.weather.domain

import com.example.mistyweather.core.weather.WeatherType

// ─── Weather Domain State ─────────────────────────────────────────────────────
//
// Phase 3.6 Domain Layer: Pure domain models for weather reports.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Complete domain weather state for a geographic location.
 *
 * @param cityName         Localized city name (e.g. "东京").
 * @param cityNameLatin    Romanized city name (e.g. "Tokyo").
 * @param temperature      Current temperature in °C.
 * @param feelsLike        Apparent temperature in °C.
 * @param humidity         Relative humidity percentage (0-100).
 * @param condition        Domain [WeatherCondition].
 * @param weatherType      Underlying [WeatherType] for rendering.
 * @param sunriseTime      Formatted sunrise timestamp (e.g. "05:32").
 * @param sunsetTime       Formatted sunset timestamp (e.g. "17:28").
 * @param hourlyForecast   24-hour domain forecast strip.
 */
data class WeatherState(
    val cityName: String,
    val cityNameLatin: String,
    val temperature: Int,
    val feelsLike: Int,
    val humidity: Int,
    val condition: WeatherCondition,
    val weatherType: WeatherType = condition.canonicalWeatherType,
    val sunriseTime: String = "05:32",
    val sunsetTime: String = "17:28",
    val hourlyForecast: List<DomainForecastSlot> = emptyList(),
)

/**
 * A single hourly forecast item in the domain layer.
 */
data class DomainForecastSlot(
    val timeLabel: String,
    val temperature: Int,
    val condition: WeatherCondition,
    val weatherType: WeatherType = condition.canonicalWeatherType,
)
