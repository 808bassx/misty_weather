package com.example.mistyweather.weather.icon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

// ─── WeatherIconPackValidator Unit Tests ─────────────────────────────────────
// Phase 4.4-B0: Security validation — Zip Slip, Zip Bomb, SVG injection, manifest rules.
// ─────────────────────────────────────────────────────────────────────────────

class WeatherIconPackValidatorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    // ── validateZipEntryName — Zip Slip Prevention ───────────────────────────

    @Test
    fun validateZipEntryName_normalPath_isValid() {
        val target = tempFolder.newFolder("pack")
        val result = WeatherIconPackValidator.validateZipEntryName("icons/clear_day.svg", target)
        assertTrue(result is WeatherIconPackValidator.ValidationResult.Valid)
    }

    @Test
    fun validateZipEntryName_dotDotSlash_isInvalid() {
        val target = tempFolder.newFolder("pack")
        val result = WeatherIconPackValidator.validateZipEntryName("../evil.svg", target)
        assertTrue("Zip Slip path must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateZipEntryName_slashPrefix_isInvalid() {
        val target = tempFolder.newFolder("pack")
        val result = WeatherIconPackValidator.validateZipEntryName("/etc/passwd", target)
        assertTrue("Absolute path must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateZipEntryName_windowsAbsolutePath_isInvalid() {
        val target = tempFolder.newFolder("pack")
        val result = WeatherIconPackValidator.validateZipEntryName("C:\\evil\\path.svg", target)
        assertTrue("Windows absolute path must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateZipEntryName_emptyName_isInvalid() {
        val target = tempFolder.newFolder("pack")
        val result = WeatherIconPackValidator.validateZipEntryName("", target)
        assertTrue("Blank entry name must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateZipEntryName_doubleDotComponent_isInvalid() {
        val target = tempFolder.newFolder("pack")
        val result = WeatherIconPackValidator.validateZipEntryName("icons/../../../etc/shadow", target)
        assertTrue("Path traversal via icons/../.. must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateZipEntryName_safeNestedPath_isValid() {
        val target = tempFolder.newFolder("pack")
        val result = WeatherIconPackValidator.validateZipEntryName("icons/day/clear.svg", target)
        assertTrue(result is WeatherIconPackValidator.ValidationResult.Valid)
    }

    // ── validateManifest ──────────────────────────────────────────────────────

    @Test
    fun validateManifest_valid_returnsValid() {
        val manifest = WeatherIconPackManifest(
            id    = "my-pack",
            name  = "My Pack",
            icons = mapOf("clear_day" to "clear.svg"),
        )
        val result = WeatherIconPackValidator.validateManifest(manifest)
        assertTrue(result is WeatherIconPackValidator.ValidationResult.Valid)
    }

    @Test
    fun validateManifest_emptyId_isInvalid() {
        val manifest = WeatherIconPackManifest(id = "", name = "Test", icons = mapOf("rain" to "rain.svg"))
        val result = WeatherIconPackValidator.validateManifest(manifest)
        assertTrue(result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateManifest_reservedMistyDefaultId_isInvalid() {
        val manifest = WeatherIconPackManifest(id = "misty-default", name = "Copy", icons = mapOf("rain" to "rain.svg"))
        val result = WeatherIconPackValidator.validateManifest(manifest)
        assertTrue("Reserved ID 'misty-default' must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateManifest_idWithSpecialChars_isInvalid() {
        val manifest = WeatherIconPackManifest(id = "bad id!", name = "Bad", icons = mapOf("rain" to "rain.svg"))
        val result = WeatherIconPackValidator.validateManifest(manifest)
        assertTrue("ID with spaces/exclamations must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateManifest_emptyName_isInvalid() {
        val manifest = WeatherIconPackManifest(id = "valid-id", name = "", icons = mapOf("rain" to "rain.svg"))
        val result = WeatherIconPackValidator.validateManifest(manifest)
        assertTrue(result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateManifest_emptyIcons_isInvalid() {
        val manifest = WeatherIconPackManifest(id = "valid-id", name = "Pack", icons = emptyMap())
        val result = WeatherIconPackValidator.validateManifest(manifest)
        assertTrue("Empty icons map must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateManifest_versionZero_isInvalid() {
        val manifest = WeatherIconPackManifest(id = "valid", name = "Pack", version = 0, icons = mapOf("rain" to "rain.svg"))
        val result = WeatherIconPackValidator.validateManifest(manifest)
        assertTrue("Version 0 must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    // ── validateSvgContent — Script Injection Prevention ─────────────────────

    @Test
    fun validateSvgContent_cleanSvg_isValid() {
        val svg = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><circle cx="32" cy="32" r="20" fill="gold"/></svg>"""
        val result = WeatherIconPackValidator.validateSvgContent(svg)
        assertTrue(result is WeatherIconPackValidator.ValidationResult.Valid)
    }

    @Test
    fun validateSvgContent_withScriptTag_isInvalid() {
        val svg = """<svg xmlns="http://www.w3.org/2000/svg"><script>alert('xss')</script><circle r="20"/></svg>"""
        val result = WeatherIconPackValidator.validateSvgContent(svg)
        assertTrue("SVG with <script> must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateSvgContent_withJavascriptProtocol_isInvalid() {
        val svg = """<svg xmlns="http://www.w3.org/2000/svg"><a href="javascript:evil()"><circle r="20"/></a></svg>"""
        val result = WeatherIconPackValidator.validateSvgContent(svg)
        assertTrue("SVG with javascript: href must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateSvgContent_withXXEEntity_isInvalid() {
        val svg = """<?xml version="1.0"?><!DOCTYPE svg [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><svg xmlns="http://www.w3.org/2000/svg"><text>&xxe;</text></svg>"""
        val result = WeatherIconPackValidator.validateSvgContent(svg)
        assertTrue("SVG with XXE entity must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateSvgContent_withExternalHttpUrl_isInvalid() {
        val svg = """<svg xmlns="http://www.w3.org/2000/svg"><image href="https://evil.com/tracker.png"/></svg>"""
        val result = WeatherIconPackValidator.validateSvgContent(svg)
        assertTrue("SVG with external https:// URL must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateSvgContent_missingClosingTag_isInvalid() {
        val svg = """<svg xmlns="http://www.w3.org/2000/svg"><circle r="20"/>"""
        val result = WeatherIconPackValidator.validateSvgContent(svg)
        assertTrue("SVG without </svg> must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    @Test
    fun validateSvgContent_notAnSvg_isInvalid() {
        val notSvg = """{"key": "I am JSON not SVG"}"""
        val result = WeatherIconPackValidator.validateSvgContent(notSvg)
        assertTrue("Non-SVG content must be rejected", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }

    // ── validateUnpackedIconPack ──────────────────────────────────────────────

    @Test
    fun validateUnpackedIconPack_missingManifest_isInvalid() {
        val packDir = tempFolder.newFolder("empty-pack")
        // No manifest.json created
        val (result, manifest) = WeatherIconPackValidator.validateUnpackedIconPack(packDir)
        assertTrue(result is WeatherIconPackValidator.ValidationResult.Invalid)
        assertEquals(null, manifest)
    }

    @Test
    fun validateUnpackedIconPack_validPackWithOneSvg_isValid() {
        val packDir = tempFolder.newFolder("valid-pack")
        val iconFile = File(packDir, "clear_day.svg").apply {
            writeText("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><circle cx="32" cy="32" r="20" fill="gold"/></svg>""")
        }
        File(packDir, "manifest.json").writeText(
            WeatherIconPackManifest.toJson(
                WeatherIconPackManifest(
                    id    = "test-valid",
                    name  = "Test Valid",
                    icons = mapOf("clear_day" to "clear_day.svg"),
                )
            )
        )

        val (result, manifest) = WeatherIconPackValidator.validateUnpackedIconPack(packDir)

        assertTrue("Valid pack should pass validation", result is WeatherIconPackValidator.ValidationResult.Valid)
        assertEquals("test-valid", manifest?.id)
    }

    @Test
    fun validateUnpackedIconPack_manifestPointsToMissingFile_isInvalid() {
        val packDir = tempFolder.newFolder("missing-svg-pack")
        File(packDir, "manifest.json").writeText(
            WeatherIconPackManifest.toJson(
                WeatherIconPackManifest(
                    id    = "test-missing-file",
                    name  = "Missing File Pack",
                    icons = mapOf("rain" to "icons/rain.svg"), // File does not exist
                )
            )
        )

        val (result, _) = WeatherIconPackValidator.validateUnpackedIconPack(packDir)
        assertTrue("Pack with missing SVG file must fail validation", result is WeatherIconPackValidator.ValidationResult.Invalid)
    }
}
