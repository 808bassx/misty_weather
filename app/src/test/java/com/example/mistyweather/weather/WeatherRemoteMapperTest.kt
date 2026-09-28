package com.example.mistyweather.weather

import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.weather.data.mapper.WeatherDtoMapper
import com.example.mistyweather.weather.data.remote.CurrentWeatherDto
import com.example.mistyweather.weather.data.remote.DailyWeatherDto
import com.example.mistyweather.weather.data.remote.HourlyWeatherDto
import com.example.mistyweather.weather.data.remote.WeatherApiClient
import com.example.mistyweather.weather.data.remote.WeatherResponseDto
import com.example.mistyweather.weather.domain.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

// ─── Weather Remote Mapper Tests ─────────────────────────────────────────────
//
// Phase 4.2: Architectural Firewall Verification.
// Ensures Open-Meteo REST DTOs are mapped to pure domain WeatherState with
// zero data leakage into rendering or UI layers.
//
// ─────────────────────────────────────────────────────────────────────────────

class WeatherRemoteMapperTest {

    @Test
    fun `WMO weather codes map accurately to domain WeatherCondition and canonical WeatherType`() {
        val testCases = listOf(
            0 to (WeatherCondition.ClearSky to WeatherType.Sunny),
            1 to (WeatherCondition.MainlyClear to WeatherType.PartlyCloudy),
            2 to (WeatherCondition.PartlyCloudy to WeatherType.PartlyCloudy),
            3 to (WeatherCondition.Overcast to WeatherType.Cloudy),
            45 to (WeatherCondition.Fog to WeatherType.Fog),
            48 to (WeatherCondition.Fog to WeatherType.Fog),
            51 to (WeatherCondition.Drizzle to WeatherType.Rain),
            53 to (WeatherCondition.Drizzle to WeatherType.Rain),
            61 to (WeatherCondition.ModerateRain to WeatherType.Rain),
            65 to (WeatherCondition.HeavyRain to WeatherType.Rain),
            71 to (WeatherCondition.LightSnow to WeatherType.Snow),
            75 to (WeatherCondition.LightSnow to WeatherType.Snow),
            95 to (WeatherCondition.Thunderstorm to WeatherType.Storm),
            96 to (WeatherCondition.SevereThunderstorm to WeatherType.Storm),
        )

        for ((wmoCode, expected) in testCases) {
            val condition = WeatherCondition.fromWmoCode(wmoCode)
            assertEquals("Condition mismatch for WMO code $wmoCode", expected.first, condition)
            assertEquals("WeatherType mismatch for WMO code $wmoCode", expected.second, condition.canonicalWeatherType)
        }
    }

    @Test
    fun `toDomainState correctly maps temperature, apparent temperature, and humidity`() {
        val dto = WeatherResponseDto(
            latitude = 35.6895,
            longitude = 139.6917,
            timezone = "Asia/Tokyo",
            current = CurrentWeatherDto(
                time = "2026-09-28T16:00",
                temperature2m = 22.6,
                relativeHumidity2m = 68,
                apparentTemperature = 21.2,
                weatherCode = 1,
            ),
            daily = DailyWeatherDto(
                sunrise = listOf("2026-09-28T05:33"),
                sunset = listOf("2026-09-28T17:31"),
            ),
        )

        val state = WeatherDtoMapper.toDomainState(dto, cityName = "东京", cityNameLatin = "Tokyo")

        assertEquals("东京", state.cityName)
        assertEquals("Tokyo", state.cityNameLatin)
        assertEquals(23, state.temperature) // 22.6 rounds to 23
        assertEquals(21, state.feelsLike)   // 21.2 rounds to 21
        assertEquals(68, state.humidity)
        assertEquals(WeatherCondition.MainlyClear, state.condition)
        assertEquals(WeatherType.PartlyCloudy, state.weatherType)
        assertEquals("05:33", state.sunriseTime)
        assertEquals("17:31", state.sunsetTime)
    }

