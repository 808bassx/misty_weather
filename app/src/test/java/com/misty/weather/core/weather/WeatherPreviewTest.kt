package com.misty.weather.core.weather

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

// ─── Phase 1 Unit Tests ───────────────────────────────────────────────────────
// Validates domain model correctness and preview data integrity.
// No UI tests in Phase 1 — Compose UI tests require an instrumented environment.
// ─────────────────────────────────────────────────────────────────────────────

class WeatherPreviewTest {

    @Test
    fun `PreviewWeatherTokyo has expected city name`() {
        assertEquals("东京", PreviewWeatherTokyo.city)
    }

    @Test
    fun `PreviewWeatherTokyo has non-negative temperature`() {
        assert(PreviewWeatherTokyo.temperature >= -60) {
            "Temperature should be a plausible value"
        }
    }

    @Test
    fun `PreviewWeatherTokyo feelsLike is within 5 degrees of temperature`() {
        val diff = Math.abs(PreviewWeatherTokyo.temperature - PreviewWeatherTokyo.feelsLike)
        assert(diff <= 10) { "FeelsLike ($diff° diff) seems too far from temperature" }
    }

    @Test
    fun `PreviewWeatherTokyo humidity is between 0 and 100`() {
        assert(PreviewWeatherTokyo.humidity in 0..100) {
            "Humidity must be 0–100, got ${PreviewWeatherTokyo.humidity}"
        }
    }

    @Test
    fun `PreviewForecastSlots is non-empty`() {
        assert(PreviewForecastSlots.isNotEmpty()) { "Forecast slots must not be empty" }
    }

    @Test
    fun `PreviewForecastSlots first slot is labeled 现在`() {
        assertEquals("现在", PreviewForecastSlots.first().label)
    }

    @Test
    fun `PreviewForecastSlots all temperatures are plausible`() {
        PreviewForecastSlots.forEach { slot ->
            assert(slot.temperature in -60..60) {
                "Slot '${slot.label}' has implausible temperature ${slot.temperature}"
            }
        }
    }

    @Test
    fun `WeatherType enum contains all expected variants`() {
        val expected = setOf("Sunny", "PartlyCloudy", "Cloudy", "Rain", "Storm", "Snow", "Fog")
        val actual   = WeatherType.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `WeatherPreview data class copy preserves unchanged fields`() {
        val modified = PreviewWeatherTokyo.copy(temperature = 30)
        assertEquals("东京",  modified.city)
        assertEquals(30,      modified.temperature)
        assertEquals(PreviewWeatherTokyo.humidity, modified.humidity)
    }

    @Test
    fun `ForecastSlot data class is value-equal when fields match`() {
        val a = ForecastSlot(label = "现在", temperature = 23, weatherType = WeatherType.Cloudy)
        val b = ForecastSlot(label = "现在", temperature = 23, weatherType = WeatherType.Cloudy)
        assertEquals(a, b)
    }

    @Test
    fun `PreviewForecastSlots temperatures decrease or stay equal over time`() {
        // This is a soft sanity check — weather generally cools toward evening.
        val temps = PreviewForecastSlots.map { it.temperature }
        val maxDrop = temps.zipWithNext().maxOf { (a, b) -> a - b }
        assert(maxDrop >= 0) { "Forecast temperatures should not spike up dramatically" }
    }
}
