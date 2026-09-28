package com.example.mistyweather.weather.data.remote

import com.example.mistyweather.weather.atmosphere.TimeOfDay
import com.example.mistyweather.weather.cache.CachedWeather
import com.example.mistyweather.weather.cache.WeatherCache
import com.example.mistyweather.weather.data.FakeWeatherRepository
import com.example.mistyweather.weather.data.mapper.WeatherDtoMapper
import com.example.mistyweather.weather.domain.WeatherMapper
import com.example.mistyweather.weather.domain.WeatherRepository
import com.example.mistyweather.weather.domain.WeatherState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.util.Calendar

// ─── Remote Weather Repository ───────────────────────────────────────────────
//
// Phase 4.2: Open-Meteo REST repository implementation.
//
// Coordinates:
//   1. Remote API calls via [WeatherApi].
//   2. Firewall DTO mapping via [WeatherDtoMapper].
//   3. Write-through persistence via [WeatherCache].
//   4. Offline error recovery with graceful cache fallback.
//
// ─────────────────────────────────────────────────────────────────────────────

class RemoteWeatherRepository(
    private val api: WeatherApi = WeatherApiClient.api,
    private val cache: WeatherCache,
    initialState: WeatherState = FakeWeatherRepository.canonicalTokyoWeather,
) : WeatherRepository {

    private val _weatherFlow = MutableStateFlow(initialState)

    override fun getWeather(cityId: String): Flow<WeatherState> = flow {
        // 1. Emit cached weather first if available for instant cold-boot presentation
        val cached = cache.load()
        if (cached != null) {
            _weatherFlow.value = cached.weatherState
            emit(cached.weatherState)
        }

        // 2. Fetch fresh weather from remote
        try {
            fetchAndPersist(cityId)
        } catch (e: Exception) {
            // If there is no cache at all, propagate exception to trigger Error state in UI
            if (cached == null) {
                throw e
            }
        }

        // 3. Collect future updates triggered by refreshWeather()
        _weatherFlow.collect { state ->
            emit(state)
        }
    }.distinctUntilChanged()

    override suspend fun refreshWeather(cityId: String) {
        fetchAndPersist(cityId)
    }

    override fun getWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        cityName: String,
        cityNameLatin: String,
    ): Flow<WeatherState> = flow {
        val cached = cache.load()
        if (cached != null) {
            _weatherFlow.value = cached.weatherState
            emit(cached.weatherState)
        }

        try {
            fetchAndPersistCoordinates(latitude, longitude, cityName, cityNameLatin)
        } catch (e: Exception) {
            if (cached == null) {
                throw e
            }
        }

        _weatherFlow.collect { state ->
            emit(state)
        }
    }.distinctUntilChanged()

    override suspend fun refreshWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        cityName: String,
        cityNameLatin: String,
    ) {
        fetchAndPersistCoordinates(latitude, longitude, cityName, cityNameLatin)
    }

    /**
     * Executes remote network call for a cityId, maps response to domain state, and saves to cache.
     */
    suspend fun fetchAndPersist(cityId: String = "tokyo"): WeatherState {
        val (lat, lon, name, nameLatin) = resolveCoordinates(cityId)
        return fetchAndPersistCoordinates(lat, lon, name, nameLatin, locationKey = cityId)
    }

    /**
     * Executes remote network call for explicit coordinates, maps response to domain state, and saves to cache.
     */
    suspend fun fetchAndPersistCoordinates(
        latitude: Double,
        longitude: Double,
        cityName: String,
        cityNameLatin: String,
        locationKey: String = "coord_${latitude}_${longitude}",
    ): WeatherState {
        val dto = api.getForecast(latitude = latitude, longitude = longitude)
        val domainState = WeatherDtoMapper.toDomainState(dto, cityName, cityNameLatin)

        _weatherFlow.value = domainState

        // Write-through to cache
        val (time, progress) = resolveDiurnalTime()
        val renderState = WeatherMapper.toRenderState(domainState.weatherType, time, progress)
        cache.save(CachedWeather(locationKey, domainState, renderState, System.currentTimeMillis()))

        return domainState
    }

    private fun resolveCoordinates(cityId: String): CityConfig {
        return when (cityId.lowercase()) {
            "tokyo" -> CityConfig(35.6895, 139.6917, "东京", "Tokyo")
            "kyoto" -> CityConfig(35.0116, 135.7681, "京都", "Kyoto")
            "osaka" -> CityConfig(34.6937, 135.5023, "大阪", "Osaka")
            "beijing" -> CityConfig(39.9042, 116.4074, "北京", "Beijing")
            "shanghai" -> CityConfig(31.2304, 121.4737, "上海", "Shanghai")
            else -> CityConfig(35.6895, 139.6917, "东京", "Tokyo")
        }
    }

    private fun resolveDiurnalTime(): Pair<TimeOfDay, Float> {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val time = TimeOfDay.from(hour, minute)
        val progress = TimeOfDay.progressWithin(hour, minute)
        return time to progress
    }

    private data class CityConfig(
        val latitude: Double,
        val longitude: Double,
        val name: String,
        val nameLatin: String,
    )
}
