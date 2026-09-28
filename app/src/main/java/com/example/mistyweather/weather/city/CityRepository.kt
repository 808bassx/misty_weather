package com.example.mistyweather.weather.city

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

// ─── City Repository Contract & Implementation ────────────────────────────────
//
// Phase 4.3: High-level city management domain boundary.
//
// Coordinates favorite cities, current active city, and search catalogs.
//
// ─────────────────────────────────────────────────────────────────────────────

interface CityRepository {
    fun getFavoriteCities(): Flow<List<City>>
    fun getCurrentCity(): Flow<City>
    suspend fun addFavorite(city: City)
    suspend fun removeFavorite(cityId: String)
    suspend fun setCurrentCity(city: City)
    suspend fun setCurrentCityById(cityId: String)
    suspend fun searchCities(query: String): List<City>
}

class DefaultCityRepository(
    private val storage: CityStorage,
    private val availableCatalog: List<City> = CatalogPresetCities,
) : CityRepository {

    override fun getFavoriteCities(): Flow<List<City>> = storage.getFavoriteCities()

    override fun getCurrentCity(): Flow<City> = combine(
        storage.getCurrentCityId(),
        storage.getFavoriteCities(),
    ) { currentId, favorites ->
        favorites.find { it.id == currentId }
            ?: availableCatalog.find { it.id == currentId }
            ?: City.Tokyo
    }

    override suspend fun addFavorite(city: City) {
        val currentFavorites = storage.getFavoriteCities().first().toMutableList()
        if (currentFavorites.none { it.id == city.id }) {
            currentFavorites.add(city.copy(isFavorite = true))
            storage.saveFavoriteCities(currentFavorites)
        }
    }

    override suspend fun removeFavorite(cityId: String) {
        val currentFavorites = storage.getFavoriteCities().first().toMutableList()
        currentFavorites.removeAll { it.id == cityId }
        storage.saveFavoriteCities(currentFavorites)

        val currentId = storage.getCurrentCityId().first()
        if (currentId == cityId) {
            val fallbackId = currentFavorites.firstOrNull()?.id ?: "tokyo"
            storage.saveCurrentCityId(fallbackId)
        }
    }

    override suspend fun setCurrentCity(city: City) {
        addFavorite(city)
        storage.saveCurrentCityId(city.id)
    }

    override suspend fun setCurrentCityById(cityId: String) {
        storage.saveCurrentCityId(cityId)
    }

    override suspend fun searchCities(query: String): List<City> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return emptyList()
        return availableCatalog.filter { city ->
            city.name.lowercase().contains(trimmed) ||
                city.id.lowercase().contains(trimmed) ||
                city.country.lowercase().contains(trimmed)
        }
    }

    companion object {
        val CatalogPresetCities = listOf(
            City.Tokyo,
            City.Kyoto,
            City.Osaka,
            City.Shanghai,
            City.Beijing,
            City(id = "london", name = "伦敦", country = "英国", latitude = 51.5074, longitude = -0.1278),
            City(id = "newyork", name = "纽约", country = "美国", latitude = 40.7128, longitude = -74.0060),
            City(id = "paris", name = "巴黎", country = "法国", latitude = 48.8566, longitude = 2.3522),
            City(id = "sydney", name = "悉尼", country = "澳大利亚", latitude = -33.8688, longitude = 151.2093),
            City(id = "seoul", name = "首尔", country = "韩国", latitude = 37.5665, longitude = 126.9780),
        )
    }
}
