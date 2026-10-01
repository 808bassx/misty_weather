package com.misty.weather.weather.location

// ─── Location Result ──────────────────────────────────────────────────────────
//
// Phase 4.3: Sealed result hierarchy for geographic location retrieval.
//
// Decouples location acquisition (GPS, Network, Mock, Fake) from ViewModel & UI.
//
// ─────────────────────────────────────────────────────────────────────────────

sealed interface LocationResult {
    /**
     * Successfully resolved geographic coordinates.
     */
    data class Success(
        val latitude: Double,
        val longitude: Double,
    ) : LocationResult

    /**
     * User denied runtime location permissions.
     * System falls back to default/selected city without disruption.
     */
    data object PermissionDenied : LocationResult

    /**
     * Location services are disabled, timed out, or unavailable.
     */
    data object Unavailable : LocationResult
}
