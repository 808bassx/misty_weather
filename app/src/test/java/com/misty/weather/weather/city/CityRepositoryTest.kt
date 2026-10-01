package com.misty.weather.weather.city

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

// ─── City Repository Unit Tests ───────────────────────────────────────────────
//
// Phase 4.3: High-level city management domain boundary verification.
//
// ─────────────────────────────────────────────────────────────────────────────

class CityRepositoryTest {

    @Test
    fun `getFavoriteCities returns default cities on initial state`() = runTest {
        val storage = InMemoryCityStorage()
        val repo = DefaultCityRepository(storage)

        val favorites = repo.getFavoriteCities().first()
        assertEquals(5, favorites.size)
        assertEquals("tokyo", favorites[0].id)
    }

    @Test
    fun `getCurrentCity returns Tokyo initially`() = runTest {
        val storage = InMemoryCityStorage()
        val repo = DefaultCityRepository(storage)

        val current = repo.getCurrentCity().first()
        assertEquals("tokyo", current.id)
        assertEquals("东京", current.name)
    }

    @Test
    fun `addFavorite adds new city without duplicates`() = runTest {
        val storage = InMemoryCityStorage()
        val repo = DefaultCityRepository(storage)

        val london = City(id = "london", name = "伦敦", country = "英国", latitude = 51.5074, longitude = -0.1278)
        repo.addFavorite(london)

        var favorites = repo.getFavoriteCities().first()
        assertEquals(6, favorites.size)
        assertTrue(favorites.any { it.id == "london" })

        // Adding duplicate should not increase size
        repo.addFavorite(london)
        favorites = repo.getFavoriteCities().first()
        assertEquals(6, favorites.size)
    }

    @Test
    fun `removeFavorite removes city from favorites`() = runTest {
        val storage = InMemoryCityStorage()
        val repo = DefaultCityRepository(storage)

        repo.removeFavorite("kyoto")
        val favorites = repo.getFavoriteCities().first()

        assertEquals(4, favorites.size)
        assertFalse(favorites.any { it.id == "kyoto" })
    }

    @Test
    fun `removeFavorite falls back currentCity if active city was removed`() = runTest {
        val storage = InMemoryCityStorage()
        val repo = DefaultCityRepository(storage)

        repo.setCurrentCityById("kyoto")
        assertEquals("kyoto", repo.getCurrentCity().first().id)

        repo.removeFavorite("kyoto")
        val newCurrent = repo.getCurrentCity().first()
        // Should fall back to first remaining city (Tokyo)
        assertEquals("tokyo", newCurrent.id)
    }

    @Test
    fun `setCurrentCity adds city to favorites if not already present and sets it active`() = runTest {
        val storage = InMemoryCityStorage()
        val repo = DefaultCityRepository(storage)

        val paris = City(id = "paris", name = "巴黎", country = "法国", latitude = 48.8566, longitude = 2.3522)
        repo.setCurrentCity(paris)

        val current = repo.getCurrentCity().first()
        assertEquals("paris", current.id)
        assertEquals("巴黎", current.name)

        val favorites = repo.getFavoriteCities().first()
        assertTrue(favorites.any { it.id == "paris" })
    }

    @Test
    fun `searchCities matches by Chinese name, ID, or country, and empty query returns empty list`() = runTest {
        val storage = InMemoryCityStorage()
        val repo = DefaultCityRepository(storage)

        // Empty query
        assertTrue(repo.searchCities("").isEmpty())
        assertTrue(repo.searchCities("   ").isEmpty())

        // Match by Chinese name
        val resultName = repo.searchCities("伦敦")
        assertEquals(1, resultName.size)
        assertEquals("london", resultName[0].id)

        // Match by English/Latin ID
        val resultId = repo.searchCities("tokyo")
        assertEquals(1, resultId.size)
        assertEquals("tokyo", resultId[0].id)

        // Match by country
        val resultCountry = repo.searchCities("中国")
        assertEquals(2, resultCountry.size) // Beijing and Shanghai
    }
}
