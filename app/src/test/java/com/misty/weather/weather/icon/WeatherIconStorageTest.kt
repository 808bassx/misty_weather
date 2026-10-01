package com.misty.weather.weather.icon

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

// ─── WeatherIconStorage Unit Tests ───────────────────────────────────────────
// Phase 4.4-B0: Tests InMemoryWeatherIconStorage for pack management lifecycle.
// DataStoreWeatherIconStorage is tested via integration path (requires Android context).
// ─────────────────────────────────────────────────────────────────────────────

class WeatherIconStorageTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createInMemoryStorage(
        packsDir: File? = null,
        initialSelectedId: String = BuiltInIconPack.DEFAULT_PACK_ID,
    ) = InMemoryWeatherIconStorage(packsDir, initialSelectedId)

    private fun makeTestPack(id: String): WeatherIconPack = WeatherIconPack(
        id            = id,
        name          = "Test $id",
        version       = 1,
        author        = null,
        license       = null,
        description   = null,
        previewPath   = null,
        iconCount     = 1,
        supportedKeys = setOf(WeatherIconKey.RAIN),
        source        = IconPackSource.USER_IMPORTED,
    )

    // ── getSelectedPackId ─────────────────────────────────────────────────────

    @Test
    fun getSelectedPackId_default_returnsBuiltInId() = runBlocking {
        val storage = createInMemoryStorage()
        val id = storage.getSelectedPackId().first()
        assertEquals(BuiltInIconPack.DEFAULT_PACK_ID, id)
    }

    @Test
    fun getSelectedPackId_afterSave_returnsNewId() = runBlocking {
        val storage = createInMemoryStorage()
        storage.saveSelectedPackId("my-pack")
        val id = storage.getSelectedPackId().first()
        assertEquals("my-pack", id)
    }

    // ── getInstalledPacks ─────────────────────────────────────────────────────

    @Test
    fun getInstalledPacks_default_containsBuiltInPackFirst() {
        val storage = createInMemoryStorage()
        val packs = storage.getInstalledPacks()
        assertTrue("Should contain at least built-in pack", packs.isNotEmpty())
        assertEquals(BuiltInIconPack.DEFAULT_PACK_ID, packs.first().id)
    }

    @Test
    fun getInstalledPacks_afterAdd_containsAddedPack() {
        val storage = createInMemoryStorage()
        val testPack = makeTestPack("test-pack-alpha")
        storage.addPack(testPack)

        val packs = storage.getInstalledPacks()
        assertTrue("Should contain added test pack", packs.any { it.id == "test-pack-alpha" })
    }

    @Test
    fun getInstalledPacks_addSameIdTwice_replacesExisting() {
        val storage = createInMemoryStorage()
        val pack1 = makeTestPack("same-id").copy(name = "Version 1")
        val pack2 = makeTestPack("same-id").copy(name = "Version 2")

        storage.addPack(pack1)
        storage.addPack(pack2)

        val packs = storage.getInstalledPacks()
        val matching = packs.filter { it.id == "same-id" }
        assertEquals("Should only have one pack with the same id", 1, matching.size)
        assertEquals("Version 2", matching.first().name)
    }

    // ── deletePack ────────────────────────────────────────────────────────────

    @Test
    fun deletePack_existingPack_removesIt() = runBlocking {
        val storage = createInMemoryStorage()
        val pack = makeTestPack("to-delete")
        storage.addPack(pack)

        val deleted = storage.deletePack("to-delete")

        assertTrue("deletePack should return true for existing pack", deleted)
        val packs = storage.getInstalledPacks()
        assertFalse("Deleted pack should no longer be listed", packs.any { it.id == "to-delete" })
    }

    @Test
    fun deletePack_builtInPack_returnsFalseAndDoesNotDelete() = runBlocking {
        val storage = createInMemoryStorage()
        val result = storage.deletePack(BuiltInIconPack.DEFAULT_PACK_ID)
        assertFalse("Cannot delete built-in pack", result)
        val packs = storage.getInstalledPacks()
        assertTrue("Built-in pack must still be present", packs.any { it.id == BuiltInIconPack.DEFAULT_PACK_ID })
    }

    @Test
    fun deletePack_activePackDeleted_resetsSelectionToDefault() = runBlocking {
        val storage = createInMemoryStorage(initialSelectedId = "active-pack")
        val pack = makeTestPack("active-pack")
        storage.addPack(pack)
        storage.saveSelectedPackId("active-pack")

        storage.deletePack("active-pack")

        val selectedId = storage.getSelectedPackId().first()
        assertEquals(
            "Deleting active pack must reset selection to built-in default",
            BuiltInIconPack.DEFAULT_PACK_ID,
            selectedId,
        )
    }

    @Test
    fun deletePack_nonActivePackDeleted_selectionUnchanged() = runBlocking {
        val storage = createInMemoryStorage()
        val pack = makeTestPack("other-pack")
        storage.addPack(pack)
        storage.saveSelectedPackId(BuiltInIconPack.DEFAULT_PACK_ID)

        storage.deletePack("other-pack")

        val selectedId = storage.getSelectedPackId().first()
        assertEquals(
            "Deleting non-active pack must not change current selection",
            BuiltInIconPack.DEFAULT_PACK_ID,
            selectedId,
        )
    }

    // ── Filesystem scanning ───────────────────────────────────────────────────

    @Test
    fun getInstalledPacks_withPacksDir_scansInstalledZipFolders() {
        val packsDir = tempFolder.newFolder("icon_packs")

        // Create a valid installed pack on filesystem
        val packDir = File(packsDir, "fs-test-pack").apply { mkdirs() }
        val svgFile = File(packDir, "rain.svg").apply {
            writeText("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><circle cx="32" cy="32" r="20"/></svg>""")
        }
        File(packDir, "manifest.json").writeText(
            WeatherIconPackManifest.toJson(
                WeatherIconPackManifest(
                    id    = "fs-test-pack",
                    name  = "Filesystem Test Pack",
                    icons = mapOf("rain" to "rain.svg"),
                )
            )
        )

        val storage = InMemoryWeatherIconStorage(packsDir)
        val packs = storage.getInstalledPacks()

        assertTrue("Filesystem scan should find fs-test-pack", packs.any { it.id == "fs-test-pack" })
    }

    @Test
    fun getInstalledPacks_withCorruptManifest_skipsCorruptPack() {
        val packsDir = tempFolder.newFolder("icon_packs_corrupt")
        val corruptDir = File(packsDir, "corrupt-pack").apply { mkdirs() }
        File(corruptDir, "manifest.json").writeText("NOT VALID JSON {{{")

        val storage = InMemoryWeatherIconStorage(packsDir)
        // Should not throw — corrupt pack is silently skipped
        val packs = storage.getInstalledPacks()

        // Only built-in should remain
        assertEquals(1, packs.size)
        assertEquals(BuiltInIconPack.DEFAULT_PACK_ID, packs.first().id)
    }
}
