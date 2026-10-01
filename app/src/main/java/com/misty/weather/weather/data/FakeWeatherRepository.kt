package com.misty.weather.weather.data

import com.misty.weather.core.weather.WeatherType
import com.misty.weather.weather.domain.DomainForecastSlot
import com.misty.weather.weather.domain.WeatherCondition
import com.misty.weather.weather.domain.WeatherRepository
import com.misty.weather.weather.domain.WeatherState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// ─── Fake Weather Repository ──────────────────────────────────────────────────
//
// Phase 3.6 Data Layer: In-memory repository implementation for verification & testing.
//
// Supplies canonical Tokyo weather data (23°C, Sunny, Sunrise 05:32, Sunset 17:28).
// Zero network calls, zero external libraries.
//
// ─────────────────────────────────────────────────────────────────────────────

class FakeWeatherRepository(
    initialState: WeatherState = canonicalTokyoWeather,
) : WeatherRepository {

    private val _weatherFlow = MutableStateFlow(initialState)

    override fun getWeather(cityId: String): Flow<WeatherState> = _weatherFlow.asStateFlow()

    override suspend fun refreshWeather(cityId: String) {
        // No-op for fake repository
    }

    override fun getWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        cityName: String,
        cityNameLatin: String,
    ): Flow<WeatherState> {
        return if (cityName.isNotBlank()) {
            kotlinx.coroutines.flow.flow {
                _weatherFlow.collect { state ->
                    emit(
                        state.copy(
                            cityName = cityName,
                            cityNameLatin = if (cityNameLatin.isNotBlank()) cityNameLatin else state.cityNameLatin,
                        )
                    )
                }
            }
        } else {
            _weatherFlow.asStateFlow()
        }
    }

    override suspend fun refreshWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        cityName: String,
        cityNameLatin: String,
    ) {
        if (cityName.isNotBlank()) {
            _weatherFlow.value = _weatherFlow.value.copy(
                cityName = cityName,
                cityNameLatin = if (cityNameLatin.isNotBlank()) cityNameLatin else _weatherFlow.value.cityNameLatin,
            )
        }
    }

    /** Helper to simulate weather transitions in demo & tests. */
    fun updateWeather(newState: WeatherState) {
        _weatherFlow.value = newState
    }

    companion object {
        /** Canonical offline state for Tokyo. */
        val canonicalTokyoWeather = WeatherState(
            cityName         = "东京",
            cityNameLatin    = "Tokyo",
            temperature      = 23,
            feelsLike        = 22,
            humidity         = 71,
            condition        = WeatherCondition.PartlyCloudy,
            weatherType      = WeatherType.PartlyCloudy,
            sunriseTime      = "05:32",
            sunsetTime       = "17:28",
            hourlyForecast   = listOf(
                DomainForecastSlot(timeLabel = "现在",  temperature = 23, condition = WeatherCondition.PartlyCloudy),
                DomainForecastSlot(timeLabel = "16:00", temperature = 23, condition = WeatherCondition.PartlyCloudy),
                DomainForecastSlot(timeLabel = "17:00", temperature = 22, condition = WeatherCondition.PartlyCloudy),
                DomainForecastSlot(timeLabel = "18:00", temperature = 21, condition = WeatherCondition.Drizzle),
                DomainForecastSlot(timeLabel = "19:00", temperature = 20, condition = WeatherCondition.ModerateRain),
                DomainForecastSlot(timeLabel = "20:00", temperature = 19, condition = WeatherCondition.ModerateRain),
            ),
        )
    }
}
