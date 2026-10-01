package com.misty.weather.weather.location

// ─── Location Provider Interface ──────────────────────────────────────────────
//
// Phase 4.3: Location Provider contract.
//
// Allows seamless switching between real Android LocationManager, Network location,
// or Hermetic In-Memory Fakes for testing.
//
// ─────────────────────────────────────────────────────────────────────────────

interface LocationProvider {
    /**
     * Queries current geographic coordinates asynchronously.
     */
    suspend fun getCurrentLocation(): LocationResult
}

/**
 * Fake location provider for testing and deterministic previews.
 */
class FakeLocationProvider(
    var resultToReturn: LocationResult = LocationResult.Success(35.6762, 139.6503),
) : LocationProvider {
    override suspend fun getCurrentLocation(): LocationResult = resultToReturn
}
