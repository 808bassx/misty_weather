package com.misty.weather.weather.icon

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

// ─── Weather Icon Storage Contract & Implementations ─────────────────────────
//
// Phase 4.4-B0: Manages persistent icon pack filesystem directories and DataStore preferences.
//
// Responsibilities:
//   1. DataStore persistence for active pack ID selection.
//   2. Filesystem scanning for user-installed icon packs.
//   3. Safe deletion with automatic rollback to Misty Default.
//
// ─────────────────────────────────────────────────────────────────────────────

interface WeatherIconStorage {
    fun getSelectedPackId(): Flow<String>
    suspend fun saveSelectedPackId(packId: String)
    fun getInstalledPacks(): List<WeatherIconPack>
    suspend fun deletePack(packId: String): Boolean
}

class DataStoreWeatherIconStorage(
    private val dataStore: DataStore<Preferences>,
    private val packsDir: File,
) : WeatherIconStorage {

    init {
        packsDir.mkdirs()
    }

    override fun getSelectedPackId(): Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_PACK_ID] ?: BuiltInIconPack.DEFAULT_PACK_ID
    }

    override suspend fun saveSelectedPackId(packId: String) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_PACK_ID] = packId
        }
    }

    override fun getInstalledPacks(): List<WeatherIconPack> {
        val packs = mutableListOf<WeatherIconPack>()
        // Built-in is always first and persistent
        packs.add(BuiltInIconPack.instance)

        val subDirs = packsDir.listFiles { file -> file.isDirectory } ?: emptyArray()
        for (dir in subDirs) {
            val manifestFile = File(dir, WeatherIconPackManifest.MANIFEST_FILE_NAME)
            if (manifestFile.exists() && manifestFile.isFile) {
                try {
                    val manifest = WeatherIconPackManifest.fromJson(manifestFile.readText(Charsets.UTF_8))
                    val pack = WeatherIconPackImporter.buildPackFromInstalledDir(dir, manifest)
                    packs.add(pack)
                } catch (e: Exception) {
                    // Skip corrupt pack during scan
                }
            }
        }

        return packs
    }

    override suspend fun deletePack(packId: String): Boolean {
        if (packId == BuiltInIconPack.DEFAULT_PACK_ID) {
            return false // Cannot delete built-in default pack
        }

        val targetDir = File(packsDir, packId)
        val deleted = if (targetDir.exists()) {
            targetDir.deleteRecursively()
        } else {
            false
        }

        // If active pack was deleted, fall back to default pack immediately
        val currentSelected = getSelectedPackId().first()
        if (currentSelected == packId) {
            saveSelectedPackId(BuiltInIconPack.DEFAULT_PACK_ID)
        }

        return deleted
    }

    companion object {
        val KEY_SELECTED_PACK_ID = stringPreferencesKey("selected_icon_pack_id")
    }
}

/**
 * In-memory storage implementation for hermetic unit testing without Android DataStore dependencies.
 */
class InMemoryWeatherIconStorage(
    private val packsDir: File? = null,
    initialSelectedId: String = BuiltInIconPack.DEFAULT_PACK_ID,
) : WeatherIconStorage {

    private val _selectedId = MutableStateFlow(initialSelectedId)
    private val inMemoryPacks = mutableListOf<WeatherIconPack>(BuiltInIconPack.instance)

    override fun getSelectedPackId(): Flow<String> = _selectedId.asStateFlow()

    override suspend fun saveSelectedPackId(packId: String) {
        _selectedId.value = packId
    }

    fun addPack(pack: WeatherIconPack) {
        inMemoryPacks.removeAll { it.id == pack.id }
        inMemoryPacks.add(pack)
    }

    override fun getInstalledPacks(): List<WeatherIconPack> {
        if (packsDir != null && packsDir.exists()) {
            val packs = mutableListOf<WeatherIconPack>(BuiltInIconPack.instance)
            packsDir.listFiles { f -> f.isDirectory }?.forEach { dir ->
                val mFile = File(dir, WeatherIconPackManifest.MANIFEST_FILE_NAME)
                if (mFile.exists()) {
                    runCatching {
                        val m = WeatherIconPackManifest.fromJson(mFile.readText())
                        packs.add(WeatherIconPackImporter.buildPackFromInstalledDir(dir, m))
                    }
                }
            }
            return packs
        }
        return inMemoryPacks.toList()
    }

    override suspend fun deletePack(packId: String): Boolean {
        if (packId == BuiltInIconPack.DEFAULT_PACK_ID) return false
        val removed = inMemoryPacks.removeAll { it.id == packId }
        if (packsDir != null) {
            File(packsDir, packId).deleteRecursively()
        }
        if (_selectedId.value == packId) {
            _selectedId.value = BuiltInIconPack.DEFAULT_PACK_ID
        }
        return removed || packsDir != null
    }
}
