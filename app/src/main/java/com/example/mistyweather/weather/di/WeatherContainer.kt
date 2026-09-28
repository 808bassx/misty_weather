package com.example.mistyweather.weather.di

import com.example.mistyweather.weather.cache.InMemoryWeatherCache
import com.example.mistyweather.weather.cache.WeatherCache
import com.example.mistyweather.weather.data.FakeWeatherRepository
import com.example.mistyweather.weather.domain.WeatherRepository

// ─── Weather DI Container ─────────────────────────────────────────────────────
//
// Phase 4.0 & 4.1.5: Dependency Injection Foundation.
//
// Establishes a lightweight, testable container for weather data sources and cache.
// Decouples ViewModel from concrete repository and persistence implementations.
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
 * Provides offline [FakeWeatherRepository] and [WeatherCache].
 */
class DefaultWeatherContainer(
    override val weatherRepository: WeatherRepository = FakeWeatherRepository(),
    override val weatherCache: WeatherCache = InMemoryWeatherCache(),
) : WeatherContainer

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
     * Resets the active container to [DefaultWeatherContainer].
     */
    fun reset() {
        containerInstance = DefaultWeatherContainer()
    }
}
