package com.misty.weather.weather.domain

import com.misty.weather.core.weather.WeatherType
import com.misty.weather.weather.atmosphere.TimeOfDay
import com.misty.weather.weather.data.FakeWeatherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

// ─── Phase 3.6 Unit Tests: Domain Layer & Architecture Hardening ───────────────

class WeatherDomainTest {

    // ── 1. WeatherCondition WMO Code Mapping Tests ────────────────────────────

    @Test
    fun `WeatherCondition resolves standard WMO codes to domain and weather types`() {
        assertEquals(WeatherCondition.ClearSky, WeatherCondition.fromWmoCode(0))
        assertEquals(WeatherType.Sunny, WeatherCondition.fromWmoCode(0).canonicalWeatherType)

        assertEquals(WeatherCondition.MainlyClear, WeatherCondition.fromWmoCode(1))
        assertEquals(WeatherType.PartlyCloudy, WeatherCondition.fromWmoCode(1).canonicalWeatherType)

        assertEquals(WeatherCondition.PartlyCloudy, WeatherCondition.fromWmoCode(2))
        assertEquals(WeatherCondition.Overcast, WeatherCondition.fromWmoCode(3))
        assertEquals(WeatherType.Cloudy, WeatherCondition.fromWmoCode(3).canonicalWeatherType)

        assertEquals(WeatherCondition.Fog, WeatherCondition.fromWmoCode(45))
        assertEquals(WeatherType.Fog, WeatherCondition.fromWmoCode(45).canonicalWeatherType)

        assertEquals(WeatherCondition.ModerateRain, WeatherCondition.fromWmoCode(61))
        assertEquals(WeatherType.Rain, WeatherCondition.fromWmoCode(61).canonicalWeatherType)

        assertEquals(WeatherCondition.LightSnow, WeatherCondition.fromWmoCode(71))
        assertEquals(WeatherType.Snow, WeatherCondition.fromWmoCode(71).canonicalWeatherType)

        assertEquals(WeatherCondition.Thunderstorm, WeatherCondition.fromWmoCode(95))
        assertEquals(WeatherType.Storm, WeatherCondition.fromWmoCode(95).canonicalWeatherType)
    }

    // ── 2. WeatherMapper Frozen Render State Tests ────────────────────────────

    @Test
    fun `WeatherMapper creates valid WeatherRenderState without leaking data layer`() {
        val renderState = WeatherMapper.toRenderState(
            weatherType  = WeatherType.Sunny,
            timeOfDay    = TimeOfDay.Sunset,
            timeProgress = 0.5f,
        )

        assertEquals(WeatherType.Sunny, renderState.weatherType)
        assertEquals(TimeOfDay.Sunset, renderState.timeOfDay)
        assertEquals(0.5f, renderState.timeProgress, 0.001f)
        assertNotNull(renderState.atmosphere)
        assertTrue("Sun should be setting in the western sky", renderState.atmosphere.sunPosition.position.x > 0.60f)
        assertTrue("Glass ambient should have subtle transparency", renderState.atmosphere.glassAmbient.alpha in 0.10f..0.35f)
    }

    @Test
    fun `WeatherMapper toWeatherPreview maps domain WeatherState to UI preview model`() {
        val domainState = WeatherState(
            cityName      = "东京",
            cityNameLatin = "Tokyo",
            temperature   = 23,
            feelsLike     = 22,
            humidity      = 71,
            condition     = WeatherCondition.PartlyCloudy,
        )

        val preview = WeatherMapper.toWeatherPreview(domainState, TimeOfDay.Noon)
        assertEquals("东京", preview.city)
        assertEquals("Tokyo", preview.cityLatin)
        assertEquals(23, preview.temperature)
        assertEquals("多云", preview.condition)
        assertEquals(22, preview.feelsLike)
        assertEquals(71, preview.humidity)
        assertEquals(WeatherType.PartlyCloudy, preview.weatherType)
        assertEquals(TimeOfDay.Noon, preview.timeOfDay)
    }

    // ── 3. FakeWeatherRepository Architecture Verification ────────────────────

    @Test
    fun `FakeWeatherRepository emits canonical Tokyo data`() = runTest {
        val repository = FakeWeatherRepository()
        val state = repository.getWeather(35.6895, 139.6917, "东京", "Tokyo").first()

        assertEquals("东京", state.cityName)
        assertEquals("Tokyo", state.cityNameLatin)
        assertEquals(23, state.temperature)
        assertEquals("05:32", state.sunriseTime)
        assertEquals("17:28", state.sunsetTime)
        assertTrue("Hourly forecast should contain multiple slots", state.hourlyForecast.isNotEmpty())
    }

    @Test
    fun `FakeWeatherRepository updates reactive state seamlessly`() = runTest {
        val repository = FakeWeatherRepository()
        val newState = FakeWeatherRepository.canonicalTokyoWeather.copy(
            temperature = 18,
            condition   = WeatherCondition.HeavyRain,
            weatherType = WeatherType.Rain,
        )

        repository.updateWeather(newState)
        val updatedState = repository.getWeather(35.6895, 139.6917, "东京", "Tokyo").first()

        assertEquals(18, updatedState.temperature)
        assertEquals(WeatherCondition.HeavyRain, updatedState.condition)
        assertEquals(WeatherType.Rain, updatedState.weatherType)
    }
}
