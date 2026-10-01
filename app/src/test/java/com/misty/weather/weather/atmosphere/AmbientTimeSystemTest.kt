package com.misty.weather.weather.atmosphere

import com.misty.weather.core.weather.WeatherType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

// ─── Phase 3 Unit Tests: Ambient Time & Atmosphere System ─────────────────────

class AmbientTimeSystemTest {

    // ── 1. TimeOfDay Canonical Boundary Tests ─────────────────────────────────

    @Test
    fun `TimeOfDay from maps hours according to canonical specification`() {
        // 05:00 ───── Dawn
        assertEquals(TimeOfDay.Dawn, TimeOfDay.from(LocalTime.of(5, 0)))
        assertEquals(TimeOfDay.Dawn, TimeOfDay.from(LocalTime.of(6, 0)))
        assertEquals(TimeOfDay.Dawn, TimeOfDay.from(LocalTime.of(6, 29)))

        // 06:30 ───── Morning
        assertEquals(TimeOfDay.Morning, TimeOfDay.from(LocalTime.of(6, 30)))
        assertEquals(TimeOfDay.Morning, TimeOfDay.from(LocalTime.of(9, 0)))
        assertEquals(TimeOfDay.Morning, TimeOfDay.from(LocalTime.of(10, 59)))

        // 11:00 ───── Noon
        assertEquals(TimeOfDay.Noon, TimeOfDay.from(LocalTime.of(11, 0)))
        assertEquals(TimeOfDay.Noon, TimeOfDay.from(LocalTime.of(14, 0)))
        assertEquals(TimeOfDay.Noon, TimeOfDay.from(LocalTime.of(17, 29)))

        // 17:30 ───── Sunset
        assertEquals(TimeOfDay.Sunset, TimeOfDay.from(LocalTime.of(17, 30)))
        assertEquals(TimeOfDay.Sunset, TimeOfDay.from(LocalTime.of(18, 15)))
        assertEquals(TimeOfDay.Sunset, TimeOfDay.from(LocalTime.of(18, 59)))

        // 19:00 ───── Evening
        assertEquals(TimeOfDay.Evening, TimeOfDay.from(LocalTime.of(19, 0)))
        assertEquals(TimeOfDay.Evening, TimeOfDay.from(LocalTime.of(20, 0)))
        assertEquals(TimeOfDay.Evening, TimeOfDay.from(LocalTime.of(20, 59)))

        // 21:00 ───── Night
        assertEquals(TimeOfDay.Night, TimeOfDay.from(LocalTime.of(21, 0)))
        assertEquals(TimeOfDay.Night, TimeOfDay.from(LocalTime.of(23, 45)))
        assertEquals(TimeOfDay.Night, TimeOfDay.from(LocalTime.of(1, 30)))
        assertEquals(TimeOfDay.Night, TimeOfDay.from(LocalTime.of(4, 59)))
    }

    @Test
    fun `TimeOfDay progressWithin returns normalized 0 to 1 values`() {
        val testTimes = listOf(
            LocalTime.of(5, 0), LocalTime.of(5, 45), LocalTime.of(6, 29),
            LocalTime.of(6, 30), LocalTime.of(8, 45), LocalTime.of(10, 59),
            LocalTime.of(11, 0), LocalTime.of(14, 15), LocalTime.of(17, 29),
            LocalTime.of(17, 30), LocalTime.of(18, 15), LocalTime.of(18, 59),
            LocalTime.of(19, 0), LocalTime.of(20, 0), LocalTime.of(20, 59),
            LocalTime.of(21, 0), LocalTime.of(1, 0), LocalTime.of(4, 59),
        )

        testTimes.forEach { time ->
            val progress = TimeOfDay.progressWithin(time)
            assertTrue("Progress at $time should be in [0, 1], got $progress", progress in 0f..1f)
        }
    }

    @Test
    fun `TimeOfDay cycle completes in 6 steps`() {
        var current = TimeOfDay.Dawn
        val cycle = mutableListOf(current)
        repeat(6) {
            current = with(TimeOfDay.Companion) { current.next() }
            cycle.add(current)
        }
        assertEquals(
            listOf(
                TimeOfDay.Dawn,
                TimeOfDay.Morning,
                TimeOfDay.Noon,
                TimeOfDay.Sunset,
                TimeOfDay.Evening,
                TimeOfDay.Night,
                TimeOfDay.Dawn,
            ),
            cycle,
        )
    }

