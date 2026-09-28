package com.example.mistyweather.weather

import com.example.mistyweather.weather.data.remote.CurrentWeatherDto
import com.example.mistyweather.weather.data.remote.WeatherApiClient
import com.example.mistyweather.weather.data.remote.WeatherResponseDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

// ─── Weather API & Serializer Tests ──────────────────────────────────────────
//
// Phase 4.2: Open-Meteo API Client & JSON Deserializer Verification.
//
// ─────────────────────────────────────────────────────────────────────────────

class WeatherApiTest {

    @Test
    fun `WeatherApiClient creates valid Retrofit API client`() {
        val client = WeatherApiClient.create()
        assertNotNull("WeatherApi instance should be created", client)
        assertEquals("https://api.open-meteo.com/", WeatherApiClient.BASE_URL)
    }

    @Test
    fun `Json deserializer safely parses minimal Open-Meteo JSON payload with defaults`() {
        val minimalJson = """
            {
                "current": {
                    "temperature_2m": 20.5
                }
            }
        """.trimIndent()

        val dto = WeatherApiClient.json.decodeFromString<WeatherResponseDto>(minimalJson)

        assertEquals(20.5, dto.current.temperature2m, 0.01)
        assertEquals(0, dto.current.relativeHumidity2m)
        assertNull(dto.current.apparentTemperature)
        assertEquals(0, dto.current.weatherCode)
        assertNull(dto.hourly)
        assertNull(dto.daily)
    }

    @Test
    fun `Json deserializer tolerates unexpected external schema additions without error`() {
        val futureProofJson = """
            {
                "latitude": 35.68,
                "longitude": 139.69,
                "new_open_meteo_model": "gemini-atmosphere-v2",
                "experimental_features": [1, 2, 3],
                "current": {
                    "time": "2026-09-28T18:00",
                    "temperature_2m": 19.8,
                    "relative_humidity_2m": 80,
                    "apparent_temperature": 19.2,
                    "weather_code": 3,
                    "uv_index": 0.0,
                    "cloud_cover": 95
                }
            }
        """.trimIndent()

        val dto = WeatherApiClient.json.decodeFromString<WeatherResponseDto>(futureProofJson)

        assertEquals(19.8, dto.current.temperature2m, 0.01)
        assertEquals(80, dto.current.relativeHumidity2m)
        assertEquals(19.2, dto.current.apparentTemperature!!, 0.01)
        assertEquals(3, dto.current.weatherCode)
    }
}
