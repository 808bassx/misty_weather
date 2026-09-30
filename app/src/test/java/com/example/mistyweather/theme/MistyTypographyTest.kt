package com.example.mistyweather.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying typographic scale hierarchy, numeric letter-spacing,
 * and semantic style completeness.
 */
class MistyTypographyTest {

    @Test
    fun temperatureNumericScaleIsOrdered() {
        val hero = MistyType.TemperatureHero
        val large = MistyType.TemperatureLarge
        val compact = MistyType.TemperatureCompact

        assertTrue("Hero > Large", hero.fontSize.value > large.fontSize.value)
        assertTrue("Large > Compact", large.fontSize.value > compact.fontSize.value)

        // Hypotheses: Hero is 84sp Light, Large is 56sp Light, Compact is 28sp Light
        assertEquals(84.sp, hero.fontSize)
        assertEquals(56.sp, large.fontSize)
        assertEquals(28.sp, compact.fontSize)

        assertEquals(FontWeight.Light, hero.fontWeight)
        assertEquals(FontWeight.Light, large.fontWeight)
        assertEquals(FontWeight.Light, compact.fontWeight)

        // Letter tracking: Hero must have negative letter spacing (-3sp) to avoid digit splay
        assertTrue("Hero has negative tracking", hero.letterSpacing.value < 0f)
        assertTrue("Large has negative tracking", large.letterSpacing.value < 0f)
    }

    @Test
    fun semanticTextStylesAreDefinedAndNonNull() {
        assertNotNull(MistyType.LocationPrimary)
        assertNotNull(MistyType.LocationSecondary)
        assertNotNull(MistyType.ConditionPrimary)
        assertNotNull(MistyType.DetailRow)
        assertNotNull(MistyType.ForecastTime)
        assertNotNull(MistyType.ForecastTemp)
        assertNotNull(MistyType.CardHeader)
        assertNotNull(MistyType.ScreenTitle)
        assertNotNull(MistyType.ButtonLabel)
        assertNotNull(MistyType.NavLabel)
        assertNotNull(MistyType.CaptionMicro)

        // Backward-compatibility aliases
        assertEquals(MistyType.TemperatureHero, MistyType.TemperatureDisplay)
        assertEquals(MistyType.LocationPrimary, MistyType.LocationDisplay)
        assertEquals(MistyType.ConditionPrimary, MistyType.ConditionLabel)
        assertEquals(MistyType.DetailRow, MistyType.DetailLabel)
        assertEquals(MistyType.CardHeader, MistyType.SectionHeader)
    }

    @Test
    fun material3TypographyMappingIsPopulated() {
        val m3 = MistyM3Typography
        assertNotNull(m3.displayLarge)
        assertNotNull(m3.displayMedium)
        assertNotNull(m3.displaySmall)
        assertNotNull(m3.headlineLarge)
        assertNotNull(m3.headlineMedium)
        assertNotNull(m3.headlineSmall)
        assertNotNull(m3.titleLarge)
        assertNotNull(m3.titleMedium)
        assertNotNull(m3.titleSmall)
        assertNotNull(m3.bodyLarge)
        assertNotNull(m3.bodyMedium)
        assertNotNull(m3.bodySmall)
        assertNotNull(m3.labelLarge)
        assertNotNull(m3.labelMedium)
        assertNotNull(m3.labelSmall)
    }
}
