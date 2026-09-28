package com.example.mistyweather.weather.viewmodel

import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import com.example.mistyweather.weather.cache.CachedWeather
import com.example.mistyweather.weather.cache.InMemoryWeatherCache
import com.example.mistyweather.weather.city.DefaultCityRepository
import com.example.mistyweather.weather.city.InMemoryCityStorage
import com.example.mistyweather.weather.data.FakeWeatherRepository
import com.example.mistyweather.weather.di.WeatherContainer
import com.example.mistyweather.weather.di.WeatherModule
import com.example.mistyweather.weather.domain.WeatherCondition
import com.example.mistyweather.weather.domain.WeatherMapper
import com.example.mistyweather.weather.runtime.WeatherConnection
import com.example.mistyweather.weather.runtime.WeatherError
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// ─── Weather ViewModel Tests ──────────────────────────────────────────────────
//
// Phase 4.1.5: Persistent Offline Weather Cache & State Machine Unit Tests.
//
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

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
    fun `WeatherViewModel emits Success with canonical Tokyo data and render state`() = runTest {
        val fakeRepo = FakeWeatherRepository()
        val viewModel = WeatherViewModel(fakeRepo)

        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        val state = viewModel.uiState.value
        assertTrue("State should be Success, got $state", state is WeatherUiState.Success)
        val success = state as WeatherUiState.Success

        assertEquals("东京", success.weatherState.cityName)
        assertEquals("Tokyo", success.weatherState.cityNameLatin)
        assertEquals(23, success.weatherState.temperature)
        assertEquals(22, success.weatherState.feelsLike)
        assertEquals(71, success.weatherState.humidity)
        assertEquals(6, success.forecastSlots.size)

        // Verify render state contract
        assertEquals(success.weatherState.weatherType, success.renderState.weatherType)
        assertEquals(success.activeTimeOfDay, success.renderState.timeOfDay)
        assertNotNull(success.renderState.atmosphere.sky.zenith)
    }

    @Test
    fun `WeatherViewModel updates reactively when repository emits new weather`() = runTest {
        val fakeRepo = FakeWeatherRepository()
        val viewModel = WeatherViewModel(fakeRepo)

        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        fakeRepo.updateWeather(
            FakeWeatherRepository.canonicalTokyoWeather.copy(
                temperature = 16,
                condition   = WeatherCondition.HeavyRain,
                weatherType = WeatherType.Rain,
            )
        )

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is WeatherUiState.Success)
        val success = state as WeatherUiState.Success

        assertEquals(16, success.weatherState.temperature)
        assertEquals(WeatherType.Rain, success.weatherState.weatherType)
        assertEquals(WeatherType.Rain, success.renderState.weatherType)
    }

    @Test
    fun `WeatherViewModel respects setTimeOfDay override`() = runTest {
        val fakeRepo = FakeWeatherRepository()
        val viewModel = WeatherViewModel(fakeRepo)

        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        viewModel.setTimeOfDay(TimeOfDay.Sunset)

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Success)
        val success = state as WeatherUiState.Success

        assertEquals(TimeOfDay.Sunset, success.activeTimeOfDay)
        assertEquals(TimeOfDay.Sunset, success.renderState.timeOfDay)
    }

    @Test
    fun `WeatherModule DI container allows swapping and resetting container`() = runTest {
        val customRepo = FakeWeatherRepository(
            initialState = FakeWeatherRepository.canonicalTokyoWeather.copy(temperature = 30)
        )
        val customContainer = object : WeatherContainer {
            override val weatherRepository = customRepo
            override val weatherCache = InMemoryWeatherCache()
        }

        WeatherModule.setCustomContainer(customContainer)
        val viewModel = WeatherViewModel()
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        val state = viewModel.uiState.value as WeatherUiState.Success
        assertEquals(30, state.weatherState.temperature)

        WeatherModule.reset()
        val defaultVm = WeatherViewModel()
        backgroundScope.launch(testDispatcher) { defaultVm.uiState.collect() }

        val defaultState = defaultVm.uiState.value as WeatherUiState.Success
        assertEquals(23, defaultState.weatherState.temperature)
    }

    @Test
    fun `WeatherViewModel error recovery preserves lastKnown and renderState`() = runTest {
        val fakeRepo = FakeWeatherRepository()
        val viewModel = WeatherViewModel(fakeRepo)
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        // Initial healthy state
        assertTrue(viewModel.uiState.value is WeatherUiState.Success)

        // Inject simulated error
        viewModel.simulateError(WeatherError.RequestTimeout)

        val errorState = viewModel.uiState.value
        assertTrue("State should transition to Error", errorState is WeatherUiState.Error)
        val err = errorState as WeatherUiState.Error

        assertEquals(WeatherError.RequestTimeout, err.error)
        assertNotNull("Last known success must be preserved", err.lastKnown)
        assertNotNull("Last known weather must be preserved", err.lastKnown?.weatherState)
        assertNotNull("Last known render state must be preserved for uninterrupted canvas", err.lastKnown?.renderState)
        assertFalse(err.error.userFacingMessage.isBlank())
        assertTrue(err.canRetry)

        // Clear error and recover
        viewModel.clearError()
        assertTrue("State should recover to Success", viewModel.uiState.value is WeatherUiState.Success)
    }

    @Test
    fun `WeatherViewModel setConnection updates runtime connection and active error`() = runTest {
        val fakeRepo = FakeWeatherRepository()
        val viewModel = WeatherViewModel(fakeRepo)
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        // Device goes offline
        viewModel.setConnection(WeatherConnection.Offline)
        assertEquals(WeatherConnection.Offline, viewModel.appState.value.connection)
        val offlineState = viewModel.uiState.value
        assertTrue(offlineState is WeatherUiState.Error)
        assertEquals(WeatherError.NetworkUnavailable, (offlineState as WeatherUiState.Error).error)

        // Device reconnects
        viewModel.setConnection(WeatherConnection.Online)
        assertEquals(WeatherConnection.Online, viewModel.appState.value.connection)
        assertTrue(viewModel.uiState.value is WeatherUiState.Success)
    }

    @Test
    fun `WeatherViewModel cold start loads from persistent cache immediately`() = runTest {
        val customState = FakeWeatherRepository.canonicalTokyoWeather.copy(
            cityName    = "京都",
            temperature = 19,
        )
        val customRender = WeatherMapper.toRenderState(customState, TimeOfDay.Morning)
        val cache = InMemoryWeatherCache(
            initialWeather = CachedWeather("kyoto", customState, customRender, 1727530000000L)
        )
        val fakeRepo = FakeWeatherRepository(initialState = customState)
        val cityStorage = InMemoryCityStorage(initialCurrentCityId = "kyoto")
        val cityRepo = DefaultCityRepository(cityStorage)

        val viewModel = WeatherViewModel(
            repository = fakeRepo,
            cache = cache,
            cityRepository = cityRepo,
        )
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        val state = viewModel.uiState.value as WeatherUiState.Success
        assertEquals("京都", state.weatherState.cityName)
        assertEquals(19, state.weatherState.temperature)
        assertEquals(state.activeTimeOfDay, state.renderState.timeOfDay)
    }

    @Test
    fun `WeatherViewModel refreshWeather transitions through sync cycle successfully`() = runTest {
        val fakeRepo = FakeWeatherRepository()
        val viewModel = WeatherViewModel(fakeRepo)
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        val initial = viewModel.uiState.value
        assertTrue("Initial state must be Success", initial is WeatherUiState.Success)

        viewModel.refreshWeather("tokyo")
        val afterSync = viewModel.uiState.value
        assertTrue("State after sync must remain Success", afterSync is WeatherUiState.Success)
        assertFalse(viewModel.appState.value.isSyncing)
        assertEquals("tokyo", (afterSync as WeatherUiState.Success).preview.cityLatin.lowercase())
    }

    @Test
    fun `WeatherError fromThrowable correctly maps network exceptions`() {
        val networkErr = WeatherError.fromThrowable(UnknownHostException("No address"))
        assertEquals(WeatherError.NetworkUnavailable, networkErr)

        val timeoutErr = WeatherError.fromThrowable(SocketTimeoutException("Connect timed out"))
        assertEquals(WeatherError.RequestTimeout, timeoutErr)

        val genericErr = WeatherError.fromThrowable(IllegalStateException("Custom failure"))
        assertTrue(genericErr is WeatherError.Unknown)
        assertEquals("Custom failure", genericErr.userFacingMessage)
    }
}
