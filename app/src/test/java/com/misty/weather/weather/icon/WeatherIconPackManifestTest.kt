package com.misty.weather.weather.icon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// ─── WeatherIconPackManifest Unit Tests ───────────────────────────────────────
// Phase 4.4-B0: Validates JSON serialization round-trips and fromJson parsing.
// ─────────────────────────────────────────────────────────────────────────────

class WeatherIconPackManifestTest {

    @Test
    fun fromJson_minimalValidManifest_parsesCorrectly() {
        val json = """
            {
              "format": "misty-weather-icon-pack",
              "id": "test-pack",
              "name": "Test Pack",
              "icons": { "clear_day": "icons/clear_day.svg" }
            }
        """.trimIndent()

        val manifest = WeatherIconPackManifest.fromJson(json)

        assertEquals("test-pack", manifest.id)
        assertEquals("Test Pack", manifest.name)
        assertEquals(1, manifest.icons.size)
        assertEquals("icons/clear_day.svg", manifest.icons["clear_day"])
    }

    @Test
    fun fromJson_fullManifest_parsesAllFields() {
        val json = """
            {
              "format": "misty-weather-icon-pack",
              "version": 2,
              "id": "my-icons-v2",
              "name": "My Icons",
              "author": "Test Author",
              "license": "MIT",
              "description": "A test icon pack",
              "preview": "preview.png",
              "sourceUrl": "https://example.com",
              "licenseUrl": "https://mit.org",
              "icons": {
                "clear_day": "clear_day.svg",
                "rain": "rain.svg",
                "snow": "snow.svg"
              }
            }
        """.trimIndent()

        val manifest = WeatherIconPackManifest.fromJson(json)

        assertEquals(2, manifest.version)
        assertEquals("my-icons-v2", manifest.id)
        assertEquals("My Icons", manifest.name)
        assertEquals("Test Author", manifest.author)
        assertEquals("MIT", manifest.license)
        assertEquals("A test icon pack", manifest.description)
        assertEquals("preview.png", manifest.preview)
        assertEquals(3, manifest.icons.size)
    }

    @Test
    fun fromJson_ignoresUnknownFields_doesNotCrash() {
        val json = """
            {
              "format": "misty-weather-icon-pack",
              "id": "test",
              "name": "Test",
              "icons": {},
              "unknownFutureField": "some_value",
              "anotherNewField": 42
            }
        """.trimIndent()

        // Should not throw
        val manifest = WeatherIconPackManifest.fromJson(json)
        assertEquals("test", manifest.id)
    }

    @Test
    fun fromJson_missingOptionalFields_useSafeDefaults() {
        val json = """
            {
              "format": "misty-weather-icon-pack",
              "id": "minimal",
              "name": "Minimal",
              "icons": {}
            }
        """.trimIndent()

        val manifest = WeatherIconPackManifest.fromJson(json)

        assertEquals(1, manifest.version)
        assertNull(manifest.author)
        assertNull(manifest.license)
        assertNull(manifest.description)
        assertNull(manifest.preview)
    }

    @Test
    fun toJson_fromJson_roundTrip_preservesAllFields() {
        val original = WeatherIconPackManifest(
            id          = "roundtrip-test",
            name        = "Roundtrip Pack",
            version     = 3,
            author      = "Alice",
            license     = "Apache-2.0",
            description = "Round trip test",
            preview     = "preview.png",
            icons       = mapOf("clear_day" to "icons/sun.svg", "rain" to "icons/rain.svg"),
        )

        val json = WeatherIconPackManifest.toJson(original)
        val restored = WeatherIconPackManifest.fromJson(json)

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.version, restored.version)
        assertEquals(original.author, restored.author)
        assertEquals(original.license, restored.license)
        assertEquals(original.description, restored.description)
        assertEquals(original.icons, restored.icons)
    }

    @Test
    fun format_constant_hasExpectedValue() {
        assertEquals("misty-weather-icon-pack", WeatherIconPackManifest.FORMAT_MISTY_ICON_PACK)
    }

    @Test
    fun manifestFileName_constant_isManifestJson() {
        assertEquals("manifest.json", WeatherIconPackManifest.MANIFEST_FILE_NAME)
    }
}