    // ── 2. SunPosition Trajectory & Visibility Tests ─────────────────────────

    @Test
    fun `SunPosition at Noon reaches highest elevation and centered position`() {
        val sunNoon = computeSunPosition(TimeOfDay.Noon, 0.5f)
        assertTrue("Sun must be visible at Noon", sunNoon.visible)
        assertTrue("Sun elevation at Noon should be highest (> 0.95)", sunNoon.elevation > 0.95f)
        assertTrue("Sun Y at Noon should be near top (< 0.15)", sunNoon.position.y < 0.15f)
        assertTrue("Sun X at Noon should be near center (~0.5)", sunNoon.position.x in 0.40f..0.60f)
        assertTrue("Sun disc alpha at Noon should be high (> 0.9)", sunNoon.discAlpha > 0.9f)
    }

    @Test
    fun `SunPosition at Dawn and Sunset follows continuous descent towards horizon`() {
        val sunDawn = computeSunPosition(TimeOfDay.Dawn, 0.0f)
        val sunSunset = computeSunPosition(TimeOfDay.Sunset, 1.0f)
        val sunEvening = computeSunPosition(TimeOfDay.Evening, 1.0f)

        assertTrue("Sun X at Dawn start should be east (< 0.20)", sunDawn.position.x < 0.20f)
        assertTrue("Sun Y at Dawn start should be low near horizon (> 0.80)", sunDawn.position.y > 0.80f)

        assertTrue("Sun X at Sunset end should be west (> 0.70)", sunSunset.position.x > 0.70f)
        assertTrue("Sun Y at Sunset end should be lower than Noon (> 0.50)", sunSunset.position.y > 0.50f)

        assertTrue("Sun X at Evening end should be far west (> 0.85)", sunEvening.position.x > 0.85f)
        assertTrue("Sun Y at Evening end should be near horizon (> 0.75)", sunEvening.position.y > 0.75f)
    }

    @Test
    fun `SunPosition at Night is invisible with zero discAlpha`() {
        val sunNight = computeSunPosition(TimeOfDay.Night, 0.5f)
        assertFalse("Sun must not be visible at Night", sunNight.visible)
        assertEquals(0f, sunNight.discAlpha, 0.001f)
    }

    // ── 3. ColorTemperature Tests ─────────────────────────────────────────────

    @Test
    fun `ColorTemperatures have valid parameters and high glass transparency`() {
        val phases = listOf(
            TimeOfDay.Dawn,
            TimeOfDay.Morning,
            TimeOfDay.Noon,
            TimeOfDay.Sunset,
            TimeOfDay.Evening,
            TimeOfDay.Night,
        )

        phases.forEach { phase ->
            val temp = ColorTemperatures.forTimeOfDay(phase)
            assertTrue("Luminosity for $phase should be in [0.4, 1.0]", temp.luminosity in 0.4f..1.0f)
            assertTrue("Star opacity for $phase should be in [0, 1]", temp.starOpacity in 0f..1f)
            // Strict check: Glass tint must be subtle & translucent (alpha <= 0.40)
            assertTrue("Glass tint alpha for $phase must maintain high transparency", temp.glassAmbient.alpha <= 0.40f)
        }
    }

    // ── 4. Weather × Time Composition Tests ───────────────────────────────────

    @Test
    fun `Sunny combinations produce valid atmospheric environments`() {
        val sunnyMorning = computeAmbientAtmosphere(WeatherType.Sunny, TimeOfDay.Morning)
        val sunnyNoon    = computeAmbientAtmosphere(WeatherType.Sunny, TimeOfDay.Noon)
        val sunnySunset  = computeAmbientAtmosphere(WeatherType.Sunny, TimeOfDay.Sunset)
        val sunnyNight   = computeAmbientAtmosphere(WeatherType.Sunny, TimeOfDay.Night)

        // Sunny Noon: Maximum luminosity, sun visible
        assertEquals(1.0f, sunnyNoon.luminosity, 0.01f)
        assertTrue(sunnyNoon.light.sunVisible)
        assertEquals(0f, sunnyNoon.starOpacity, 0.001f)

        // Sunny Sunset: Sun visible, warmer glow, medium luminosity
        assertTrue(sunnySunset.light.sunVisible)
        assertTrue(sunnySunset.luminosity < sunnyNoon.luminosity)

        // Sunny Night: Sun invisible, stars visible
        assertFalse(sunnyNight.light.sunVisible)
        assertTrue("Clear night must have visible stars", sunnyNight.starOpacity > 0.5f)
        assertTrue("Night luminosity must be lower than day", sunnyNight.luminosity < 0.6f)
    }

