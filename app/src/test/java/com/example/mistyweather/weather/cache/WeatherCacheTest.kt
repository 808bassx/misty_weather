package com.example.mistyweather.weather.cache

import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import com.example.mistyweather.weather.data.FakeWeatherRepository
import com.example.mistyweather.weather.domain.WeatherCondition
import com.example.mistyweather.weather.domain.WeatherMapper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

// ─── Weather Cache & Persistence Tests ─────────────────────────────────────────
//
// Phase 4.1.5: Persistent Offline Weather Cache Unit Tests.
//
// ─────────────────────────────────────────────────────────────────────────────

class WeatherCacheTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults    = true
    }

    @Test
    fun `InMemoryWeatherCache saves, loads, and clears cached weather snapshot`() = runTest {
        val cache = InMemoryWeatherCache()
        assertNull("Initial cache should be empty", cache.load())

        val domainState = FakeWeatherRepository.canonicalTokyoWeather
        val renderState = WeatherMapper.toRenderState(domainState, TimeOfDay.Noon)
        val snapshot = CachedWeather(
            location     = "tokyo",
            weatherState = domainState,
            renderState  = renderState,
            timestamp    = 1727539200000L,
        )

        cache.save(snapshot)
        val loaded = cache.load()

        assertNotNull("Loaded cache should not be null", loaded)
        assertEquals("tokyo", loaded?.location)
        assertEquals(23, loaded?.weatherState?.temperature)
        assertEquals(WeatherType.PartlyCloudy, loaded?.renderState?.weatherType)
        assertEquals(1727539200000L, loaded?.timestamp)

        cache.clear()
        assertNull("Cache should be null after clear", cache.load())
    }

    @Test
    fun `CachedWeatherDto JSON serialization roundtrip preserves complete domain and render state`() {
        val domainState = FakeWeatherRepository.canonicalTokyoWeather.copy(
            cityName      = "巴黎",
            cityNameLatin = "Paris",
            temperature   = 18,
            condition     = WeatherCondition.ModerateRain,
            weatherType   = WeatherType.Rain,
        )
        val renderState = WeatherMapper.toRenderState(domainState, TimeOfDay.Sunset)
        val originalSnapshot = CachedWeather(
            location     = "paris",
            weatherState = domainState,
            renderState  = renderState,
            timestamp    = 1727540000000L,
        )

        val dto = CachedWeatherDto.fromCachedWeather(originalSnapshot)
        val serializedJson = json.encodeToString(dto)

        val deserializedDto = json.decodeFromString<CachedWeatherDto>(serializedJson)
        val reconstructed = deserializedDto.toCachedWeather()

        assertEquals(originalSnapshot.location, reconstructed.location)
        assertEquals("巴黎", reconstructed.weatherState.cityName)
        assertEquals("Paris", reconstructed.weatherState.cityNameLatin)
        assertEquals(18, reconstructed.weatherState.temperature)
        assertEquals(WeatherType.Rain, reconstructed.weatherState.weatherType)
        assertEquals(WeatherType.Rain, reconstructed.renderState.weatherType)
        assertEquals(TimeOfDay.Sunset, reconstructed.renderState.timeOfDay)
        assertEquals(originalSnapshot.timestamp, reconstructed.timestamp)
    }
}
