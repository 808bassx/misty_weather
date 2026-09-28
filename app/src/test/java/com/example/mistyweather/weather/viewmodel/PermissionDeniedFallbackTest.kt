package com.example.mistyweather.weather.viewmodel

import com.example.mistyweather.weather.cache.InMemoryWeatherCache
import com.example.mistyweather.weather.city.City
import com.example.mistyweather.weather.city.DefaultCityRepository
import com.example.mistyweather.weather.city.InMemoryCityStorage
import com.example.mistyweather.weather.data.FakeWeatherRepository
import com.example.mistyweather.weather.di.WeatherModule
import com.example.mistyweather.weather.location.FakeLocationProvider
import com.example.mistyweather.weather.location.LocationResult
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ─── Permission Denied & Location Fallback Tests ──────────────────────────────
//
// Phase 4.3: Verification of on-demand permission handling, permission denied
// fallback, and location service unavailability.
//
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionDeniedFallbackTest {

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
    fun `requestCurrentLocation with PermissionDenied sets locationNotice and preserves active city`() = runTest {
        val storage = InMemoryCityStorage()
        val cityRepo = DefaultCityRepository(storage)
        val locationProvider = FakeLocationProvider(LocationResult.PermissionDenied)

        val viewModel = WeatherViewModel(
            repository       = FakeWeatherRepository(),
            cache            = InMemoryWeatherCache(),
            cityRepository   = cityRepo,
            locationProvider = locationProvider,
        )

        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        assertEquals("tokyo", viewModel.currentCity.value.id)

        viewModel.requestCurrentLocation()

        // Active city must remain tokyo without interruption
        assertEquals("tokyo", viewModel.currentCity.value.id)
        // User-friendly feedback notice is set
        assertNotNull(viewModel.locationNotice.value)
        assertTrue(viewModel.locationNotice.value!!.contains("未授予位置权限"))

        // UI state must remain Success
        assertTrue(viewModel.uiState.value is WeatherUiState.Success)
    }

    @Test
    fun `requestCurrentLocation with Unavailable sets locationNotice and preserves active city`() = runTest {
        val storage = InMemoryCityStorage()
        val cityRepo = DefaultCityRepository(storage)
        val locationProvider = FakeLocationProvider(LocationResult.Unavailable)

        val viewModel = WeatherViewModel(
            repository       = FakeWeatherRepository(),
            cache            = InMemoryWeatherCache(),
            cityRepository   = cityRepo,
            locationProvider = locationProvider,
        )

        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        viewModel.requestCurrentLocation()

        assertEquals("tokyo", viewModel.currentCity.value.id)
        assertNotNull(viewModel.locationNotice.value)
        assertTrue(viewModel.locationNotice.value!!.contains("无法获取当前位置"))
    }

    @Test
    fun `requestCurrentLocation with Success creates current_location City and activates it`() = runTest {
        val storage = InMemoryCityStorage()
        val cityRepo = DefaultCityRepository(storage)
        val locationProvider = FakeLocationProvider(LocationResult.Success(34.6937, 135.5023)) // Osaka coordinates

        val viewModel = WeatherViewModel(
            repository       = FakeWeatherRepository(),
            cache            = InMemoryWeatherCache(),
            cityRepository   = cityRepo,
            locationProvider = locationProvider,
        )

        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        viewModel.requestCurrentLocation()

        val activeCity = viewModel.currentCity.value
        assertEquals("current_location", activeCity.id)
        assertEquals("当前位置", activeCity.name)
        assertTrue(activeCity.isCurrentLocation)
        assertEquals(34.6937, activeCity.latitude, 0.0001)
        assertEquals(135.5023, activeCity.longitude, 0.0001)
        assertNull(viewModel.locationNotice.value)
    }

    @Test
    fun `onLocationPermissionDenied displays friendly message and does not alter weather presentation`() = runTest {
        val storage = InMemoryCityStorage()
        val cityRepo = DefaultCityRepository(storage)

        val viewModel = WeatherViewModel(
            repository       = FakeWeatherRepository(),
            cache            = InMemoryWeatherCache(),
            cityRepository   = cityRepo,
            locationProvider = FakeLocationProvider(),
        )

        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        viewModel.onLocationPermissionDenied()

        assertEquals("未授予位置权限，保留当前城市", viewModel.locationNotice.value)
        assertEquals("tokyo", viewModel.currentCity.value.id)
        assertTrue(viewModel.uiState.value is WeatherUiState.Success)
    }

    @Test
    fun `clearLocationNotice clears transient location notice message`() = runTest {
        val storage = InMemoryCityStorage()
        val cityRepo = DefaultCityRepository(storage)

        val viewModel = WeatherViewModel(
            repository       = FakeWeatherRepository(),
            cache            = InMemoryWeatherCache(),
            cityRepository   = cityRepo,
            locationProvider = FakeLocationProvider(),
        )

        viewModel.onLocationPermissionDenied()
        assertNotNull(viewModel.locationNotice.value)

        viewModel.clearLocationNotice()
        assertNull(viewModel.locationNotice.value)
    }
}
