package com.example.mistyweather.weather.di

import com.example.mistyweather.weather.data.FakeWeatherRepository
import com.example.mistyweather.weather.domain.WeatherRepository

// ─── Weather DI Container ─────────────────────────────────────────────────────
//
// Phase 4.0: Dependency Injection Foundation.
//
// Establishes a lightweight, testable container for weather data sources.
// Decouples ViewModel from concrete repository implementations.
// In Phase 4.2, RemoteWeatherRepository can be seamlessly injected here.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dependency container contract exposing weather architecture components.
 */
interface WeatherContainer {
    val weatherRepository: WeatherRepository
}

/**
 * Default container configuration.
 * Provides offline [FakeWeatherRepository] for Phase 4.0 & 4.1.
 */
class DefaultWeatherContainer(
    override val weatherRepository: WeatherRepository = FakeWeatherRepository(),
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
