package com.example.mistyweather.weather.icon

import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.weather.domain.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// ─── WeatherIconKey Unit Tests ───────────────────────────────────────────────
// Phase 4.4-B0: Validates semantic key resolution from conditions, types, and serial names.
// ─────────────────────────────────────────────────────────────────────────────

class WeatherIconKeyTest {

    // ── fromSerialName ────────────────────────────────────────────────────────

    @Test
    fun fromSerialName_exactMatch_returnsCorrectKey() {
        assertEquals(WeatherIconKey.CLEAR_DAY, WeatherIconKey.fromSerialName("clear_day"))
        assertEquals(WeatherIconKey.RAIN, WeatherIconKey.fromSerialName("rain"))
        assertEquals(WeatherIconKey.SNOW, WeatherIconKey.fromSerialName("snow"))
        assertEquals(WeatherIconKey.THUNDERSTORM, WeatherIconKey.fromSerialName("thunderstorm"))
        assertEquals(WeatherIconKey.FOG, WeatherIconKey.fromSerialName("fog"))
    }

    @Test
    fun fromSerialName_uppercase_returnsCorrectKey() {
        assertEquals(WeatherIconKey.CLEAR_DAY, WeatherIconKey.fromSerialName("CLEAR_DAY"))
        assertEquals(WeatherIconKey.RAIN, WeatherIconKey.fromSerialName("RAIN"))
    }

    @Test
    fun fromSerialName_withWhitespace_returnsCorrectKey() {
        assertEquals(WeatherIconKey.SNOW, WeatherIconKey.fromSerialName("  snow  "))
    }

    @Test
    fun fromSerialName_withDash_treatedAsUnderscore() {
        // Dashes are normalized to underscores
        assertEquals(WeatherIconKey.CLEAR_DAY, WeatherIconKey.fromSerialName("clear-day"))
        assertEquals(WeatherIconKey.PARTLY_CLOUDY_DAY, WeatherIconKey.fromSerialName("partly-cloudy-day"))
    }

    @Test
    fun fromSerialName_unknownString_returnsUNKNOWN() {
        assertEquals(WeatherIconKey.UNKNOWN, WeatherIconKey.fromSerialName("hail"))
        assertEquals(WeatherIconKey.UNKNOWN, WeatherIconKey.fromSerialName(""))
        assertEquals(WeatherIconKey.UNKNOWN, WeatherIconKey.fromSerialName("tornado"))
    }

    // ── fromCondition ─────────────────────────────────────────────────────────

    @Test
    fun fromCondition_clearSky_daytime_returnsClearDay() {
        assertEquals(WeatherIconKey.CLEAR_DAY, WeatherIconKey.fromCondition(WeatherCondition.ClearSky, true))
    }

    @Test
    fun fromCondition_clearSky_nighttime_returnsClearNight() {
        assertEquals(WeatherIconKey.CLEAR_NIGHT, WeatherIconKey.fromCondition(WeatherCondition.ClearSky, false))
    }

    @Test
    fun fromCondition_partlyCloudy_daytime_returnsPartlyCloudyDay() {
        assertEquals(WeatherIconKey.PARTLY_CLOUDY_DAY, WeatherIconKey.fromCondition(WeatherCondition.PartlyCloudy, true))
    }

    @Test
    fun fromCondition_partlyCloudy_nighttime_returnsPartlyCloudyNight() {
        assertEquals(WeatherIconKey.PARTLY_CLOUDY_NIGHT, WeatherIconKey.fromCondition(WeatherCondition.PartlyCloudy, false))
    }

    @Test
    fun fromCondition_mainlyClear_daytime_returnsPartlyCloudyDay() {
        assertEquals(WeatherIconKey.PARTLY_CLOUDY_DAY, WeatherIconKey.fromCondition(WeatherCondition.MainlyClear, true))
    }

    @Test
    fun fromCondition_overcast_returnsSameRegardlessOfDaytime() {
        assertEquals(WeatherIconKey.OVERCAST, WeatherIconKey.fromCondition(WeatherCondition.Overcast, true))
        assertEquals(WeatherIconKey.OVERCAST, WeatherIconKey.fromCondition(WeatherCondition.Overcast, false))
    }

