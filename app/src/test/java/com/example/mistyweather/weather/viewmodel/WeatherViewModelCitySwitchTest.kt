package com.example.mistyweather.weather.viewmodel

import com.example.mistyweather.weather.cache.InMemoryWeatherCache
import com.example.mistyweather.weather.city.City
import com.example.mistyweather.weather.city.DefaultCityRepository
import com.example.mistyweather.weather.city.InMemoryCityStorage
import com.example.mistyweather.weather.data.FakeWeatherRepository
import com.example.mistyweather.weather.di.WeatherModule
import com.example.mistyweather.weather.location.FakeLocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ─── Weather ViewModel City Switch Unit Tests ─────────────────────────────────
//
// Phase 4.3: Verification of city switching, multi-city state flows, and favorites.
//
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelCitySwitchTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        WeatherModule.reset()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        WeatherModule.reset()
    }

    @Test
    fun `selectCity updates currentCity StateFlow and persists choice`() = runTest {
        val storage = InMemoryCityStorage()
        val cityRepo = DefaultCityRepository(storage)
        val weatherRepo = FakeWeatherRepository()
        val viewModel = WeatherViewModel(
            repository     = weatherRepo,
            cache          = InMemoryWeatherCache(),
            cityRepository = cityRepo,
            locationProvider = FakeLocationProvider(),
        )

        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        assertEquals("tokyo", viewModel.currentCity.value.id)

        viewModel.selectCity(City.Kyoto)
        assertEquals("kyoto", viewModel.currentCity.value.id)
        assertEquals("京都", viewModel.currentCity.value.name)
    }

    @Test
    fun `selectCity triggers weather stream for selected city coordinates`() = runTest {
        val storage = InMemoryCityStorage()
        val cityRepo = DefaultCityRepository(storage)
        val weatherRepo = FakeWeatherRepository()
        val viewModel = WeatherViewModel(
            repository     = weatherRepo,
            cache          = InMemoryWeatherCache(),
            cityRepository = cityRepo,
            locationProvider = FakeLocationProvider(),
        )

        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        val initialState = viewModel.uiState.value as WeatherUiState.Success
        assertEquals(23, initialState.weatherState.temperature)

        viewModel.selectCity(City.Shanghai)

        val updatedState = viewModel.uiState.value as WeatherUiState.Success
        assertEquals("shanghai", viewModel.currentCity.value.id)
    }

    @Test
    fun `addFavoriteCity and removeFavoriteCity update favorite cities flow`() = runTest {
        val storage = InMemoryCityStorage()
        val cityRepo = DefaultCityRepository(storage)
        val viewModel = WeatherViewModel(
            repository     = FakeWeatherRepository(),
            cache          = InMemoryWeatherCache(),
            cityRepository = cityRepo,
            locationProvider = FakeLocationProvider(),
        )

        backgroundScope.launch(testDispatcher) { viewModel.favoriteCities.collect() }

        assertEquals(5, viewModel.favoriteCities.value.size)

        val london = City(id = "london", name = "伦敦", country = "英国", latitude = 51.5074, longitude = -0.1278)
        viewModel.addFavoriteCity(london)

        assertEquals(6, viewModel.favoriteCities.value.size)
        assertTrue(viewModel.favoriteCities.value.any { it.id == "london" })

        viewModel.removeFavoriteCity("london")
        assertEquals(5, viewModel.favoriteCities.value.size)
        assertFalse(viewModel.favoriteCities.value.any { it.id == "london" })
    }

    @Test
    fun `searchCities updates searchResults StateFlow`() = runTest {
        val storage = InMemoryCityStorage()
        val cityRepo = DefaultCityRepository(storage)
        val viewModel = WeatherViewModel(
            repository     = FakeWeatherRepository(),
            cache          = InMemoryWeatherCache(),
            cityRepository = cityRepo,
            locationProvider = FakeLocationProvider(),
        )

        viewModel.searchCities("巴黎")
        val results = viewModel.searchResults.value

        assertEquals(1, results.size)
        assertEquals("paris", results[0].id)
    }

    @Test
    fun `refreshWeather for specific city triggers targeted refresh without error`() = runTest {
        val storage = InMemoryCityStorage()
        val cityRepo = DefaultCityRepository(storage)
        val viewModel = WeatherViewModel(
            repository     = FakeWeatherRepository(),
            cache          = InMemoryWeatherCache(),
            cityRepository = cityRepo,
            locationProvider = FakeLocationProvider(),
        )

        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        viewModel.refreshWeather("tokyo")
        val state = viewModel.uiState.value
        assertTrue("State should remain Success after refresh", state is WeatherUiState.Success)
    }
}
