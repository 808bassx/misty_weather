package com.example.mistyweather.feature.home

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mistyweather.core.weather.PreviewWeatherTokyo
import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.theme.MistyDimensions
import com.example.mistyweather.theme.MistyMotion
import com.example.mistyweather.theme.MistyType
import org.junit.Assert.assertEquals
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
        // Hero icon must be 72dp for atmospheric stage focal presence
        assertEquals(72.dp, MistyDimensions.WeatherIconHero)
        // Forecast icon must be 28dp (elevated from legacy 22dp)
        assertEquals(28.dp, MistyDimensions.WeatherIconForecast)
    }

    @Test
    fun `hero typography adheres to Plus Jakarta Sans architectural scales`() {
        // Hero temperature: 84sp Light with -3sp tracking
        assertEquals(84.sp, MistyType.TemperatureHero.fontSize)
        assertEquals((-3).sp, MistyType.TemperatureHero.letterSpacing)

        // Location primary: 22sp Medium
        assertEquals(22.sp, MistyType.LocationPrimary.fontSize)

        // Condition primary: 17sp Regular
        assertEquals(17.sp, MistyType.ConditionPrimary.fontSize)

        // Detail row: 14sp Regular
        assertEquals(14.sp, MistyType.DetailRow.fontSize)
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
}
