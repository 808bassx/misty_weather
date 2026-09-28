package com.example.mistyweather.weather.di

import com.example.mistyweather.BuildConfig
import com.example.mistyweather.weather.cache.InMemoryWeatherCache
import com.example.mistyweather.weather.cache.WeatherCache
import com.example.mistyweather.weather.city.CityRepository
import com.example.mistyweather.weather.city.CityStorage
import com.example.mistyweather.weather.city.DefaultCityRepository
import com.example.mistyweather.weather.city.InMemoryCityStorage
import com.example.mistyweather.weather.data.FakeWeatherRepository
import com.example.mistyweather.weather.data.remote.RemoteWeatherRepository
import com.example.mistyweather.weather.data.remote.WeatherApi
import com.example.mistyweather.weather.data.remote.WeatherApiClient
import com.example.mistyweather.weather.domain.WeatherRepository
import com.example.mistyweather.weather.domain.usecase.DefaultGetCurrentWeatherUseCase
import com.example.mistyweather.weather.domain.usecase.GetCurrentWeatherUseCase
import com.example.mistyweather.weather.location.FakeLocationProvider
import com.example.mistyweather.weather.location.LocationProvider

// ─── Weather DI Container ─────────────────────────────────────────────────────
//
// Phase 4.0, 4.1.5, 4.2 & 4.3: Dependency Injection Foundation.
//
// Establishes a lightweight, testable container for weather, location, and city systems.
// Decouples ViewModel from concrete repositories, networks, providers, and persistence.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dependency container contract exposing weather architecture components.
 */
interface WeatherContainer {
    val weatherRepository: WeatherRepository
    val weatherCache: WeatherCache
    val cityStorage: CityStorage
        get() = InMemoryCityStorage()
    val cityRepository: CityRepository
        get() = DefaultCityRepository(cityStorage)
    val locationProvider: LocationProvider
        get() = FakeLocationProvider()
    val getCurrentWeatherUseCase: GetCurrentWeatherUseCase
        get() = DefaultGetCurrentWeatherUseCase(cityRepository, weatherRepository)
}

/**
 * Default container configuration.
 * Provides remote [RemoteWeatherRepository] (backed by Open-Meteo REST API) with offline cache,
 * and allows instant fallback or switching to offline [FakeWeatherRepository].
 */
class DefaultWeatherContainer(
    override val weatherCache: WeatherCache = InMemoryWeatherCache(),
    override val cityStorage: CityStorage = InMemoryCityStorage(),
    override val cityRepository: CityRepository = DefaultCityRepository(cityStorage),
    override val locationProvider: LocationProvider = FakeLocationProvider(),
    val api: WeatherApi = WeatherApiClient.api,
    val useRemote: Boolean = !BuildConfig.DEBUG,
    override val weatherRepository: WeatherRepository = if (useRemote) {
        RemoteWeatherRepository(api = api, cache = weatherCache)
    } else {
        FakeWeatherRepository()
    },
    override val getCurrentWeatherUseCase: GetCurrentWeatherUseCase =
        DefaultGetCurrentWeatherUseCase(cityRepository, weatherRepository),
) : WeatherContainer {
    companion object {
        fun createRemote(
            api: WeatherApi = WeatherApiClient.api,
            cache: WeatherCache = InMemoryWeatherCache(),
            cityStorage: CityStorage = InMemoryCityStorage(),
            cityRepository: CityRepository = DefaultCityRepository(cityStorage),
            locationProvider: LocationProvider = FakeLocationProvider(),
        ): DefaultWeatherContainer {
            return DefaultWeatherContainer(
                weatherCache = cache,
                cityStorage = cityStorage,
                cityRepository = cityRepository,
                locationProvider = locationProvider,
                api = api,
                useRemote = true,
            )
        }

        fun createOffline(
            cache: WeatherCache = InMemoryWeatherCache(),
            cityStorage: CityStorage = InMemoryCityStorage(),
            cityRepository: CityRepository = DefaultCityRepository(cityStorage),
            locationProvider: LocationProvider = FakeLocationProvider(),
        ): DefaultWeatherContainer {
            return DefaultWeatherContainer(
                weatherCache = cache,
                cityStorage = cityStorage,
                cityRepository = cityRepository,
                locationProvider = locationProvider,
                useRemote = false,
            )
        }
    }
}

/**
 * Global dependency provider module for weather components.
 * Supports runtime replacement for hermetic unit and UI tests.
 */
object WeatherModule {
    @Volatile
    private var containerInstance: WeatherContainer = DefaultWeatherContainer()

    /**
     * Active [WeatherContainer] instance.
     */
    val container: WeatherContainer
        get() = containerInstance

    /**
     * Replaces the active container (e.g. for testing with custom mocks/fakes).
     */
    fun setCustomContainer(container: WeatherContainer) {
        containerInstance = container
    }

    /**
     * Switches to offline [FakeWeatherRepository].
     */
    fun useFakeRepository() {
        containerInstance = DefaultWeatherContainer(useRemote = false)
    }

    /**
     * Switches to [RemoteWeatherRepository] with custom or default [WeatherApi].
     */
    fun useRemoteRepository(api: WeatherApi = WeatherApiClient.api) {
        containerInstance = DefaultWeatherContainer(api = api, useRemote = true)
    }

    /**
     * Resets the active container to [DefaultWeatherContainer].
     */
    fun reset() {
        containerInstance = DefaultWeatherContainer()
    }
}
