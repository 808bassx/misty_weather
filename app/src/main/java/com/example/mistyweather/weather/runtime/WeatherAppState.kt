package com.example.mistyweather.weather.runtime

// ─── Weather App State ────────────────────────────────────────────────────────
//
// Phase 4.1: Runtime Experience & State Architecture.
//
// Encapsulates the overall operational state of the weather application:
// connectivity health, background syncing state, last refresh timestamp,
// and active diagnostic errors.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Global operational state of the weather application.
 *
 * @param connection            Active connectivity health status.
 * @param isSyncing             Whether a background or user-initiated sync is in progress.
 * @param lastSuccessfulSyncEpoch Milliseconds timestamp of last successful data refresh.
 * @param activeError           Current active error if any, or null when healthy.
 */
data class WeatherAppState(
    val connection: WeatherConnection = WeatherConnection.Online,
    val isSyncing: Boolean = false,
    val lastSuccessfulSyncEpoch: Long = System.currentTimeMillis(),
    val activeError: WeatherError? = null,
) {
    /** True if the app has an active error condition. */
    val hasError: Boolean get() = activeError != null

    /** True if operating normally online without active errors. */
    val isHealthy: Boolean get() = connection.isOnline && activeError == null
}
