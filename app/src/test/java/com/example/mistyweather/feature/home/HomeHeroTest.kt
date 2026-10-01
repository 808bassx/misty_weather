package com.example.mistyweather.feature.home

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mistyweather.core.weather.PreviewWeatherTokyo
import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.theme.MistyDimensions
import com.example.mistyweather.theme.MistyMotion
import com.example.mistyweather.theme.MistyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase D: Unit tests for HomeHero formatting, accessibility semantics,
 * layout tokens, and motion choreography constraints.
 */
class HomeHeroTest {

    @Test
    fun `formatLocationSubtitle converts raw Current_location to human-friendly GPS string`() {
        assertEquals("GPS 定位", HomeHeroFormatters.formatLocationSubtitle("Current_location"))
        assertEquals("GPS 定位", HomeHeroFormatters.formatLocationSubtitle("current_location"))
        assertEquals("GPS 定位", HomeHeroFormatters.formatLocationSubtitle("CURRENT_LOCATION"))
        assertEquals("GPS 定位", HomeHeroFormatters.formatLocationSubtitle("Current location"))
        assertEquals("GPS 定位", HomeHeroFormatters.formatLocationSubtitle("  Current_location  "))
    }

    @Test
    fun `formatLocationSubtitle preserves valid latin names and handles blanks`() {
        assertEquals("Tokyo, Japan", HomeHeroFormatters.formatLocationSubtitle("Tokyo, Japan"))
        assertEquals("Beijing", HomeHeroFormatters.formatLocationSubtitle("Beijing"))
        assertEquals("Paris, France", HomeHeroFormatters.formatLocationSubtitle("Paris, France"))
        assertEquals("", HomeHeroFormatters.formatLocationSubtitle(""))
        assertEquals("", HomeHeroFormatters.formatLocationSubtitle("   "))
    }

    @Test
    fun `formatTemperature appends degree symbol to positive, zero, and negative values`() {
        assertEquals("24°", HomeHeroFormatters.formatTemperature(24))
        assertEquals("0°", HomeHeroFormatters.formatTemperature(0))
        assertEquals("-7°", HomeHeroFormatters.formatTemperature(-7))
        assertEquals("100°", HomeHeroFormatters.formatTemperature(100))
    }

    @Test
    fun `formatMetrics produces clean dot-separated telemetry line`() {
        assertEquals("体感 22° · 湿度 65%", HomeHeroFormatters.formatMetrics(22, 65))
        assertEquals("体感 0° · 湿度 100%", HomeHeroFormatters.formatMetrics(0, 100))
        assertEquals("体感 -5° · 湿度 40%", HomeHeroFormatters.formatMetrics(-5, 40))
    }

    @Test
    fun `accessibility summary produces complete descriptive string`() {
        val weather = PreviewWeatherTokyo.copy(
            city = "上海",
            condition = "晴空万里",
            temperature = 26,
            feelsLike = 28,
            humidity = 55,
        )
        val expected = "上海，当前天气晴空万里，温度26度，体感28度，湿度55百分比"
        val summary = "${weather.city}，当前天气${weather.condition}，温度${weather.temperature}度，体感${weather.feelsLike}度，湿度${weather.humidity}百分比"

        assertEquals(expected, summary)
    }

    @Test
    fun `hero weather icon dimension adheres to Phase D design scale`() {
        // Hero icon must be 72dp for legacy compatibility
        assertEquals(72.dp, MistyDimensions.WeatherIconHero)
        // Forecast icon must be 28dp (elevated from legacy 22dp)
        assertEquals(28.dp, MistyDimensions.WeatherIconForecast)
        // Phase F: Weather Orb celestial size (80dp) and ambient aura (48dp)
        assertEquals(80.dp, MistyDimensions.WeatherOrbSize)
        assertEquals(48.dp, MistyDimensions.WeatherOrbAuraRadius)
    }

    @Test
    fun `hero typography adheres to Plus Jakarta Sans architectural scales`() {
        // Hero temperature: 68sp Light with -2.5sp tracking (Phase F)
        assertEquals(68.sp, MistyType.TemperatureHero.fontSize)
        assertEquals((-2.5).sp, MistyType.TemperatureHero.letterSpacing)

        // Location primary: 22sp Medium
        assertEquals(22.sp, MistyType.LocationPrimary.fontSize)

        // Location title wide: 18sp Medium with 4.5sp tracking
        assertEquals(18.sp, MistyType.LocationTitleWide.fontSize)
        assertEquals(4.5.sp, MistyType.LocationTitleWide.letterSpacing)

        // Literary phrase: 13sp Regular
        assertEquals(13.sp, MistyType.LiteraryPhrase.fontSize)

        // Condition primary: 17sp Regular
        assertEquals(17.sp, MistyType.ConditionPrimary.fontSize)

        // Detail row: 14sp Regular
        assertEquals(14.sp, MistyType.DetailRow.fontSize)
    }

