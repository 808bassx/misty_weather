package com.example.mistyweather.weather.di

import com.example.mistyweather.BuildConfig
import com.example.mistyweather.weather.cache.InMemoryWeatherCache
import com.example.mistyweather.weather.cache.WeatherCache
import com.example.mistyweather.weather.data.FakeWeatherRepository
import com.example.mistyweather.weather.data.remote.RemoteWeatherRepository
import com.example.mistyweather.weather.data.remote.WeatherApi
import com.example.mistyweather.weather.data.remote.WeatherApiClient
import com.example.mistyweather.weather.domain.WeatherRepository

// ─── Weather DI Container ─────────────────────────────────────────────────────
//
// Phase 4.0, 4.1.5 & 4.2: Dependency Injection Foundation.
//
// Establishes a lightweight, testable container for weather data sources and cache.
// Decouples ViewModel from concrete repository, network, and persistence implementations.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dependency container contract exposing weather architecture components.
 */
interface WeatherContainer {
    val weatherRepository: WeatherRepository
    val weatherCache: WeatherCache
}

/**
 * Default container configuration.
 * Provides remote [RemoteWeatherRepository] (backed by Open-Meteo REST API) with offline cache,
 * and allows instant fallback or switching to offline [FakeWeatherRepository].
 */
class DefaultWeatherContainer(
    override val weatherCache: WeatherCache = InMemoryWeatherCache(),
    val api: WeatherApi = WeatherApiClient.api,
    val useRemote: Boolean = !BuildConfig.DEBUG,
    override val weatherRepository: WeatherRepository = if (useRemote) {
        RemoteWeatherRepository(api = api, cache = weatherCache)
    } else {
        FakeWeatherRepository()
    },
) : WeatherContainer {
    companion object {
        fun createRemote(
            api: WeatherApi = WeatherApiClient.api,
            cache: WeatherCache = InMemoryWeatherCache(),
        ): DefaultWeatherContainer {
            return DefaultWeatherContainer(
                weatherCache = cache,
                api = api,
                useRemote = true,
            )
        }

        fun createOffline(cache: WeatherCache = InMemoryWeatherCache()): DefaultWeatherContainer {
            return DefaultWeatherContainer(
                weatherCache = cache,
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
