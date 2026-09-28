package com.example.mistyweather.weather.domain.usecase

import com.example.mistyweather.weather.city.City
import com.example.mistyweather.weather.city.CityRepository
import com.example.mistyweather.weather.domain.WeatherRepository
import com.example.mistyweather.weather.domain.WeatherState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

// ─── Weather Use Cases ────────────────────────────────────────────────────────
//
// Phase 4.3-R1: Domain Use Case isolating City selection and Weather retrieval.
//
// Mediates between [CityRepository] and [WeatherRepository], producing a unified
// reactive stream of [WeatherState] for the currently active city.
// Reusable across ViewModel, Background Workers, and upcoming App Widgets.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Use case supplying continuous reactive weather data for whichever city is currently active.
 */
interface GetCurrentWeatherUseCase {
    operator fun invoke(): Flow<WeatherState>
    suspend fun refresh()
}

/**
 * Default implementation of [GetCurrentWeatherUseCase].
 */
class DefaultGetCurrentWeatherUseCase(
    private val cityRepository: CityRepository,
    private val weatherRepository: WeatherRepository,
) : GetCurrentWeatherUseCase {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke(): Flow<WeatherState> = cityRepository.getCurrentCity()
        .distinctUntilChanged()
        .flatMapLatest { city ->
            weatherRepository.getWeatherByCoordinates(
                latitude      = city.latitude,
                longitude     = city.longitude,
                cityName      = city.name,
                cityNameLatin = city.nameLatin,
            )
        }

    override suspend fun refresh() {
        val city = cityRepository.getCurrentCity().first()
        weatherRepository.refreshWeatherByCoordinates(
            latitude      = city.latitude,
            longitude     = city.longitude,
            cityName      = city.name,
            cityNameLatin = city.nameLatin,
        )
    }
}

/**
 * Use case querying weather on-demand for an arbitrary [City] instance.
 */
class GetWeatherForCityUseCase(
    private val weatherRepository: WeatherRepository,
) {
    operator fun invoke(city: City): Flow<WeatherState> =
        weatherRepository.getWeatherByCoordinates(
            latitude      = city.latitude,
            longitude     = city.longitude,
            cityName      = city.name,
            cityNameLatin = city.nameLatin,
        )

    suspend fun refresh(city: City) {
        weatherRepository.refreshWeatherByCoordinates(
            latitude      = city.latitude,
            longitude     = city.longitude,
            cityName      = city.name,
            cityNameLatin = city.nameLatin,
        )
    }
}