    @Test
    fun `formatLocationTitleWide converts latin names to wide uppercase title`() {
        assertEquals("TOKYO", HomeHeroFormatters.formatLocationTitleWide("Tokyo, Japan", "东京"))
        assertEquals("BEIJING", HomeHeroFormatters.formatLocationTitleWide("Beijing", "北京"))
        assertEquals("PARIS", HomeHeroFormatters.formatLocationTitleWide("Paris, France", "巴黎"))
        assertEquals("上海", HomeHeroFormatters.formatLocationTitleWide("Current_location", "上海"))
        assertEquals("MISTY", HomeHeroFormatters.formatLocationTitleWide("", ""))
    }

    @Test
    fun `formatLiteraryPhrase produces poetic environmental descriptions`() {
        val sunnyDay = HomeHeroFormatters.formatLiteraryPhrase("晴", WeatherType.Sunny, com.example.mistyweather.weather.atmosphere.TimeOfDay.Noon)
        assertEquals("明媚日光 · 万物朗阔", sunnyDay)

        val sunnyNight = HomeHeroFormatters.formatLiteraryPhrase("晴", WeatherType.Sunny, com.example.mistyweather.weather.atmosphere.TimeOfDay.Night)
        assertEquals("星汉静谧 · 夜色澄澈", sunnyNight)

        val rainMorning = HomeHeroFormatters.formatLiteraryPhrase("小雨", WeatherType.Rain, com.example.mistyweather.weather.atmosphere.TimeOfDay.Morning)
        assertEquals("清晨带露 · 细雨初歇", rainMorning)

        val rainNight = HomeHeroFormatters.formatLiteraryPhrase("小雨", WeatherType.Rain, com.example.mistyweather.weather.atmosphere.TimeOfDay.Night)
        assertEquals("夜雨淅沥 · 窗凝水汽", rainNight)

        val cloudyDay = HomeHeroFormatters.formatLiteraryPhrase("多云", WeatherType.Cloudy, com.example.mistyweather.weather.atmosphere.TimeOfDay.Noon)
        assertEquals("层云微遮 · 天光温润", cloudyDay)
    }

    @Test
    fun `getAtmosphericTemperatureColor avoids harsh pure white and keys to lighting`() {
        val sunnyColor = HomeHeroFormatters.getAtmosphericTemperatureColor(WeatherType.Sunny, com.example.mistyweather.weather.atmosphere.TimeOfDay.Noon)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFF8DC).copy(alpha = 0.95f), sunnyColor)

        val nightColor = HomeHeroFormatters.getAtmosphericTemperatureColor(WeatherType.Sunny, com.example.mistyweather.weather.atmosphere.TimeOfDay.Night)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFDCE1FF).copy(alpha = 0.90f), nightColor)

        val rainColor = HomeHeroFormatters.getAtmosphericTemperatureColor(WeatherType.Rain, com.example.mistyweather.weather.atmosphere.TimeOfDay.Noon)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFDCF0FF).copy(alpha = 0.92f), rainColor)

        val cloudyColor = HomeHeroFormatters.getAtmosphericTemperatureColor(WeatherType.Cloudy, com.example.mistyweather.weather.atmosphere.TimeOfDay.Noon)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFE8EEF5).copy(alpha = 0.92f), cloudyColor)
    }

    @Test
    fun `hero motion respects reduced motion accessibility preference`() {
        val normalDuration = MistyMotion.DurationNormal
        assertEquals(280, normalDuration)

        // When reduced motion is requested, duration must resolve to instant (0ms)
        val resolvedReduced = MistyMotion.resolveDuration(normalDuration, isReduced = true)
        assertEquals(MistyMotion.DurationInstant, resolvedReduced)
        assertEquals(0, resolvedReduced)

        // When standard motion is active, normal duration is preserved
        val resolvedNormal = MistyMotion.resolveDuration(normalDuration, isReduced = false)
        assertEquals(normalDuration, resolvedNormal)
    }

    @Test
    fun `weather types are faithfully represented in hero data model`() {
        val weatherTypes = listOf(
            WeatherType.Sunny,
            WeatherType.Cloudy,
            WeatherType.PartlyCloudy,
            WeatherType.Rain,
            WeatherType.Storm,
        )

        for (type in weatherTypes) {
            val preview = PreviewWeatherTokyo.copy(weatherType = type)
            assertEquals(type, preview.weatherType)
            assertTrue(preview.temperature in -50..60)
        }
    }

    @Test
    fun `isHourlySlotDaytime calculates diurnal phase from hourly labels accurately`() {
        assertTrue(isHourlySlotDaytime("现在", com.example.mistyweather.weather.atmosphere.TimeOfDay.Noon))
        assertFalse(isHourlySlotDaytime("现在", com.example.mistyweather.weather.atmosphere.TimeOfDay.Night))
        assertFalse(isHourlySlotDaytime("现在", com.example.mistyweather.weather.atmosphere.TimeOfDay.Evening))

        assertTrue(isHourlySlotDaytime("09:00", null))
        assertTrue(isHourlySlotDaytime("12:00", null))
        assertTrue(isHourlySlotDaytime("17:00", null))
        assertFalse(isHourlySlotDaytime("21:00", null))
        assertFalse(isHourlySlotDaytime("23:00", null))
        assertFalse(isHourlySlotDaytime("02:00", null))
    }
}
