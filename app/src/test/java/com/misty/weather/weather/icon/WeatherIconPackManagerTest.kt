package com.misty.weather.weather.icon

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// ─── WeatherIconPackManager Unit Tests ───────────────────────────────────────
// Phase 4.4-B0: Manager lifecycle — selection, import, deletion, prewarm, singleton reset.
// Uses DefaultWeatherIconPackManager directly with InMemoryWeatherIconStorage
// to avoid Android DataStore dependencies in JVM unit tests.
// Uses Dispatchers.Unconfined instead of Main to avoid Android Main dispatcher.
// ─────────────────────────────────────────────────────────────────────────────

class WeatherIconPackManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var packsDir: File
    private lateinit var stagingDir: File
    private lateinit var storage: InMemoryWeatherIconStorage
    private lateinit var importer: WeatherIconPackImporter
    private lateinit var manager: DefaultWeatherIconPackManager

    @Before
    fun setUp() {
        packsDir = tempFolder.newFolder("icon_packs")
        stagingDir = tempFolder.newFolder("staging")
        storage = InMemoryWeatherIconStorage(packsDir)
        importer = WeatherIconPackImporter(stagingDir, packsDir)
        // Inject Dispatchers.Unconfined for test scope — avoids Android Main dispatcher
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        manager = DefaultWeatherIconPackManager(
            storage      = storage,
            importer     = importer,
            scope        = testScope,
            ioDispatcher = Dispatchers.Unconfined,
        )
        DefaultWeatherIconPackManager.setInstanceForTesting(manager)
    }

    @After
    fun tearDown() {
        DefaultWeatherIconPackManager.setInstanceForTesting(null)
    }

    // ── Initial State ─────────────────────────────────────────────────────────

    @Test
    fun initialState_activePackIsBuiltIn() = runBlocking {
        val activePack = manager.activePack.first()
        assertEquals(BuiltInIconPack.DEFAULT_PACK_ID, activePack.id)
        assertTrue("Initial active pack should be built-in", activePack.isBuiltIn)
    }

    @Test
    fun initialState_installedPacksContainsOnlyBuiltIn() = runBlocking {
        val packs = manager.installedPacks.first()
        assertEquals(1, packs.size)
        assertEquals(BuiltInIconPack.DEFAULT_PACK_ID, packs.first().id)
    }

    @Test
    fun initialState_activePackIdIsDefault() = runBlocking {
        val id = manager.activePackId.first()
        assertEquals(BuiltInIconPack.DEFAULT_PACK_ID, id)
    }

    // ── selectPack ────────────────────────────────────────────────────────────

    @Test
    fun selectPack_unknownPackId_fallsBackToBuiltIn() = runBlocking {
        manager.selectPack("nonexistent-pack-id")
        val activePack = manager.activePack.first()
        assertEquals(BuiltInIconPack.DEFAULT_PACK_ID, activePack.id)
    }

    // ── resolveIcon ───────────────────────────────────────────────────────────

    @Test
    fun resolveIcon_withBuiltInActivePack_returnsVectorContent() {
        val asset = manager.resolveIcon(WeatherIconKey.CLEAR_DAY)
        assertTrue("Built-in pack should produce VectorContent", asset is WeatherIconAsset.VectorContent)
    }

    @Test
    fun resolveIcon_allRequiredKeys_neverReturnsMissing() {
        WeatherIconKey.requiredKeys.forEach { key ->
            val asset = manager.resolveIcon(key)
            assertFalse(
                "resolveIcon must never return Missing for built-in pack (key=$key)",
                asset is WeatherIconAsset.Missing,
            )
        }
    }

    // ── getOrLoadPicture ──────────────────────────────────────────────────────

    @Test
    fun getOrLoadPicture_builtInFallback_returnsNull() {
        val fallback = WeatherIconAsset.BuiltInFallback(WeatherIconKey.CLEAR_DAY)
        val picture = manager.getOrLoadPicture(fallback)
        assertEquals(
            "BuiltInFallback must return null (drawn by Canvas, not SVG picture)",
            null,
            picture,
        )
    }

    @Test
    fun getOrLoadPicture_missingAsset_returnsNull() {
        val missing = WeatherIconAsset.Missing(WeatherIconKey.CLEAR_DAY)
        val picture = manager.getOrLoadPicture(missing)
        assertEquals(null, picture)
    }

    // ── importPack ────────────────────────────────────────────────────────────

    @Test
    fun importPack_validCompleteZip_returnsSuccess() = runBlocking {
        val zipStream = createValidZip(
            id       = "test-import",
            name     = "Test Import Pack",
            iconKeys = listOf(WeatherIconKey.CLEAR_DAY, WeatherIconKey.RAIN, WeatherIconKey.SNOW),
        )

        val result = manager.importPack(zipStream.inputStream())

        assertTrue("Valid ZIP import should succeed", result is IconPackImportResult.Success)
        val success = result as IconPackImportResult.Success
        assertEquals("test-import", success.pack.id)
        assertEquals("Test Import Pack", success.pack.name)
        assertTrue("Import should add pack to installed list", manager.installedPacks.first().any { it.id == "test-import" })
    }

    @Test
    fun importPack_zipWithZipSlipPath_returnsUnsafeArchive() = runBlocking {
        val maliciousZip = createMaliciousZipWithSlip()
        val result = manager.importPack(maliciousZip.inputStream())
        assertTrue("Zip Slip attack must be rejected", result is IconPackImportResult.UnsafeArchive)
    }

    @Test
    fun importPack_validZip_switchesActivePack() = runBlocking {
        val zipStream = createValidZip("auto-select-pack", "Auto Select", listOf(WeatherIconKey.RAIN))
        manager.importPack(zipStream.inputStream())

        val activeId = manager.activePackId.first()
        assertEquals("Importing a pack should auto-select it", "auto-select-pack", activeId)
    }

    // ── deletePack ────────────────────────────────────────────────────────────

    @Test
    fun deletePack_existingUserPack_removesFromInstalled() = runBlocking {
        // Import a pack first
        val zipStream = createValidZip("delete-test-pack", "Delete Test", listOf(WeatherIconKey.FOG))
        manager.importPack(zipStream.inputStream())

        assertTrue("Pack should be installed before deletion", manager.installedPacks.first().any { it.id == "delete-test-pack" })

        val deleted = manager.deletePack("delete-test-pack")

        assertTrue("deletePack should return true", deleted)
        assertFalse("Pack should be removed from installed list", manager.installedPacks.first().any { it.id == "delete-test-pack" })
    }

    @Test
    fun deletePack_builtInPack_returnsFalse() = runBlocking {
        val result = manager.deletePack(BuiltInIconPack.DEFAULT_PACK_ID)
        assertFalse("Cannot delete built-in pack", result)
        assertTrue("Built-in must still be installed", manager.installedPacks.first().any { it.id == BuiltInIconPack.DEFAULT_PACK_ID })
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun createValidZip(
        id: String,
        name: String,
        iconKeys: List<WeatherIconKey>,
    ): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            val iconMap = iconKeys.associate { key -> key.serialName to "${key.serialName}.svg" }

            // manifest.json
            val manifest = WeatherIconPackManifest(id = id, name = name, icons = iconMap)
            val manifestJson = WeatherIconPackManifest.toJson(manifest)
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(manifestJson.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // SVG files
            for (key in iconKeys) {
                val svgContent = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><circle cx="32" cy="32" r="20" fill="currentColor"/></svg>"""
                zos.putNextEntry(ZipEntry("${key.serialName}.svg"))
                zos.write(svgContent.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    private fun createMaliciousZipWithSlip(): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            // Zip Slip path traversal entry
            zos.putNextEntry(ZipEntry("../evil_outside.svg"))
            zos.write("<svg></svg>".toByteArray())
            zos.closeEntry()

            // manifest.json that references the malicious path
            val manifest = WeatherIconPackManifest(
                id    = "malicious-pack",
                name  = "Malicious",
                icons = mapOf("rain" to "../evil_outside.svg"),
            )
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(WeatherIconPackManifest.toJson(manifest).toByteArray())
            zos.closeEntry()
        }
        return baos.toByteArray()
    }
}
