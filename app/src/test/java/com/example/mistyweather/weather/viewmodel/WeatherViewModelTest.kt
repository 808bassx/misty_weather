package com.example.mistyweather.weather.viewmodel

import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import com.example.mistyweather.weather.data.FakeWeatherRepository
import com.example.mistyweather.weather.di.WeatherContainer
import com.example.mistyweather.weather.di.WeatherModule
import com.example.mistyweather.weather.domain.WeatherCondition
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ─── Weather ViewModel Tests ──────────────────────────────────────────────────
//
// Phase 4.0: Offline First Weather Pipeline & DI Integration Tests.
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
}
