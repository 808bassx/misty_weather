package com.misty.weather.weather.icon

import android.content.Context
import android.graphics.Picture
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

// ─── Weather Icon Pack Manager ───────────────────────────────────────────────
//
// Phase 4.4-B0: Central manager coordinating icon pack lifecycle, caching, and resolution.
//
// Performance & Concurrency Invariants:
//   1. Compose recompositions and weather state refreshes perform ZERO filesystem IO.
//   2. High-performance LruCache caches parsed Android [Picture] instances.
//   3. Pack loading, unzipping, and SVG parsing are dispatched to [Dispatchers.IO].
//   4. Automatic rollback to Misty Default if the active user pack is deleted or corrupted.
//
// ─────────────────────────────────────────────────────────────────────────────

interface WeatherIconPackManager {
    val installedPacks: StateFlow<List<WeatherIconPack>>
    val activePack: StateFlow<WeatherIconPack>
    val activePackId: StateFlow<String>

    suspend fun selectPack(packId: String)
    suspend fun importPack(inputStream: InputStream): IconPackImportResult
    suspend fun deletePack(packId: String): Boolean
    fun resolveIcon(key: WeatherIconKey): WeatherIconAsset
    fun getOrLoadPicture(asset: WeatherIconAsset): Picture?

    companion object {
        fun getInstance(context: Context): WeatherIconPackManager =
            DefaultWeatherIconPackManager.getInstance(context)

        fun setInstanceForTesting(manager: WeatherIconPackManager?) =
            DefaultWeatherIconPackManager.setInstanceForTesting(manager)
    }
}

class DefaultWeatherIconPackManager(
    private val storage: WeatherIconStorage,
    private val importer: WeatherIconPackImporter,
    private val resolver: WeatherIconResolver = DefaultWeatherIconResolver(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WeatherIconPackManager {

    private val _installedPacks = MutableStateFlow<List<WeatherIconPack>>(listOf(BuiltInIconPack.instance))
    override val installedPacks: StateFlow<List<WeatherIconPack>> = _installedPacks.asStateFlow()

    private val _activePack = MutableStateFlow<WeatherIconPack>(BuiltInIconPack.instance)
    override val activePack: StateFlow<WeatherIconPack> = _activePack.asStateFlow()

    private val _activePackId = MutableStateFlow(BuiltInIconPack.DEFAULT_PACK_ID)
    override val activePackId: StateFlow<String> = _activePackId.asStateFlow()

    // ── In-Memory Picture LRU Cache (High-Performance Zero-IO Recompositions) ─
    private val pictureCacheLock = Any()
    private val pictureCache = object : LinkedHashMap<String, Picture>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Picture>?): Boolean {
            return size > 64
        }
    }

    init {
        // Initial scan of installed packs
        refreshInstalledPacks()

        // Observe DataStore active pack preference
        scope.launch {
            storage.getSelectedPackId().collectLatest { selectedId ->
                _activePackId.value = selectedId
                val found = _installedPacks.value.find { it.id == selectedId } ?: BuiltInIconPack.instance
                _activePack.value = found
                prewarmPack(found)
            }
        }
    }

    private fun refreshInstalledPacks() {
        val packs = storage.getInstalledPacks()
        _installedPacks.value = packs
        val currentId = _activePackId.value
        val currentActive = packs.find { it.id == currentId } ?: BuiltInIconPack.instance
        _activePack.value = currentActive
    }

    override suspend fun selectPack(packId: String) {
        val pack = _installedPacks.value.find { it.id == packId } ?: BuiltInIconPack.instance
        storage.saveSelectedPackId(pack.id)
        _activePackId.value = pack.id
        _activePack.value = pack
        prewarmPack(pack)
    }

    override suspend fun importPack(inputStream: InputStream): IconPackImportResult = withContext(ioDispatcher) {
        val result = importer.importFromZipStream(inputStream)
        if (result is IconPackImportResult.Success) {
            refreshInstalledPacks()
            selectPack(result.pack.id)
        }
        result
    }

    override suspend fun deletePack(packId: String): Boolean = withContext(ioDispatcher) {
        if (packId == BuiltInIconPack.DEFAULT_PACK_ID) return@withContext false
        val deleted = storage.deletePack(packId)
        if (deleted) {
            // Invalidate cached pictures for this pack
            val prefix = "${packId}_"
            synchronized(pictureCacheLock) {
                val toRemove = pictureCache.keys.filter { it.startsWith(prefix) }
                toRemove.forEach { pictureCache.remove(it) }
            }
            refreshInstalledPacks()
        }
        deleted
    }

    override fun resolveIcon(key: WeatherIconKey): WeatherIconAsset {
        return resolver.resolve(
            key         = key,
            activePack  = _activePack.value,
            defaultPack = BuiltInIconPack.instance,
        )
    }

    override fun getOrLoadPicture(asset: WeatherIconAsset): Picture? {
        val cacheKey = when (asset) {
            is WeatherIconAsset.VectorFile -> "${activePack.value.id}_${asset.key.name}"
            is WeatherIconAsset.VectorContent -> "${BuiltInIconPack.DEFAULT_PACK_ID}_${asset.key.name}"
            is WeatherIconAsset.BuiltInFallback,
            is WeatherIconAsset.Missing -> return null
        }

        synchronized(pictureCacheLock) {
            pictureCache[cacheKey]
        }?.let { return it }

        // Cache miss: parse SVG safely into Picture
        val picture = try {
            when (asset) {
                is WeatherIconAsset.VectorFile -> {
                    FileInputStream(asset.file).use { fis ->
                        SVG.getFromInputStream(fis).renderToPicture()
                    }
                }
                is WeatherIconAsset.VectorContent -> {
                    ByteArrayInputStream(asset.svgContent.toByteArray(Charsets.UTF_8)).use { bais ->
                        SVG.getFromInputStream(bais).renderToPicture()
                    }
                }
            }
        } catch (e: Exception) {
            null
        }

        if (picture != null) {
            synchronized(pictureCacheLock) {
                pictureCache[cacheKey] = picture
            }
        }

        return picture
    }

    private fun prewarmPack(pack: WeatherIconPack) {
        scope.launch(ioDispatcher) {
            for (key in WeatherIconKey.entries) {
                if (key == WeatherIconKey.UNKNOWN) continue
                val asset = resolver.resolve(key, pack, BuiltInIconPack.instance)
                getOrLoadPicture(asset)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: WeatherIconPackManager? = null

        private val Context.iconPackDataStore: DataStore<Preferences> by preferencesDataStore(name = "misty_icon_pack_prefs")

        fun getInstance(context: Context): WeatherIconPackManager {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val appContext = context.applicationContext
                    val packsDir = File(appContext.filesDir, "icon_packs")
                    val stagingDir = File(appContext.filesDir, "staging_icon_packs")
                    val storage = DataStoreWeatherIconStorage(appContext.iconPackDataStore, packsDir)
                    val importer = WeatherIconPackImporter(stagingDir, packsDir)
                    DefaultWeatherIconPackManager(storage, importer).also { instance = it }
                }
            }
        }

        fun setInstanceForTesting(manager: WeatherIconPackManager?) {
            instance = manager
        }
    }
}
