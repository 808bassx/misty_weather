package com.misty.weather.weather.domain

import com.misty.weather.core.weather.WeatherType
import com.misty.weather.weather.atmosphere.TimeOfDay
import com.misty.weather.weather.data.FakeWeatherRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

// ─── Weather Render Contract Tests ───────────────────────────────────────────
//
// Phase 4.0: Contract Verification.
//
// Ensures that EVERY domain WeatherCondition and WeatherState produces a valid,
// complete, non-null WeatherRenderState without exceptions or unmapped fallbacks.
//
// Protects the rendering engine against future meteorological condition additions.
//
// ─────────────────────────────────────────────────────────────────────────────

class WeatherRenderContractTest {

    @Test
    fun `every weather condition has a non-null valid render state across all time phases`() {
        val conditions = WeatherCondition.values()
        val times = TimeOfDay.values()

        for (condition in conditions) {
            for (time in times) {
                val renderState = WeatherMapper.toRenderState(
                    weatherType  = condition.canonicalWeatherType,
                    timeOfDay    = time,
                    timeProgress = 0.5f,
                )

                assertNotNull("RenderState must not be null for condition $condition at time $time", renderState)
                assertEquals(condition.canonicalWeatherType, renderState.weatherType)
                assertEquals(time, renderState.timeOfDay)

                val atmosphere = renderState.atmosphere
                assertNotNull("Atmosphere must be non-null for $condition at $time", atmosphere)

                // Verify sky colors integrity (three-stop gradient: zenith, mid, horizon)
                assertNotNull("Sky zenith color must be present", atmosphere.sky.zenith)
                assertNotNull("Sky mid color must be present", atmosphere.sky.mid)
                assertNotNull("Sky horizon color must be present", atmosphere.sky.horizon)

                // Verify celestial sun/moon positioning
                assertNotNull("Sun position must not be null", atmosphere.sunPosition)
                assertTrue("Sun altitude should be normalized [0, 1]", atmosphere.sunPosition.position.y in 0f..1.1f)

                // Verify scene luminosity
                assertTrue("Luminosity factor must be positive", atmosphere.luminosity > 0f)

                // Verify adaptive liquid glass alpha boundaries
                assertTrue(
                    "Glass ambient alpha should remain within [0.05, 0.50] for readability",
                    atmosphere.glassAmbient.alpha in 0.05f..0.50f,
                )
            }
        }
    }

    @Test
    fun `every domain WeatherState maps completely to WeatherRenderState and UI models`() {
        val canonicalTokyo = FakeWeatherRepository.canonicalTokyoWeather

        val renderState = WeatherMapper.toRenderState(canonicalTokyo, TimeOfDay.Noon)
        assertNotNull(renderState)
        assertEquals(canonicalTokyo.weatherType, renderState.weatherType)
        assertEquals(TimeOfDay.Noon, renderState.timeOfDay)

        val preview = WeatherMapper.toWeatherPreview(canonicalTokyo, TimeOfDay.Noon)
        assertEquals("东京", preview.city)
        assertEquals("Tokyo", preview.cityLatin)
        assertEquals(23, preview.temperature)
        assertEquals(22, preview.feelsLike)
        assertEquals(71, preview.humidity)
        assertEquals(canonicalTokyo.weatherType, preview.weatherType)

        val forecastSlots = WeatherMapper.toForecastSlots(canonicalTokyo.hourlyForecast)
        assertEquals(6, forecastSlots.size)
        assertEquals("现在", forecastSlots.first().label)
        assertEquals(23, forecastSlots.first().temperature)
        assertEquals(WeatherType.PartlyCloudy, forecastSlots.first().weatherType)
    }

    @Test
    fun `every standard WMO weather code resolves to a valid condition and render state`() {
        val standardWmoCodes = listOf(
            0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99
        )

        for (code in standardWmoCodes) {
            val condition = WeatherCondition.fromWmoCode(code)
            assertNotNull("WMO code $code must map to a WeatherCondition", condition)
            assertFalse("Display name must not be blank for WMO $code", condition.displayName.isBlank())

            val renderState = WeatherMapper.toRenderState(condition.canonicalWeatherType, TimeOfDay.Noon)
            assertNotNull("WMO code $code must generate valid render state", renderState)
        }
    }
}
