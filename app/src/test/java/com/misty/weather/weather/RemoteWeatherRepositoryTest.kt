package com.misty.weather.weather

import com.misty.weather.core.weather.WeatherType
import com.misty.weather.weather.cache.CachedWeather
import com.misty.weather.weather.cache.InMemoryWeatherCache
import com.misty.weather.weather.data.FakeWeatherRepository
import com.misty.weather.weather.data.mapper.WeatherDtoMapper
import com.misty.weather.weather.data.remote.CurrentWeatherDto
import com.misty.weather.weather.data.remote.RemoteWeatherRepository
import com.misty.weather.weather.data.remote.WeatherApi
import com.misty.weather.weather.data.remote.WeatherResponseDto
import com.misty.weather.weather.domain.WeatherCondition
import com.misty.weather.weather.domain.WeatherMapper
import com.misty.weather.weather.domain.WeatherState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

// ─── Remote Weather Repository Tests ──────────────────────────────────────────
//
// Phase 4.2: Open-Meteo REST repository verification.
//
// Tests:
//   • Network success flow & write-through cache
//   • Cache-first instant cold-boot presentation
//   • HTTP 500 & network error fallback to cache
//   • Cold-start error propagation when cache is empty
//   • City coordinate resolution
//
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteWeatherRepositoryTest {

    private class TestWeatherApi : WeatherApi {
        var responseToReturn: WeatherResponseDto = WeatherResponseDto(
            current = CurrentWeatherDto(
                time = "2026-09-28T16:00",
                temperature2m = 26.0,
                apparentTemperature = 25.0,
                relativeHumidity2m = 55,
                weatherCode = 0, // ClearSky
            )
        )
        var exceptionToThrow: Throwable? = null
        var lastRequestedLatitude: Double? = null
        var lastRequestedLongitude: Double? = null

        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            current: String,
            hourly: String,
            daily: String,
            timezone: String,
        ): WeatherResponseDto {
            lastRequestedLatitude = latitude
            lastRequestedLongitude = longitude
            exceptionToThrow?.let { throw it }
            return responseToReturn
        }
    }

    @Test
    fun `getWeather fetches from API, maps to domain, and writes through to cache`() = runTest {
        val testApi = TestWeatherApi()
        val cache = InMemoryWeatherCache()
        val repository = RemoteWeatherRepository(api = testApi, cache = cache)

        val weather = repository.getWeather("tokyo").first()

        assertEquals(26, weather.temperature)
        assertEquals(WeatherCondition.ClearSky, weather.condition)
        assertEquals(WeatherType.Sunny, weather.weatherType)

        // Verify write-through to cache
        val cached = cache.load()
        assertNotNull("Data should be persisted to cache", cached)
        assertEquals("tokyo", cached?.location)
        assertEquals(26, cached?.weatherState?.temperature)
        assertEquals(WeatherType.Sunny, cached?.renderState?.weatherType)
    }

    @Test
    fun `getWeather emits cached state first before network response arrives`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testApi = TestWeatherApi()
        val cache = InMemoryWeatherCache()

        // Seed cache with cold cached weather (15°C, Rain)
        val cachedState = FakeWeatherRepository.canonicalTokyoWeather.copy(
            temperature = 15,
            condition = WeatherCondition.ModerateRain,
            weatherType = WeatherType.Rain,
        )
        val renderState = WeatherMapper.toRenderState(cachedState)
        cache.save(CachedWeather("tokyo", cachedState, renderState, 1000L))

        // Set API response to fresh weather (28°C, ClearSky)
        testApi.responseToReturn = WeatherResponseDto(
            current = CurrentWeatherDto(
                temperature2m = 28.0,
                weatherCode = 0,
            )
        )

        val repository = RemoteWeatherRepository(api = testApi, cache = cache)
        val emissions = mutableListOf<WeatherState>()

        val job = launch(testDispatcher) {
            repository.getWeather("tokyo").toList(emissions)
        }

        assertTrue("Should have at least 2 emissions", emissions.size >= 2)
        assertEquals("First emission must be cached state", 15, emissions[0].temperature)
        assertEquals("Second emission must be fresh API state", 28, emissions[1].temperature)

        job.cancel()
    }

    @Test
    fun `getWeather falls back to cache seamlessly on HTTP 500 error`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testApi = TestWeatherApi()
        val cache = InMemoryWeatherCache()

        // Seed cache with valid state
        val cachedState = FakeWeatherRepository.canonicalTokyoWeather.copy(
            temperature = 19,
            condition = WeatherCondition.PartlyCloudy,
        )
        val renderState = WeatherMapper.toRenderState(cachedState)
        cache.save(CachedWeather("tokyo", cachedState, renderState, 2000L))

        // Simulate HTTP 500 Server Error
        testApi.exceptionToThrow = IOException("HTTP 500 Internal Server Error")

        val repository = RemoteWeatherRepository(api = testApi, cache = cache)
        val emissions = mutableListOf<WeatherState>()

        val job = launch(testDispatcher) {
            repository.getWeather("tokyo").toList(emissions)
        }

        // Must emit cached weather without crashing the flow
        assertEquals(1, emissions.size)
        assertEquals(19, emissions[0].temperature)
        assertEquals(WeatherCondition.PartlyCloudy, emissions[0].condition)

        job.cancel()
    }

    @Test
    fun `getWeather rethrows error if cache is completely empty`() = runTest {
        val testApi = TestWeatherApi()
        val emptyCache = InMemoryWeatherCache()

        testApi.exceptionToThrow = IOException("Network connection refused")

        val repository = RemoteWeatherRepository(api = testApi, cache = emptyCache)

        try {
            repository.getWeather("tokyo").first()
            fail("Expected IOException when cache is empty and network fails")
        } catch (e: IOException) {
            assertEquals("Network connection refused", e.message)
        }
    }

    @Test
    fun `refreshWeather updates repository state and persists fresh snapshot`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testApi = TestWeatherApi()
        val cache = InMemoryWeatherCache()
        val repository = RemoteWeatherRepository(api = testApi, cache = cache)

        val emissions = mutableListOf<WeatherState>()
        val job = launch(testDispatcher) {
            repository.getWeather("tokyo").toList(emissions)
        }

        // Initial fetch: 26°C
        assertEquals(26, emissions.last().temperature)

        // Trigger refresh with new temperature: 31°C
        testApi.responseToReturn = WeatherResponseDto(
            current = CurrentWeatherDto(
                temperature2m = 31.0,
                weatherCode = 1,
            )
        )
        repository.refreshWeather("tokyo")

        assertEquals(31, emissions.last().temperature)
        assertEquals(31, cache.load()?.weatherState?.temperature)

        job.cancel()
    }

    @Test
    fun `fetchAndPersist passes correct coordinates for supported cities`() = runTest {
        val testApi = TestWeatherApi()
        val cache = InMemoryWeatherCache()
        val repository = RemoteWeatherRepository(api = testApi, cache = cache)

        repository.refreshWeather("tokyo")
        assertEquals(35.6895, testApi.lastRequestedLatitude!!, 0.001)
        assertEquals(139.6917, testApi.lastRequestedLongitude!!, 0.001)

        repository.refreshWeather("beijing")
        assertEquals(39.9042, testApi.lastRequestedLatitude!!, 0.001)
        assertEquals(116.4074, testApi.lastRequestedLongitude!!, 0.001)

        repository.refreshWeather("shanghai")
        assertEquals(31.2304, testApi.lastRequestedLatitude!!, 0.001)
        assertEquals(121.4737, testApi.lastRequestedLongitude!!, 0.001)
    }
}
