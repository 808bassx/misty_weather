package com.misty.weather.weather.icon

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

// ─── WeatherIconResolver Unit Tests ──────────────────────────────────────────
// Phase 4.4-B0: Three-tier fallback resolution guarantees.
// ─────────────────────────────────────────────────────────────────────────────

class WeatherIconResolverTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val resolver = DefaultWeatherIconResolver()

    // ── Tier 1: Built-In Active Pack ─────────────────────────────────────────

    @Test
    fun resolve_builtInActivePack_returnsVectorContent() {
        val activePack = BuiltInIconPack.instance
        val result = resolver.resolve(WeatherIconKey.CLEAR_DAY, activePack)
        assertTrue(
            "Built-in pack should return VectorContent",
            result is WeatherIconAsset.VectorContent,
        )
        val content = result as WeatherIconAsset.VectorContent
        assertTrue("SVG content must not be blank", content.svgContent.isNotBlank())
        assertTrue("SVG content must contain <svg", content.svgContent.contains("<svg", ignoreCase = true))
    }

    @Test
    fun resolve_builtInActivePack_correctKey() {
        val activePack = BuiltInIconPack.instance
        val result = resolver.resolve(WeatherIconKey.SNOW, activePack)
        assertTrue(result is WeatherIconAsset.VectorContent)
        val content = result as WeatherIconAsset.VectorContent
        assertTrue(content.svgContent.isNotBlank())
    }

    // ── Tier 1: User Pack with File ───────────────────────────────────────────

    @Test
    fun resolve_userPackHasKey_returnsVectorFile() {
        val packDir = tempFolder.newFolder("user-pack")
        val svgFile = File(packDir, "rain.svg").apply {
            writeText("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><circle cx="32" cy="32" r="20"/></svg>""")
        }

        val userPack = WeatherIconPack(
            id               = "user-test",
            name             = "User Test",
            version          = 1,
            author           = null,
            license          = null,
            description      = null,
            previewPath      = null,
            iconCount        = 1,
            supportedKeys    = setOf(WeatherIconKey.RAIN),
            source           = IconPackSource.USER_IMPORTED,
            rootDir          = packDir,
            iconRelativePaths = mapOf(WeatherIconKey.RAIN to "rain.svg"),
        )

        val result = resolver.resolve(WeatherIconKey.RAIN, userPack)
        assertTrue("User pack with valid file should return VectorFile", result is WeatherIconAsset.VectorFile)
        val vectorFile = result as WeatherIconAsset.VectorFile
        assertTrue("Resolved file must exist", vectorFile.file.exists())
        assertTrue("Resolved file must not be empty", vectorFile.file.length() > 0)
    }

    // ── Tier 2: Fallback from User Pack Missing Key to Default ────────────────

    @Test
    fun resolve_userPackMissingKey_fallsToBuiltIn() {
        // User pack supports only RAIN, no SNOW
        val packDir = tempFolder.newFolder("partial-pack")
        val userPack = WeatherIconPack(
            id               = "partial-pack",
            name             = "Partial",
            version          = 1,
            author           = null,
            license          = null,
            description      = null,
            previewPath      = null,
            iconCount        = 1,
            supportedKeys    = setOf(WeatherIconKey.RAIN), // No SNOW
            source           = IconPackSource.USER_IMPORTED,
            rootDir          = packDir,
            iconRelativePaths = mapOf(WeatherIconKey.RAIN to "rain.svg"),
        )

        // Resolving SNOW with partial user pack should fall through to built-in
        val result = resolver.resolve(WeatherIconKey.SNOW, userPack, BuiltInIconPack.instance)
        assertTrue(
            "Missing key in user pack must fall to built-in VectorContent",
            result is WeatherIconAsset.VectorContent,
        )
    }

    // ── Tier 3: Both Packs Missing Key → BuiltInFallback ─────────────────────

    @Test
    fun resolve_bothPacksMissingKey_returnsBuiltInFallback() {
        // User pack with no supported keys
        val emptyUserPack = WeatherIconPack(
            id               = "empty-pack",
            name             = "Empty",
            version          = 1,
            author           = null,
            license          = null,
            description      = null,
            previewPath      = null,
            iconCount        = 0,
            supportedKeys    = emptySet(),
            source           = IconPackSource.USER_IMPORTED,
            rootDir          = tempFolder.newFolder("empty"),
            iconRelativePaths = emptyMap(),
        )

        // Create a minimal non-built-in "default" pack with no keys either
        val emptyDefaultPack = WeatherIconPack(
            id               = "empty-default",
            name             = "Empty Default",
            version          = 1,
            author           = null,
            license          = null,
            description      = null,
            previewPath      = null,
            iconCount        = 0,
            supportedKeys    = emptySet(),
            source           = IconPackSource.USER_IMPORTED,
            rootDir          = tempFolder.newFolder("emptyDefault"),
            iconRelativePaths = emptyMap(),
        )

        val result = resolver.resolve(WeatherIconKey.FOG, emptyUserPack, emptyDefaultPack)
        assertTrue(
            "When both packs lack the key, must fall to BuiltInFallback",
            result is WeatherIconAsset.BuiltInFallback,
        )
    }

    // ── Coverage: BuiltIn Pack supports all required keys ────────────────────

    @Test
    fun builtInIconPack_supportsAllRequiredKeys() {
        WeatherIconKey.requiredKeys.forEach { key ->
            val result = resolver.resolve(key, BuiltInIconPack.instance)
            assertTrue(
                "BuiltInIconPack must support $key",
                result is WeatherIconAsset.VectorContent,
            )
        }
    }

    // ── Incomplete pack never crashes ─────────────────────────────────────────

    @Test
    fun resolve_incompleteUserPack_neverCrashesForAnyKey() {
        val packDir = tempFolder.newFolder("incomplete")
        // Pack declares all keys but has no actual files
        val incompletePack = WeatherIconPack(
            id               = "incomplete",
            name             = "Incomplete",
            version          = 1,
            author           = null,
            license          = null,
            description      = null,
            previewPath      = null,
            iconCount        = 0,
            supportedKeys    = WeatherIconKey.requiredKeys, // Declares support but no files
            source           = IconPackSource.USER_IMPORTED,
            rootDir          = packDir,
            iconRelativePaths = WeatherIconKey.requiredKeys.associateWith { "missing_${it.name}.svg" },
        )

        // Must not throw for any key
        WeatherIconKey.entries.filter { it != WeatherIconKey.UNKNOWN }.forEach { key ->
            val result = resolver.resolve(key, incompletePack, BuiltInIconPack.instance)
            // Result should not be Missing — should fall to VectorContent (Tier 2 built-in)
            assertTrue(
                "Incomplete pack must never produce Missing for $key",
                result !is WeatherIconAsset.Missing,
            )
        }
    }
}
