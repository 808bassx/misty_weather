package com.example.mistyweather.weather.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mistyweather.core.weather.ForecastSlot
import com.example.mistyweather.core.weather.WeatherPreview
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import com.example.mistyweather.weather.data.FakeWeatherRepository
import com.example.mistyweather.weather.di.WeatherModule
import com.example.mistyweather.weather.domain.WeatherMapper
import com.example.mistyweather.weather.domain.WeatherRenderState
import com.example.mistyweather.weather.domain.WeatherRepository
import com.example.mistyweather.weather.domain.WeatherState
import com.example.mistyweather.weather.runtime.WeatherAppState
import com.example.mistyweather.weather.runtime.WeatherConnection
import com.example.mistyweather.weather.runtime.WeatherError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

// ─── Weather UI State ─────────────────────────────────────────────────────────
//
// Phase 4.1: Runtime Experience & State Architecture.
//
// Robust state machine: Loading, Success, and Error with cache preservation.
// Protects downstream UI from crashing or blanking during network disruptions.
//
// ─────────────────────────────────────────────────────────────────────────────

sealed interface WeatherUiState {
    /** Initial or cold-start loading state. */
    data object Loading : WeatherUiState

    /**
     * Successful weather state delivery.
     *
     * @param weatherState     Pure domain state (temperatures, sunrise/sunset, hourly slots).
     * @param renderState      Frozen contract consumed by [CinematicWeatherScene].
     * @param preview          Compatibility presentation model for home screen header/details.
     * @param forecastSlots    Hourly slots for the horizontal forecast card.
     * @param activeTimeOfDay  Currently active diurnal lighting phase.
     * @param lastRefreshedEpochMillis Timestamp of this refresh.
     */
    data class Success(
        val weatherState: WeatherState,
        val renderState: WeatherRenderState,
        val preview: WeatherPreview,
        val forecastSlots: List<ForecastSlot>,
        val activeTimeOfDay: TimeOfDay,
        val lastRefreshedEpochMillis: Long = System.currentTimeMillis(),
    ) : WeatherUiState

    /**
     * Error state preserving last-known weather data for seamless fallback rendering.
     *
     * @param error                 Typed domain error.
     * @param lastKnownWeather      Cached domain state before error occurred (or baseline fallback).
     * @param lastKnownRenderState  Cached rendering contract so Canvas background never blanks out.
     * @param lastKnownPreview      Cached UI presentation model.
     * @param lastKnownForecastSlots Cached forecast list.
     * @param activeTimeOfDay       Active diurnal phase.
     * @param canRetry              Whether user can trigger a re-fetch.
     */
    data class Error(
        val error: WeatherError,
        val lastKnownWeather: WeatherState?,
        val lastKnownRenderState: WeatherRenderState?,
        val lastKnownPreview: WeatherPreview?,
        val lastKnownForecastSlots: List<ForecastSlot>?,
        val activeTimeOfDay: TimeOfDay,
        val canRetry: Boolean = true,
    ) : WeatherUiState
}

// ─── Weather ViewModel ───────────────────────────────────────────────────────

/**
 * ViewModel orchestrating weather domain data, atmospheric time, runtime errors,
 * and the frozen [WeatherRenderState] rendering contract.
 */