    @Test
    fun `Rain combinations adjust particle visibility and cloud depth`() {
        val rainMorning = computeAmbientAtmosphere(WeatherType.Rain, TimeOfDay.Morning)
        val rainEvening = computeAmbientAtmosphere(WeatherType.Rain, TimeOfDay.Evening)
        val rainNight   = computeAmbientAtmosphere(WeatherType.Rain, TimeOfDay.Night)

        // Rain always has rain particles
        assertNotNull(rainMorning.particles)
        assertNotNull(rainEvening.particles)
        assertNotNull(rainNight.particles)

        // Rain has heavy clouds obscuring stars even at night
        assertTrue("Rain night clouds should attenuate stars", rainNight.starOpacity < 0.35f)

        // Rain sun should not be visible
        assertFalse(rainMorning.light.sunVisible)
        assertFalse(rainNight.light.sunVisible)

        // Rain night particles should be cooler/dimmer than day
        assertTrue(rainNight.particles!!.color.alpha < rainMorning.particles!!.color.alpha)
    }

    @Test
    fun `Snow Night and Storm Night produce correct nocturnal conditions`() {
        val snowNight  = computeAmbientAtmosphere(WeatherType.Snow, TimeOfDay.Night)
        val stormNight = computeAmbientAtmosphere(WeatherType.Storm, TimeOfDay.Night)

        assertNotNull(snowNight.particles)
        assertNotNull(stormNight.particles)

        assertFalse(snowNight.light.sunVisible)
        assertFalse(stormNight.light.sunVisible)

        // Storm has 100% cloud density, completely extinguishing stars
        assertEquals(0f, stormNight.starOpacity, 0.05f)

        // Glass surfaces have translucent adaptive tint
        assertTrue(snowNight.glassAmbient.alpha in 0.10f..0.35f)
        assertTrue(stormNight.glassAmbient.alpha in 0.10f..0.35f)
    }

    @Test
    fun `Phase 3_5 Signature Presets produce expected visual parameters`() {
        // 1. 🌅 Sunny Morning
        val sunnyMorning = computeAmbientAtmosphere(WeatherType.Sunny, TimeOfDay.Morning)
        assertTrue(sunnyMorning.light.sunVisible)
        assertTrue(sunnyMorning.luminosity >= 0.85f)

        // 2. 🌇 Sunny Sunset
        val sunnySunset = computeAmbientAtmosphere(WeatherType.Sunny, TimeOfDay.Sunset)
        assertTrue(sunnySunset.light.sunVisible)
        assertTrue(sunnySunset.sunPosition.position.x > 0.60f) // Western sky

        // 3. 🌧️ Rain Evening
        val rainEvening = computeAmbientAtmosphere(WeatherType.Rain, TimeOfDay.Evening)
        assertFalse(rainEvening.light.sunVisible)
        assertNotNull(rainEvening.particles)

        // 4. ⛈️ Storm Night
        val stormNight = computeAmbientAtmosphere(WeatherType.Storm, TimeOfDay.Night)
        assertFalse(stormNight.light.sunVisible)
        assertEquals(0f, stormNight.starOpacity, 0.01f) // Thick storm completely blocks stars

        // 5. ❄️ Snow Night
        val snowNight = computeAmbientAtmosphere(WeatherType.Snow, TimeOfDay.Night)
        assertFalse(snowNight.light.sunVisible)
        assertNotNull(snowNight.particles)
        assertTrue(snowNight.glassAmbient.alpha in 0.10f..0.35f) // High transparency cold blue glass
    }
}