    @Test
    fun `toDomainState handles null apparent temperature by falling back to temperature`() {
        val dto = WeatherResponseDto(
            current = CurrentWeatherDto(
                temperature2m = 18.0,
                apparentTemperature = null,
                weatherCode = 0,
            ),
        )

        val state = WeatherDtoMapper.toDomainState(dto)
        assertEquals(18, state.temperature)
        assertEquals(18, state.feelsLike)
    }

    @Test
    fun `hourly forecast slots map correctly and anchor the first slot as 现在`() {
        val dto = WeatherResponseDto(
            current = CurrentWeatherDto(
                time = "2026-09-28T15:00",
                temperature2m = 24.0,
                weatherCode = 2,
            ),
            hourly = HourlyWeatherDto(
                time = listOf(
                    "2026-09-28T14:00",
                    "2026-09-28T15:00",
                    "2026-09-28T16:00",
                    "2026-09-28T17:00",
                ),
                temperature2m = listOf(23.2, 24.1, 23.8, 21.9),
                weatherCode = listOf(1, 2, 2, 61),
            ),
        )

        val state = WeatherDtoMapper.toDomainState(dto)
        val slots = state.hourlyForecast

        assertTrue("Should have at least 3 slots", slots.size >= 3)
        // Since current time is 15:00, it anchors at index 1 ("15:00") and labels it "现在"
        assertEquals("现在", slots[0].timeLabel)
        assertEquals(24, slots[0].temperature)
        assertEquals(WeatherCondition.PartlyCloudy, slots[0].condition)

        assertEquals("16:00", slots[1].timeLabel)
        assertEquals(24, slots[1].temperature)

        assertEquals("17:00", slots[2].timeLabel)
        assertEquals(22, slots[2].temperature)
        assertEquals(WeatherCondition.ModerateRain, slots[2].condition)
    }

    @Test
    fun `extractTimePart handles ISO timestamps safely without API 26 dependencies`() {
        assertEquals("05:32", WeatherDtoMapper.extractTimePart("2026-09-28T05:32"))
        assertEquals("17:28", WeatherDtoMapper.extractTimePart("2026-09-28 17:28"))
        assertEquals("16:00", WeatherDtoMapper.extractTimePart("16:00"))
        assertEquals("08:00", WeatherDtoMapper.extractTimePart("2026-09-28T08:00:00Z"))
    }

    @Test
    fun `Real Open-Meteo sample JSON string deserializes and maps safely with unknown fields ignored`() {
        val sampleJson = """
            {
                "latitude": 35.6895,
                "longitude": 139.6917,
                "generationtime_ms": 0.054,
                "utc_offset_seconds": 32400,
                "timezone": "Asia/Tokyo",
                "timezone_abbreviation": "JST",
                "elevation": 40.0,
                "extra_unknown_field_123": "ignore_me",
                "current": {
                    "time": "2026-09-28T17:00",
                    "interval": 900,
                    "temperature_2m": 21.4,
                    "relative_humidity_2m": 72,
                    "apparent_temperature": 20.8,
                    "is_day": 0,
                    "precipitation": 0.0,
                    "weather_code": 95
                },
                "hourly": {
                    "time": ["2026-09-28T17:00", "2026-09-28T18:00"],
                    "temperature_2m": [21.4, 20.1],
                    "weather_code": [95, 95]
                },
                "daily": {
                    "time": ["2026-09-28"],
                    "sunrise": ["2026-09-28T05:33"],
                    "sunset": ["2026-09-28T17:31"]
                }
            }
        """.trimIndent()

        val dto = WeatherApiClient.json.decodeFromString<WeatherResponseDto>(sampleJson)
        val state = WeatherDtoMapper.toDomainState(dto)

        assertEquals(21, state.temperature)
        assertEquals(21, state.feelsLike)
        assertEquals(72, state.humidity)
        assertEquals(WeatherCondition.Thunderstorm, state.condition)
        assertEquals(WeatherType.Storm, state.weatherType)
        assertEquals("05:33", state.sunriseTime)
        assertEquals("17:31", state.sunsetTime)
        assertEquals("现在", state.hourlyForecast.first().timeLabel)
    }
}
