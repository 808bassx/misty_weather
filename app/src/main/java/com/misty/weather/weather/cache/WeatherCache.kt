package com.misty.weather.weather.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ─── Weather Cache Interface & Implementations ───────────────────────────────
//
// Phase 4.1.5: Persistent Offline Weather Cache.
//
// Supplies local persistence via DataStore Preferences.
// Enables immediate cold-start weather display before network calls.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Persistence contract for local weather caching.
 */
interface WeatherCache {
    /**
     * Persists [CachedWeather] snapshot to disk.
     */
    suspend fun save(weather: CachedWeather)

    /**
     * Reads the last known [CachedWeather] snapshot, or null if empty.
     */
    suspend fun load(): CachedWeather?

    /**
     * Clears cached weather data.
     */
    suspend fun clear()
}

// ─── DataStore Preferences Implementation ────────────────────────────────────

/**
 * Persistent [WeatherCache] backed by AndroidX DataStore Preferences.
 */
class DataStoreWeatherCache(
    private val dataStore: DataStore<Preferences>,
) : WeatherCache {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults    = true
    }

    override suspend fun save(weather: CachedWeather) {
        val dto = CachedWeatherDto.fromCachedWeather(weather)
        val serializedJson = json.encodeToString(dto)
        dataStore.edit { preferences ->
            preferences[KEY_WEATHER_CACHE] = serializedJson
        }
    }

    override suspend fun load(): CachedWeather? {
        val cachedJson = dataStore.data.map { preferences ->
            preferences[KEY_WEATHER_CACHE]
        }.firstOrNull() ?: return null

        return runCatching {
            val dto = json.decodeFromString<CachedWeatherDto>(cachedJson)
            dto.toCachedWeather()
        }.getOrNull()
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_WEATHER_CACHE)
        }
    }

    companion object {
        val KEY_WEATHER_CACHE = stringPreferencesKey("misty_weather_cache_json")
    }
}

// ─── In-Memory Implementation (For Testing & Previews) ────────────────────────

/**
 * Fast in-memory [WeatherCache] for deterministic unit tests and previews.
 */
class InMemoryWeatherCache(
    initialWeather: CachedWeather? = null,
) : WeatherCache {

    private var inMemoryStorage: CachedWeather? = initialWeather

    override suspend fun save(weather: CachedWeather) {
        inMemoryStorage = weather
    }

    override suspend fun load(): CachedWeather? = inMemoryStorage

    override suspend fun clear() {
        inMemoryStorage = null
    }
}