    @Test
    fun fromCondition_fog_returnsCorrectKey() {
        assertEquals(WeatherIconKey.FOG, WeatherIconKey.fromCondition(WeatherCondition.Fog, true))
        assertEquals(WeatherIconKey.FOG, WeatherIconKey.fromCondition(WeatherCondition.Fog, false))
    }

    @Test
    fun fromCondition_rain_returnsRain() {
        assertEquals(WeatherIconKey.RAIN, WeatherIconKey.fromCondition(WeatherCondition.ModerateRain, true))
    }

    @Test
    fun fromCondition_heavyRain_returnsHeavyRain() {
        assertEquals(WeatherIconKey.HEAVY_RAIN, WeatherIconKey.fromCondition(WeatherCondition.HeavyRain, true))
    }

    @Test
    fun fromCondition_thunderstorm_returnsThunderstorm() {
        assertEquals(WeatherIconKey.THUNDERSTORM, WeatherIconKey.fromCondition(WeatherCondition.Thunderstorm, true))
        assertEquals(WeatherIconKey.THUNDERSTORM, WeatherIconKey.fromCondition(WeatherCondition.SevereThunderstorm, true))
    }

    @Test
    fun fromCondition_snow_returnsSnow() {
        assertEquals(WeatherIconKey.SNOW, WeatherIconKey.fromCondition(WeatherCondition.LightSnow, true))
        assertEquals(WeatherIconKey.SNOW, WeatherIconKey.fromCondition(WeatherCondition.HeavySnow, true))
    }

    // ── fromWeatherType ───────────────────────────────────────────────────────

    @Test
    fun fromWeatherType_sunny_daytime_returnsClearDay() {
        assertEquals(WeatherIconKey.CLEAR_DAY, WeatherIconKey.fromWeatherType(WeatherType.Sunny, true))
    }

    @Test
    fun fromWeatherType_sunny_nighttime_returnsClearNight() {
        assertEquals(WeatherIconKey.CLEAR_NIGHT, WeatherIconKey.fromWeatherType(WeatherType.Sunny, false))
    }

    @Test
    fun fromWeatherType_rain_returnsRain() {
        assertEquals(WeatherIconKey.RAIN, WeatherIconKey.fromWeatherType(WeatherType.Rain))
    }

    @Test
    fun fromWeatherType_storm_returnsThunderstorm() {
        assertEquals(WeatherIconKey.THUNDERSTORM, WeatherIconKey.fromWeatherType(WeatherType.Storm))
    }

    @Test
    fun fromWeatherType_snow_returnsSnow() {
        assertEquals(WeatherIconKey.SNOW, WeatherIconKey.fromWeatherType(WeatherType.Snow))
    }

    @Test
    fun fromWeatherType_fog_returnsFog() {
        assertEquals(WeatherIconKey.FOG, WeatherIconKey.fromWeatherType(WeatherType.Fog))
    }

    @Test
    fun fromWeatherType_cloudy_returnsCloudy() {
        assertEquals(WeatherIconKey.CLOUDY, WeatherIconKey.fromWeatherType(WeatherType.Cloudy))
    }

    // ── requiredKeys ──────────────────────────────────────────────────────────

    @Test
    fun requiredKeys_doesNotContainUNKNOWN() {
        assertTrue("UNKNOWN must not be in requiredKeys", WeatherIconKey.UNKNOWN !in WeatherIconKey.requiredKeys)
    }

    @Test
    fun requiredKeys_containsAll14RequiredEntries() {
        assertEquals(14, WeatherIconKey.requiredKeys.size)
    }

    @Test
    fun allEntries_size_is15() {
        assertEquals(15, WeatherIconKey.entries.size)
    }

    // ── serialName uniqueness ─────────────────────────────────────────────────

    @Test
    fun serialNames_areAllUnique() {
        val names = WeatherIconKey.entries.map { it.serialName }
        assertEquals("Serial names must all be unique", names.size, names.toSet().size)
    }

    @Test
    fun serialNames_areAllLowercase() {
        WeatherIconKey.entries.forEach { key ->
            assertEquals(
                "Serial name for $key must be lowercase",
                key.serialName,
                key.serialName.lowercase(),
            )
        }
    }
}