class WeatherViewModel(
    private val repository: WeatherRepository = WeatherModule.container.weatherRepository,
) : ViewModel() {

    private val _timeOfDayOverride = MutableStateFlow<TimeOfDay?>(null)
    private val _activeError = MutableStateFlow<WeatherError?>(null)
    private val _appState = MutableStateFlow(WeatherAppState())

    // Cache of last known successful domain & rendering models
    private var lastKnownWeather: WeatherState? = FakeWeatherRepository.canonicalTokyoWeather
    private var lastKnownRenderState: WeatherRenderState? = WeatherMapper.toRenderState(
        weatherType  = FakeWeatherRepository.canonicalTokyoWeather.weatherType,
        timeOfDay    = TimeOfDay.Noon,
        timeProgress = 0.5f,
    )
    private var lastKnownPreview: WeatherPreview? = WeatherMapper.toWeatherPreview(
        FakeWeatherRepository.canonicalTokyoWeather,
        TimeOfDay.Noon,
    )
    private var lastKnownForecastSlots: List<ForecastSlot>? = WeatherMapper.toForecastSlots(
        FakeWeatherRepository.canonicalTokyoWeather.hourlyForecast,
    )

    /**
     * Global runtime connection & sync telemetry.
     */
    val appState: StateFlow<WeatherAppState> = _appState.asStateFlow()

    /**
     * Current reactive UI state combining repository stream, environmental time,
     * and runtime error state.
     */
    val uiState: StateFlow<WeatherUiState> = combine(
        repository.getWeather("tokyo"),
        _timeOfDayOverride,
        _activeError,
    ) { domainState, overrideTime, activeError ->
        val (activeTime, progress) = resolveTimeAndProgress(overrideTime)

        val renderState = WeatherMapper.toRenderState(
            weatherType  = domainState.weatherType,
            timeOfDay    = activeTime,
            timeProgress = progress,
        )
        val preview = WeatherMapper.toWeatherPreview(domainState, activeTime)
        val forecastSlots = WeatherMapper.toForecastSlots(domainState.hourlyForecast)

        // Cache successful data for error resilience
        lastKnownWeather = domainState
        lastKnownRenderState = renderState
        lastKnownPreview = preview
        lastKnownForecastSlots = forecastSlots

        if (activeError != null) {
            WeatherUiState.Error(
                error                  = activeError,
                lastKnownWeather       = lastKnownWeather,
                lastKnownRenderState   = lastKnownRenderState,
                lastKnownPreview       = lastKnownPreview,
                lastKnownForecastSlots = lastKnownForecastSlots,
                activeTimeOfDay        = activeTime,
                canRetry               = true,
            )
        } else {
            WeatherUiState.Success(
                weatherState             = domainState,
                renderState              = renderState,
                preview                  = preview,
                forecastSlots            = forecastSlots,
                activeTimeOfDay          = activeTime,
                lastRefreshedEpochMillis = System.currentTimeMillis(),
            )
        }
    }.catch { throwable ->
        val error = WeatherError.fromThrowable(throwable)
        val (activeTime, _) = resolveTimeAndProgress(_timeOfDayOverride.value)
        _activeError.value = error
        _appState.value = _appState.value.copy(activeError = error)

        emit(
            WeatherUiState.Error(
                error                  = error,
                lastKnownWeather       = lastKnownWeather,
                lastKnownRenderState   = lastKnownRenderState,
                lastKnownPreview       = lastKnownPreview,
                lastKnownForecastSlots = lastKnownForecastSlots,
                activeTimeOfDay        = activeTime,
                canRetry               = true,
            )
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeatherUiState.Loading,
    )

    /**
     * Sets manual [TimeOfDay] override. Pass `null` to return to dynamic diurnal tracking.
     */
    fun setTimeOfDay(time: TimeOfDay?) {
        _timeOfDayOverride.value = time
    }

    /**
     * Updates runtime connection telemetry.
     */
    fun setConnection(connection: WeatherConnection) {
        _appState.value = _appState.value.copy(connection = connection)
        if (connection.isOffline) {
            _activeError.value = WeatherError.NetworkUnavailable
        } else if (_activeError.value == WeatherError.NetworkUnavailable) {
            _activeError.value = null
        }
    }

    /**
     * Triggers a refresh and clears transient errors.
     */
    fun refreshWeather(cityId: String = "tokyo") {
        viewModelScope.launch {
            _appState.value = _appState.value.copy(isSyncing = true)
            try {
                repository.refreshWeather(cityId)
                _activeError.value = null
                _appState.value = _appState.value.copy(
                    isSyncing               = false,
                    activeError             = null,
                    lastSuccessfulSyncEpoch = System.currentTimeMillis(),
                )
            } catch (e: Exception) {
                val error = WeatherError.fromThrowable(e)
                _activeError.value = error
                _appState.value = _appState.value.copy(
                    isSyncing   = false,
                    activeError = error,
                )
            }
        }
    }

    /**
     * Simulates or injects a runtime error (e.g. for testing recovery and offline display).
     */
    fun simulateError(error: WeatherError) {
        _activeError.value = error
        _appState.value = _appState.value.copy(activeError = error)
    }

    /**
     * Clears active runtime error and restores regular presentation.
     */
    fun clearError() {
        _activeError.value = null
        _appState.value = _appState.value.copy(activeError = null)
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
