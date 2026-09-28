package com.example.mistyweather.weather.domain.usecase

import com.example.mistyweather.weather.city.City
import com.example.mistyweather.weather.city.DefaultCityRepository
import com.example.mistyweather.weather.city.InMemoryCityStorage
import com.example.mistyweather.weather.data.FakeWeatherRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

// ─── Weather Use Case Unit Tests ──────────────────────────────────────────────
//
// Phase 4.3-R1: Domain Use Case layer verification.
//
// Validates [DefaultGetCurrentWeatherUseCase] and [GetWeatherForCityUseCase]
// reactive stream behavior, city switching, and refresh delegation.
//
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherUseCaseTest {

    @Test
    fun `DefaultGetCurrentWeatherUseCase emits initial Tokyo weather from CityRepository`() = runTest {
        val cityStorage = InMemoryCityStorage()
        val cityRepository = DefaultCityRepository(cityStorage)
        val weatherRepository = FakeWeatherRepository()
        val useCase = DefaultGetCurrentWeatherUseCase(cityRepository, weatherRepository)

        val weatherState = useCase().first()
        assertNotNull(weatherState)
        assertEquals("东京", weatherState.cityName)
        assertEquals("Tokyo", weatherState.cityNameLatin)
    }

    @Test
    fun `DefaultGetCurrentWeatherUseCase dynamically switches weather stream on active city change`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val cityStorage = InMemoryCityStorage()
        val cityRepository = DefaultCityRepository(cityStorage)
        val weatherRepository = FakeWeatherRepository()
        val useCase = DefaultGetCurrentWeatherUseCase(cityRepository, weatherRepository)

        val states = mutableListOf<String>()
        val job = backgroundScope.launch(testDispatcher) {
            useCase().collect { state ->
                states.add(state.cityName)
            }
        }

        assertEquals(listOf("东京"), states)

        cityRepository.setCurrentCity(City.Kyoto)
        assertEquals(listOf("东京", "京都"), states)

        cityRepository.setCurrentCity(City.Shanghai)
        assertEquals(listOf("东京", "京都", "上海"), states)

        job.cancel()
    }

    @Test
    fun `DefaultGetCurrentWeatherUseCase refresh delegates to WeatherRepository for active city`() = runTest {
        val cityStorage = InMemoryCityStorage()
        val cityRepository = DefaultCityRepository(cityStorage)
        val weatherRepository = FakeWeatherRepository()
        val useCase = DefaultGetCurrentWeatherUseCase(cityRepository, weatherRepository)

        cityRepository.setCurrentCity(City.Kyoto)
        useCase.refresh()

        val weatherState = useCase().first()
        assertEquals("京都", weatherState.cityName)
    }

    @Test
    fun `GetWeatherForCityUseCase queries weather for arbitrary city`() = runTest {
        val weatherRepository = FakeWeatherRepository()
        val useCase = GetWeatherForCityUseCase(weatherRepository)

        val paris = City(
            id        = "paris",
            name      = "巴黎",
            nameLatin = "Paris",
            country   = "法国",
            latitude  = 48.8566,
            longitude = 2.3522,
        )

        val state = useCase(paris).first()
        assertEquals("巴黎", state.cityName)
        assertEquals("Paris", state.cityNameLatin)
    }

    @Test
    fun `GetWeatherForCityUseCase refresh triggers refresh on WeatherRepository`() = runTest {
        val weatherRepository = FakeWeatherRepository()
        val useCase = GetWeatherForCityUseCase(weatherRepository)

        val london = City(
            id        = "london",
            name      = "伦敦",
            nameLatin = "London",
            country   = "英国",
            latitude  = 51.5074,
            longitude = -0.1278,
        )

        useCase.refresh(london)
        val state = useCase(london).first()
        assertEquals("伦敦", state.cityName)
    }
}
