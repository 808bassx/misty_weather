package com.example.mistyweather.weather.cache

import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import com.example.mistyweather.weather.domain.DomainForecastSlot
import com.example.mistyweather.weather.domain.WeatherCondition
import com.example.mistyweather.weather.domain.WeatherMapper
import com.example.mistyweather.weather.domain.WeatherRenderState
import com.example.mistyweather.weather.domain.WeatherState
import kotlinx.serialization.Serializable

// ─── Cached Weather Model ─────────────────────────────────────────────────────
//
// Phase 4.1.5: Persistent Offline Weather Cache.
//
// Holds the persistent representation of weather and rendering state.
// Reconstructs the frozen [WeatherRenderState] contract without modifying
// any rendering engine classes or leaking serialization into domain models.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Domain representation of cached weather data.
 *
 * @param location     Unique location identifier (e.g. "tokyo").
 * @param weatherState Pure domain state loaded from disk.
 * @param renderState  Reconstructed frozen rendering contract.
 * @param timestamp    Timestamp when this snapshot was persisted.
 */
data class CachedWeather(
    val location: String,
    val weatherState: WeatherState,
    val renderState: WeatherRenderState,
    val timestamp: Long,
)

// ─── Serialization DTOs (Internal Disk Storage Format) ───────────────────────

@Serializable
internal data class CachedWeatherDto(
    val location: String,
    val cityName: String,
    val cityNameLatin: String,
    val temperature: Int,
    val feelsLike: Int,
    val humidity: Int,
    val conditionName: String,
    val weatherTypeName: String,
    val sunriseTime: String,
    val sunsetTime: String,
    val hourlyForecast: List<CachedForecastSlotDto>,
    val timeOfDayName: String,
    val timeProgress: Float,
    val timestamp: Long,
) {
    fun toCachedWeather(): CachedWeather {
        val weatherType = runCatching { WeatherType.valueOf(weatherTypeName) }
            .getOrDefault(WeatherType.PartlyCloudy)
        val condition = runCatching { WeatherCondition.valueOf(conditionName) }
            .getOrDefault(WeatherCondition.PartlyCloudy)
        val timeOfDay = runCatching { TimeOfDay.valueOf(timeOfDayName) }
            .getOrDefault(TimeOfDay.Noon)

        val domainState = WeatherState(
            cityName       = cityName,
            cityNameLatin  = cityNameLatin,
            temperature    = temperature,
            feelsLike      = feelsLike,
            humidity       = humidity,
            condition      = condition,
            weatherType    = weatherType,
            sunriseTime    = sunriseTime,
            sunsetTime     = sunsetTime,
            hourlyForecast = hourlyForecast.map { slot ->
                val slotType = runCatching { WeatherType.valueOf(slot.weatherTypeName) }
                    .getOrDefault(WeatherType.PartlyCloudy)
                val slotCond = runCatching { WeatherCondition.valueOf(slot.conditionName) }
                    .getOrDefault(WeatherCondition.PartlyCloudy)
                DomainForecastSlot(
                    timeLabel   = slot.timeLabel,
                    temperature = slot.temperature,
                    condition   = slotCond,
                    weatherType = slotType,
                )
            },
        )

        val renderState = WeatherMapper.toRenderState(
            weatherType  = domainState.weatherType,
            timeOfDay    = timeOfDay,
            timeProgress = timeProgress,
        )

        return CachedWeather(
            location     = location,
            weatherState = domainState,
            renderState  = renderState,
            timestamp    = timestamp,
        )
    }

    companion object {
        fun fromCachedWeather(cached: CachedWeather): CachedWeatherDto {
            return CachedWeatherDto(
                location        = cached.location,
                cityName        = cached.weatherState.cityName,
                cityNameLatin   = cached.weatherState.cityNameLatin,
                temperature     = cached.weatherState.temperature,
                feelsLike       = cached.weatherState.feelsLike,
                humidity        = cached.weatherState.humidity,
                conditionName   = cached.weatherState.condition.name,
                weatherTypeName = cached.weatherState.weatherType.name,
                sunriseTime     = cached.weatherState.sunriseTime,
                sunsetTime      = cached.weatherState.sunsetTime,
                hourlyForecast  = cached.weatherState.hourlyForecast.map { slot ->
                    CachedForecastSlotDto(
                        timeLabel       = slot.timeLabel,
                        temperature     = slot.temperature,
                        conditionName   = slot.condition.name,
                        weatherTypeName = slot.weatherType.name,
                    )
                },
                timeOfDayName   = cached.renderState.timeOfDay.name,
                timeProgress    = cached.renderState.timeProgress,
                timestamp       = cached.timestamp,
            )
        }
    }
}

@Serializable
internal data class CachedForecastSlotDto(
    val timeLabel: String,
    val temperature: Int,
    val conditionName: String,
    val weatherTypeName: String,
)
