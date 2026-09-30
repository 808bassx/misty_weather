package com.example.mistyweather.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying calm motion duration ordering, easing validity,
 * and reduce motion accessibility scaling.
 */
class MistyMotionTest {

    @Test
    fun durationsAreMonotonicallyOrdered() {
        assertEquals(0, MistyMotion.DurationInstant)
        assertTrue(MistyMotion.DurationInstant < MistyMotion.DurationFast)
        assertTrue(MistyMotion.DurationFast < MistyMotion.DurationNormal)
        assertTrue(MistyMotion.DurationNormal < MistyMotion.DurationSlow)
        assertTrue(MistyMotion.DurationSlow < MistyMotion.DurationWeather)

        // Specific values
        assertEquals(150, MistyMotion.DurationFast)
        assertEquals(280, MistyMotion.DurationNormal)
        assertEquals(450, MistyMotion.DurationSlow)
        assertEquals(800, MistyMotion.DurationWeather)
    }

    @Test
    fun easingsAreDefinedAndValid() {
        assertNotNull(MistyMotion.EasingStandard)
        assertNotNull(MistyMotion.EasingEmphasized)
        assertNotNull(MistyMotion.EasingDecelerate)
        assertNotNull(MistyMotion.EasingWeather)
    }

    @Test
    fun animationSpecsProduceValidDurations() {
        assertEquals(MistyMotion.DurationFast, MistyMotion.fastSpec<Float>().durationMillis)
        assertEquals(MistyMotion.DurationNormal, MistyMotion.normalSpec<Float>().durationMillis)
        assertEquals(MistyMotion.DurationSlow, MistyMotion.slowSpec<Float>().durationMillis)
        assertEquals(MistyMotion.DurationWeather, MistyMotion.weatherSpec<Float>().durationMillis)
    }

    @Test
    fun resolveDurationRespectsReducedMotion() {
        // When not reduced, returns base duration
        assertEquals(MistyMotion.DurationFast, MistyMotion.resolveDuration(MistyMotion.DurationFast, isReduced = false))
        assertEquals(MistyMotion.DurationNormal, MistyMotion.resolveDuration(MistyMotion.DurationNormal, isReduced = false))
        assertEquals(MistyMotion.DurationSlow, MistyMotion.resolveDuration(MistyMotion.DurationSlow, isReduced = false))
        assertEquals(MistyMotion.DurationWeather, MistyMotion.resolveDuration(MistyMotion.DurationWeather, isReduced = false))

        // When reduced motion is enabled, cuts duration to 0
        assertEquals(MistyMotion.DurationInstant, MistyMotion.resolveDuration(MistyMotion.DurationFast, isReduced = true))
        assertEquals(MistyMotion.DurationInstant, MistyMotion.resolveDuration(MistyMotion.DurationNormal, isReduced = true))
        assertEquals(MistyMotion.DurationInstant, MistyMotion.resolveDuration(MistyMotion.DurationSlow, isReduced = true))
        assertEquals(MistyMotion.DurationInstant, MistyMotion.resolveDuration(MistyMotion.DurationWeather, isReduced = true))
    }
}
