package com.example.mistyweather.weather.city

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// ─── City Storage Unit Tests ──────────────────────────────────────────────────
//
// Phase 4.3: Local persistence for user-managed cities and active selection.
//
// ─────────────────────────────────────────────────────────────────────────────

class CityStorageTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `InMemoryCityStorage default returns default cities and tokyo as current city`() = runTest {
        val storage = InMemoryCityStorage()
        val favorites = storage.getFavoriteCities().first()
        val currentId = storage.getCurrentCityId().first()

        assertEquals("tokyo", currentId)
        assertEquals(5, favorites.size)
        assertTrue(favorites.any { it.id == "tokyo" })
        assertTrue(favorites.any { it.id == "kyoto" })
    }

    @Test
    fun `InMemoryCityStorage saveFavoriteCities updates list and emits reactively`() = runTest {
        val storage = InMemoryCityStorage()
        val newCities = listOf(
            City("paris", "巴黎", "法国", 48.8566, 2.3522, isFavorite = true),
            City.Tokyo,
        )

        storage.saveFavoriteCities(newCities)
        val loaded = storage.getFavoriteCities().first()

        assertEquals(2, loaded.size)
        assertEquals("paris", loaded[0].id)
    }

    @Test
    fun `InMemoryCityStorage saveCurrentCityId updates current city id`() = runTest {
        val storage = InMemoryCityStorage()
        storage.saveCurrentCityId("kyoto")

        val currentId = storage.getCurrentCityId().first()
        assertEquals("kyoto", currentId)
    }

    @Test
    fun `InMemoryCityStorage clear resets to default state`() = runTest {
        val storage = InMemoryCityStorage()
        storage.saveCurrentCityId("shanghai")
        storage.saveFavoriteCities(listOf(City.Shanghai))

        storage.clear()

        assertEquals("tokyo", storage.getCurrentCityId().first())
        assertEquals(5, storage.getFavoriteCities().first().size)
    }

    @Test
    fun `City JSON serialization roundtrip preserves all fields including coordinates and favorite status`() {
        val city = City(
            id = "sapporo",
            name = "札幌",
            country = "日本",
            latitude = 43.0618,
            longitude = 141.3545,
            isFavorite = true,
            isCurrentLocation = false,
        )

        val encoded = json.encodeToString(city)
        val decoded = json.decodeFromString<City>(encoded)

        assertEquals(city.id, decoded.id)
        assertEquals(city.name, decoded.name)
        assertEquals(city.country, decoded.country)
        assertEquals(city.latitude, decoded.latitude, 0.0001)
        assertEquals(city.longitude, decoded.longitude, 0.0001)
        assertTrue(decoded.isFavorite)
        assertFalse(decoded.isCurrentLocation)
        assertEquals("Sapporo", decoded.nameLatin)
    }
}
