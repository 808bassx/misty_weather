package com.example.mistyweather.weather.icon

import java.io.File

// ─── Weather Icon Pack Domain Model ──────────────────────────────────────────
//
// Phase 4.4-B0: High-level representation of an installed or built-in icon pack.
//
// Encapsulates supported keys, relative file mapping, metadata, and coverage metrics.
//
// ─────────────────────────────────────────────────────────────────────────────

data class WeatherIconPack(
    val id: String,
    val name: String,
    val version: Int,
    val author: String?,
    val license: String?,
    val description: String?,
    val previewPath: String?,
    val iconCount: Int,
    val supportedKeys: Set<WeatherIconKey>,
    val source: IconPackSource,
    val rootDir: File? = null,
    val iconRelativePaths: Map<WeatherIconKey, String> = emptyMap(),
    val sourceUrl: String? = null,
    val licenseUrl: String? = null,
) {
    val isBuiltIn: Boolean get() = source == IconPackSource.BUILT_IN

    /**
     * Number of supported required keys (excluding UNKNOWN).
     */
    val coverageCount: Int get() = supportedKeys.filter { it != WeatherIconKey.UNKNOWN }.size

    /**
     * Total number of required keys in Misty Weather semantic standard.
     */
    val totalRequiredCount: Int get() = WeatherIconKey.requiredKeys.size

    /**
     * Formatted coverage ratio string (e.g. "14 / 14" or "10 / 14").
     */
    val coverageText: String get() = "$coverageCount / $totalRequiredCount"

    /**
     * Whether this pack covers 100% of required weather states.
     */
    val isCompleteCoverage: Boolean get() = coverageCount >= totalRequiredCount
}

enum class IconPackSource {
    BUILT_IN,
    USER_IMPORTED,
}
