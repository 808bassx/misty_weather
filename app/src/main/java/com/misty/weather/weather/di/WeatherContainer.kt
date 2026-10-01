package com.misty.weather.weather.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.misty.weather.BuildConfig
import com.misty.weather.weather.cache.DataStoreWeatherCache
import com.misty.weather.weather.cache.InMemoryWeatherCache
import com.misty.weather.weather.cache.WeatherCache
import com.misty.weather.weather.city.CityRepository
import com.misty.weather.weather.city.CityStorage
import com.misty.weather.weather.city.DataStoreCityStorage
import com.misty.weather.weather.city.DefaultCityRepository
import com.misty.weather.weather.city.InMemoryCityStorage
import com.misty.weather.weather.data.FakeWeatherRepository
import com.misty.weather.weather.data.remote.RemoteWeatherRepository
import com.misty.weather.weather.data.remote.WeatherApi
import com.misty.weather.weather.data.remote.WeatherApiClient
import com.misty.weather.weather.domain.WeatherRepository
import com.misty.weather.weather.domain.usecase.DefaultGetCurrentWeatherUseCase
import com.misty.weather.weather.domain.usecase.GetCurrentWeatherUseCase
import com.misty.weather.weather.location.DefaultLocationProvider
import com.misty.weather.weather.location.FakeLocationProvider
import com.misty.weather.weather.location.LocationProvider

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

private val Context.mistyDataStore: DataStore<Preferences> by preferencesDataStore(name = "misty_weather_prefs")

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
     * Initializes the production DI container with Android system services,
     * persistent DataStore preferences for city storage and weather cache,
     * and hardware [DefaultLocationProvider].
     */
    fun initialize(
        context: Context,
        useRemote: Boolean = true,
    ) {
        val appContext = context.applicationContext
        val dataStore = appContext.mistyDataStore
        val cache = DataStoreWeatherCache(dataStore)
        val cityStorage = DataStoreCityStorage(dataStore)
        val cityRepository = DefaultCityRepository(cityStorage)
        val locationProvider = DefaultLocationProvider(appContext)
        val weatherRepository = if (useRemote) {
            RemoteWeatherRepository(api = WeatherApiClient.api, cache = cache)
        } else {
            FakeWeatherRepository()
        }
        val useCase = DefaultGetCurrentWeatherUseCase(cityRepository, weatherRepository)

        containerInstance = object : WeatherContainer {
            override val weatherRepository: WeatherRepository = weatherRepository
            override val weatherCache: WeatherCache = cache
            override val cityStorage: CityStorage = cityStorage
            override val cityRepository: CityRepository = cityRepository
            override val locationProvider: LocationProvider = locationProvider
            override val getCurrentWeatherUseCase: GetCurrentWeatherUseCase = useCase
        }
    }

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
