package com.misty.weather.weather.location

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// ─── Location Provider Unit Tests ─────────────────────────────────────────────
//
// Phase 4.3: Location provider contract and sealed result verification.
//
// ─────────────────────────────────────────────────────────────────────────────

class LocationProviderTest {

    @Test
    fun `FakeLocationProvider returns configured Success location`() = runTest {
        val provider = FakeLocationProvider(LocationResult.Success(35.6762, 139.6503))
        val result = provider.getCurrentLocation()

        assertTrue(result is LocationResult.Success)
        val success = result as LocationResult.Success
        assertEquals(35.6762, success.latitude, 0.0001)
        assertEquals(139.6503, success.longitude, 0.0001)
    }

    @Test
    fun `FakeLocationProvider returns PermissionDenied when user declines permission`() = runTest {
        val provider = FakeLocationProvider(LocationResult.PermissionDenied)
        val result = provider.getCurrentLocation()

        assertEquals(LocationResult.PermissionDenied, result)
    }

    @Test
    fun `FakeLocationProvider returns Unavailable when GPS services disabled`() = runTest {
        val provider = FakeLocationProvider(LocationResult.Unavailable)
        val result = provider.getCurrentLocation()

        assertEquals(LocationResult.Unavailable, result)
    }

    @Test
    fun `LocationResult types represent correct sealed states`() {
        val success: LocationResult = LocationResult.Success(34.6937, 135.5023)
        val denied: LocationResult = LocationResult.PermissionDenied
        val unavailable: LocationResult = LocationResult.Unavailable

        assertTrue(success is LocationResult.Success)
        assertEquals(LocationResult.PermissionDenied, denied)
        assertEquals(LocationResult.Unavailable, unavailable)
    }
}
