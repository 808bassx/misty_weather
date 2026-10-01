package com.misty.weather.weather.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.misty.weather.core.weather.ForecastSlot
import com.misty.weather.core.weather.WeatherPreview
import com.misty.weather.weather.atmosphere.TimeOfDay
import com.misty.weather.weather.cache.CachedWeather
import com.misty.weather.weather.cache.WeatherCache
import com.misty.weather.weather.city.City
import com.misty.weather.weather.city.CityRepository
import com.misty.weather.weather.data.FakeWeatherRepository
import com.misty.weather.weather.di.WeatherModule
import com.misty.weather.weather.domain.WeatherMapper
import com.misty.weather.weather.domain.WeatherRenderState
import com.misty.weather.weather.domain.WeatherRepository
import com.misty.weather.weather.domain.WeatherState
import com.misty.weather.weather.domain.usecase.DefaultGetCurrentWeatherUseCase
import com.misty.weather.weather.domain.usecase.GetCurrentWeatherUseCase
import com.misty.weather.weather.location.LocationProvider
import com.misty.weather.weather.location.LocationResult
import com.misty.weather.weather.runtime.WeatherAppState
import com.misty.weather.weather.runtime.WeatherConnection
import com.misty.weather.weather.runtime.WeatherError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

// ─── Weather UI State ─────────────────────────────────────────────────────────
//
// Phase 4.1.5, 4.2 & 4.3: Weather State Machine.
//
// Complete lifecycle states:
//   • Loading: cold boot before cache retrieval.
//   • Success: active, healthy weather data.
//   • Refreshing: seamless background sync preserving the previous visual state.
//   • Error: error state retaining last-known cache for uninterrupted Canvas rendering.
//
// ─────────────────────────────────────────────────────────────────────────────

sealed interface WeatherUiState {
    /** Cold boot initialization before initial cache read. */
    data object Loading : WeatherUiState

    /**
     * Active weather state delivery.
     *
     * @param weatherState     Pure domain state.
     * @param renderState      Frozen contract consumed by [CinematicWeatherScene].
     * @param preview          Compatibility presentation model.
     * @param forecastSlots    Hourly slots.
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
     * Background refresh state.
     * Preserves the previous [Success] state so Canvas rendering and glass cards never flicker.
     */
    data class Refreshing(
        val previous: Success,
    ) : WeatherUiState

    /**
     * Error state preserving last-known weather data for seamless fallback rendering.
     */
    data class Error(
        val error: WeatherError,
        val lastKnown: Success?,
        val activeTimeOfDay: TimeOfDay,
        val canRetry: Boolean = true,
    ) : WeatherUiState
}

// ─── Weather ViewModel ───────────────────────────────────────────────────────

/**
 * ViewModel orchestrating domain weather data, persistent disk cache ([WeatherCache]),
 * user city management ([CityRepository]), location abstraction ([LocationProvider]),
 * and the frozen [WeatherRenderState] rendering contract.
 */
