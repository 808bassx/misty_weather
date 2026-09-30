package com.example.mistyweather

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// ─── Navigation Keys ──────────────────────────────────────────────────────────
// Phase 1: only the Home destination exists.
// Location and Settings are declared as sealed variants for future phases;
// they are NOT implemented yet.
// ─────────────────────────────────────────────────────────────────────────────

/** Home screen — the only active destination in Phase 1. */
@Serializable
data object Main : NavKey

/** Reserved for Phase 2: location/city search screen. */
@Serializable
data object LocationSearch : NavKey

/** Reserved for Phase 2: settings screen. */
@Serializable
data object Settings : NavKey

/** Visual calibration showcase screen (Phase 3.5). */
@Serializable
data object Showcase : NavKey

/** Weather icon pack management screen (Phase 4.4-B0). */
@Serializable
data object IconPackSettings : NavKey
