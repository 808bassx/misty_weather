package com.example.mistyweather.weather.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Open-Meteo Remote DTOs ──────────────────────────────────────────────────
//
// Phase 4.2: Pure data transfer objects for Open-Meteo REST API responses.
//
// Strictly confined to weather/data/remote/ and weather/data/mapper/.
// Under NO circumstances may these models leak into the domain layer or
// the frozen Canvas rendering engine.
//
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class WeatherResponseDto(
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("timezone") val timezone: String? = null,
    @SerialName("current") val current: CurrentWeatherDto,
    @SerialName("hourly") val hourly: HourlyWeatherDto? = null,
    @SerialName("daily") val daily: DailyWeatherDto? = null,
)

@Serializable
data class CurrentWeatherDto(
    @SerialName("time") val time: String? = null,
    @SerialName("temperature_2m") val temperature2m: Double,
    @SerialName("relative_humidity_2m") val relativeHumidity2m: Int = 0,
    @SerialName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerialName("weather_code") val weatherCode: Int = 0,
)

@Serializable
data class HourlyWeatherDto(
    @SerialName("time") val time: List<String> = emptyList(),
    @SerialName("temperature_2m") val temperature2m: List<Double> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int> = emptyList(),
)

@Serializable
data class DailyWeatherDto(
    @SerialName("time") val time: List<String> = emptyList(),
    @SerialName("sunrise") val sunrise: List<String> = emptyList(),
    @SerialName("sunset") val sunset: List<String> = emptyList(),
)
