package com.example.mistyweather.weather.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mistyweather.core.weather.ForecastSlot
import com.example.mistyweather.core.weather.WeatherPreview
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import com.example.mistyweather.weather.di.WeatherModule
import com.example.mistyweather.weather.domain.WeatherMapper
import com.example.mistyweather.weather.domain.WeatherRenderState
import com.example.mistyweather.weather.domain.WeatherRepository
import com.example.mistyweather.weather.domain.WeatherState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

// ─── Weather UI State ─────────────────────────────────────────────────────────
//
// Phase 4.0: Offline First Weather Pipeline State.
//
// Encapsulates both domain data models and the frozen rendering contract.
//
// ─────────────────────────────────────────────────────────────────────────────

sealed interface WeatherUiState {
    /** Initial or re-fetching state. */
    object Loading : WeatherUiState

    /**
     * Successful weather state delivery.
     *
     * @param weatherState     Pure domain state (temperatures, sunrise/sunset, hourly slots).
     * @param renderState      Frozen contract consumed by [CinematicWeatherScene].
     * @param preview          Compatibility presentation model for home screen header/details.
     * @param forecastSlots    Hourly slots for the horizontal forecast card.
     * @param activeTimeOfDay  Currently active diurnal lighting phase.
     */
    data class Success(
        val weatherState: WeatherState,
        val renderState: WeatherRenderState,
        val preview: WeatherPreview,
        val forecastSlots: List<ForecastSlot>,
        val activeTimeOfDay: TimeOfDay,
    ) : WeatherUiState

    /** Error state with diagnostic message. */
    data class Error(val message: String) : WeatherUiState
}

// ─── Weather ViewModel ───────────────────────────────────────────────────────

/**
 * ViewModel driving the weather feature and home screen.
 *
 * Consumes [WeatherRepository] supplied via dependency injection ([WeatherModule]).
 * Dispatches pure domain models to [WeatherMapper] to generate the immutable
 * [WeatherRenderState] for the Canvas rendering engine.
 */
class WeatherViewModel(
    private val repository: WeatherRepository = WeatherModule.container.weatherRepository,
) : ViewModel() {

    private val _timeOfDayOverride = MutableStateFlow<TimeOfDay?>(null)

    /**
     * Current reactive UI state combining repository stream and environmental time.
     */
    val uiState: StateFlow<WeatherUiState> = combine(
        repository.getWeather("tokyo"),
        _timeOfDayOverride,
    ) { domainState, overrideTime ->
        val (activeTime, progress) = resolveTimeAndProgress(overrideTime)

        val renderState = WeatherMapper.toRenderState(
            weatherType  = domainState.weatherType,
            timeOfDay    = activeTime,
            timeProgress = progress,
        )

        val preview = WeatherMapper.toWeatherPreview(domainState, activeTime)
        val forecastSlots = WeatherMapper.toForecastSlots(domainState.hourlyForecast)

        WeatherUiState.Success(
            weatherState    = domainState,
            renderState     = renderState,
            preview         = preview,
            forecastSlots   = forecastSlots,
            activeTimeOfDay = activeTime,
        ) as WeatherUiState
    }.catch { error ->
        emit(WeatherUiState.Error(error.localizedMessage ?: "Unknown weather error"))
    }.stateIn(
        scope          = viewModelScope,
        started        = SharingStarted.WhileSubscribed(5_000),
        initialValue   = WeatherUiState.Loading,
    )

    /**
     * Sets manual [TimeOfDay] override (e.g. for showcase or user diurnal toggle).
     * Pass `null` to return to dynamic real-time diurnal tracking.
     */
    fun setTimeOfDay(time: TimeOfDay?) {
        _timeOfDayOverride.value = time
    }

    /**
     * Refreshes weather data from repository.
     */
    fun refreshWeather(cityId: String = "tokyo") {
        viewModelScope.launch {
            try {
                repository.refreshWeather(cityId)
            } catch (e: Exception) {
                // Handled in reactive stream
            }
        }
    }

    private fun resolveTimeAndProgress(overrideTime: TimeOfDay?): Pair<TimeOfDay, Float> {
        return if (overrideTime != null) {
            overrideTime to 0.5f
        } else {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val time = TimeOfDay.from(hour, minute)
            val progress = TimeOfDay.progressWithin(hour, minute)
            time to progress
        }
    }

    companion object {
        /**
         * Factory helper for Compose `viewModel(...)` integration with DI.
         */
        fun provideFactory(
            repository: WeatherRepository = WeatherModule.container.weatherRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WeatherViewModel(repository) as T
            }
        }
    }
}
