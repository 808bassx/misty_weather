package com.misty.weather.weather.data.remote

import com.misty.weather.weather.atmosphere.TimeOfDay
import com.misty.weather.weather.cache.CachedWeather
import com.misty.weather.weather.cache.WeatherCache
import com.misty.weather.weather.data.FakeWeatherRepository
import com.misty.weather.weather.data.mapper.WeatherDtoMapper
import com.misty.weather.weather.domain.WeatherMapper
import com.misty.weather.weather.domain.WeatherRepository
import com.misty.weather.weather.domain.WeatherState
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
// Phase 4.5: Requests are scoped to coordinates only. The internal city table is
// gone — city/coordinate resolution belongs to the domain layer.
//
// ─────────────────────────────────────────────────────────────────────────────

class RemoteWeatherRepository(
    private val api: WeatherApi = WeatherApiClient.api,
    private val cache: WeatherCache,
    initialState: WeatherState = FakeWeatherRepository.canonicalTokyoWeather,
) : WeatherRepository {

    private val _weatherFlow = MutableStateFlow(initialState)

    override fun getWeather(
        latitude: Double,
        longitude: Double,
        cityName: String,
        cityNameLatin: String,
    ): Flow<WeatherState> = flow {
        // 1. Emit cached weather first if available for instant cold-boot presentation
        val cached = cache.load()
        if (cached != null) {
            _weatherFlow.value = cached.weatherState
            emit(cached.weatherState)
        }

        // 2. Fetch fresh weather from remote
        try {
            fetchAndPersist(latitude, longitude, cityName, cityNameLatin)
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

    override suspend fun refreshWeather(
        latitude: Double,
        longitude: Double,
        cityName: String,
        cityNameLatin: String,
    ) {
        fetchAndPersist(latitude, longitude, cityName, cityNameLatin)
    }

    /**
     * Executes remote network call for explicit coordinates, maps response to domain state, and saves to cache.
     */
    suspend fun fetchAndPersist(
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

    private fun resolveDiurnalTime(): Pair<TimeOfDay, Float> {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val time = TimeOfDay.from(hour, minute)
        val progress = TimeOfDay.progressWithin(hour, minute)
        return time to progress
    }
}
