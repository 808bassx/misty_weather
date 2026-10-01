package com.misty.weather.weather.runtime

// ─── Weather Connection ───────────────────────────────────────────────────────
//
// Phase 4.1: Runtime Experience & State Architecture.
//
// Abstract connectivity telemetry decoupling the UI from platform-specific
// ConnectivityManager and network listeners.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Representation of runtime network connectivity.
 */
enum class WeatherConnection {
    /** Connected to network with healthy latency. */
    Online,

    /** Device is operating offline (airplane mode or network down). */
    Offline,

    /** High packet loss or request timeouts. */
    Degraded;

    val isOnline: Boolean get() = this == Online
    val isOffline: Boolean get() = this == Offline
}
