package com.misty.weather.weather.city

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ─── City Storage Interface & Implementations ────────────────────────────────
//
// Phase 4.3: Local persistence for user-managed cities and active selection.
//
// Backed by DataStore Preferences for production, and in-memory for testing.
//
// ─────────────────────────────────────────────────────────────────────────────

interface CityStorage {
    fun getFavoriteCities(): Flow<List<City>>
    fun getCurrentCityId(): Flow<String>
    suspend fun saveFavoriteCities(cities: List<City>)
    suspend fun saveCurrentCityId(cityId: String)
    suspend fun clear()
}

/**
 * DataStore Preferences implementation of [CityStorage].
 */
class DataStoreCityStorage(
    private val dataStore: DataStore<Preferences>,
) : CityStorage {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun getFavoriteCities(): Flow<List<City>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_FAVORITE_CITIES]
        if (raw.isNullOrEmpty()) {
            City.DefaultCities
        } else {
            runCatching {
                json.decodeFromString<List<City>>(raw)
            }.getOrDefault(City.DefaultCities)
        }
    }

    override fun getCurrentCityId(): Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_CURRENT_CITY_ID] ?: "tokyo"
    }

    override suspend fun saveFavoriteCities(cities: List<City>) {
        val serialized = json.encodeToString(cities)
        dataStore.edit { prefs ->
            prefs[KEY_FAVORITE_CITIES] = serialized
        }
    }

    override suspend fun saveCurrentCityId(cityId: String) {
        dataStore.edit { prefs ->
            prefs[KEY_CURRENT_CITY_ID] = cityId
        }
    }

    override suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_FAVORITE_CITIES)
            prefs.remove(KEY_CURRENT_CITY_ID)
        }
    }

    companion object {
        val KEY_FAVORITE_CITIES = stringPreferencesKey("misty_favorite_cities_json")
        val KEY_CURRENT_CITY_ID = stringPreferencesKey("misty_current_city_id")
    }
}

/**
 * Hermetic in-memory [CityStorage] for testing and previews.
 */
class InMemoryCityStorage(
    initialCities: List<City> = City.DefaultCities,
    initialCurrentCityId: String = "tokyo",
) : CityStorage {

    private val _favoriteCitiesFlow = MutableStateFlow(initialCities)
    private val _currentCityIdFlow = MutableStateFlow(initialCurrentCityId)

    override fun getFavoriteCities(): Flow<List<City>> = _favoriteCitiesFlow.asStateFlow()

    override fun getCurrentCityId(): Flow<String> = _currentCityIdFlow.asStateFlow()

    override suspend fun saveFavoriteCities(cities: List<City>) {
        _favoriteCitiesFlow.value = cities
    }

    override suspend fun saveCurrentCityId(cityId: String) {
        _currentCityIdFlow.value = cityId
    }

    override suspend fun clear() {
        _favoriteCitiesFlow.value = City.DefaultCities
        _currentCityIdFlow.value = "tokyo"
    }
}