class WeatherViewModel(
    private val repository: WeatherRepository = WeatherModule.container.weatherRepository,
    private val cache: WeatherCache = WeatherModule.container.weatherCache,
    private val cityRepository: CityRepository = WeatherModule.container.cityRepository,
    private val locationProvider: LocationProvider = WeatherModule.container.locationProvider,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase = DefaultGetCurrentWeatherUseCase(cityRepository, repository),
) : ViewModel() {

    private val _timeOfDayOverride = MutableStateFlow<TimeOfDay?>(null)
    private val _activeError = MutableStateFlow<WeatherError?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _appState = MutableStateFlow(WeatherAppState())

    private val _currentCity = MutableStateFlow(City.Tokyo)
    val currentCity: StateFlow<City> = _currentCity.asStateFlow()

    val favoriteCities: StateFlow<List<City>> = cityRepository.getFavoriteCities()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = City.DefaultCities,
        )

    private val _searchResults = MutableStateFlow<List<City>>(emptyList())
    val searchResults: StateFlow<List<City>> = _searchResults.asStateFlow()

    private val _locationNotice = MutableStateFlow<String?>(null)
    val locationNotice: StateFlow<String?> = _locationNotice.asStateFlow()

    // Memory cache of last valid success state for error fallback & seamless refreshing
    private var lastKnownSuccess: WeatherUiState.Success? = null

    init {
        // Observe current active city from storage
        viewModelScope.launch {
            cityRepository.getCurrentCity().collect { city ->
                _currentCity.value = city
            }
        }

        // Cold start: prioritize reading persistent cache from disk
        viewModelScope.launch {
            val cached = cache.load()
            if (cached != null) {
                val preview = WeatherMapper.toWeatherPreview(cached.weatherState, cached.renderState.timeOfDay)
                val forecast = WeatherMapper.toForecastSlots(cached.weatherState.hourlyForecast)
                val success = WeatherUiState.Success(
                    weatherState             = cached.weatherState,
                    renderState              = cached.renderState,
                    preview                  = preview,
                    forecastSlots            = forecast,
                    activeTimeOfDay          = cached.renderState.timeOfDay,
                    lastRefreshedEpochMillis = cached.timestamp,
                )
                lastKnownSuccess = success
            } else {
                // Initialize default cache
                val defaultState = FakeWeatherRepository.canonicalTokyoWeather
                val (defaultTime, defaultProgress) = resolveTimeAndProgress(null)
                val renderState = WeatherMapper.toRenderState(defaultState.weatherType, defaultTime, defaultProgress)
                val defaultSuccess = WeatherUiState.Success(
                    weatherState             = defaultState,
                    renderState              = renderState,
                    preview                  = WeatherMapper.toWeatherPreview(defaultState, defaultTime),
                    forecastSlots            = WeatherMapper.toForecastSlots(defaultState.hourlyForecast),
                    activeTimeOfDay          = defaultTime,
                    lastRefreshedEpochMillis = System.currentTimeMillis(),
                )
                lastKnownSuccess = defaultSuccess
                cache.save(CachedWeather("tokyo", defaultState, renderState, System.currentTimeMillis()))
            }
        }
    }

    /**
     * Global runtime connection & sync telemetry.
     */
    val appState: StateFlow<WeatherAppState> = _appState.asStateFlow()

    /**
     * Reactive UI state combining Use Case stream, environmental time,
     * and runtime sync states.
     */
    val uiState: StateFlow<WeatherUiState> = combine(
        getCurrentWeatherUseCase(),
        _timeOfDayOverride,
        _activeError,
        _isRefreshing,
    ) { domainState, overrideTime, activeError, isRefreshing ->
        val (activeTime, progress) = resolveTimeAndProgress(overrideTime)

        val renderState = WeatherMapper.toRenderState(
            weatherType  = domainState.weatherType,
            timeOfDay    = activeTime,
            timeProgress = progress,
        )
        val preview = WeatherMapper.toWeatherPreview(domainState, activeTime)
        val forecastSlots = WeatherMapper.toForecastSlots(domainState.hourlyForecast)

        val successState = WeatherUiState.Success(
            weatherState             = domainState,
            renderState              = renderState,
            preview                  = preview,
            forecastSlots            = forecastSlots,
            activeTimeOfDay          = activeTime,
            lastRefreshedEpochMillis = System.currentTimeMillis(),
        )

        // Update memory and disk cache
        lastKnownSuccess = successState
        val city = _currentCity.value
        val cacheKey = if (city.isCurrentLocation) "current_location" else city.id
        cache.save(CachedWeather(cacheKey, domainState, renderState, System.currentTimeMillis()))

        when {
            activeError != null -> {
                WeatherUiState.Error(
                    error           = activeError,
                    lastKnown       = lastKnownSuccess,
                    activeTimeOfDay = activeTime,
                    canRetry        = true,
                )
            }
            isRefreshing -> {
                WeatherUiState.Refreshing(previous = successState)
            }
            else -> successState
        }
    }.catch { throwable ->
        val error = WeatherError.fromThrowable(throwable)
        val (activeTime, _) = resolveTimeAndProgress(_timeOfDayOverride.value)
        _activeError.value = error
        _appState.value = _appState.value.copy(activeError = error)

        emit(
            WeatherUiState.Error(
                error           = error,
                lastKnown       = lastKnownSuccess,
                activeTimeOfDay = activeTime,
                canRetry        = true,
            )
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeatherUiState.Loading,
    )

    /**
     * Switches the active city and saves to repository.
     */
    fun selectCity(city: City) {
        viewModelScope.launch {
            _currentCity.value = city
            cityRepository.setCurrentCity(city)
            _activeError.value = null
        }
    }

    /**
     * Adds a city to the user's favorites list.
     */
    fun addFavoriteCity(city: City) {
        viewModelScope.launch {
            cityRepository.addFavorite(city)
        }
    }

    /**
     * Removes a city from the user's favorites list.
     */
    fun removeFavoriteCity(cityId: String) {
        viewModelScope.launch {
            cityRepository.removeFavorite(cityId)
        }
    }

    /**
     * Searches cities from catalog.
     */
    fun searchCities(query: String) {
        viewModelScope.launch {
            _searchResults.value = cityRepository.searchCities(query)
        }
    }

    /**
     * Requests location update from [LocationProvider].
     */
    fun requestCurrentLocation() {
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val result = locationProvider.getCurrentLocation()) {
                is LocationResult.Success -> {
                    val locationCity = City(
                        id                = "current_location",
                        name              = "当前位置",
                        country           = "定位",
                        latitude          = result.latitude,
                        longitude         = result.longitude,
                        isCurrentLocation = true,
                    )
                    selectCity(locationCity)
                    _locationNotice.value = null
                }
                LocationResult.PermissionDenied -> {
                    _locationNotice.value = "未授予位置权限，保留当前城市"
                }
                LocationResult.Unavailable -> {
                    _locationNotice.value = "无法获取当前位置，保留当前城市"
                }
            }
            _isRefreshing.value = false
        }
    }

    /**
     * Handler when location permission is denied by the user in the UI.
     */
    fun onLocationPermissionDenied() {
        _locationNotice.value = "未授予位置权限，保留当前城市"
    }

    /**
     * Clears transient location notices.
     */
    fun clearLocationNotice() {
        _locationNotice.value = null
    }

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
     * Triggers a refresh while transitioning to [WeatherUiState.Refreshing].
     */
    fun refreshWeather(cityId: String = _currentCity.value.id) {
        viewModelScope.launch {
            _isRefreshing.value = true
            _appState.value = _appState.value.copy(isSyncing = true)
            try {
                val city = _currentCity.value
                if (city.id == cityId) {
                    getCurrentWeatherUseCase.refresh()
                } else {
                    repository.refreshWeather(cityId)
                }
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
            } finally {
                _isRefreshing.value = false
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
            cache: WeatherCache = WeatherModule.container.weatherCache,
            cityRepository: CityRepository = WeatherModule.container.cityRepository,
            locationProvider: LocationProvider = WeatherModule.container.locationProvider,
            getCurrentWeatherUseCase: GetCurrentWeatherUseCase = WeatherModule.container.getCurrentWeatherUseCase,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WeatherViewModel(repository, cache, cityRepository, locationProvider, getCurrentWeatherUseCase) as T
            }
        }
    }
}
