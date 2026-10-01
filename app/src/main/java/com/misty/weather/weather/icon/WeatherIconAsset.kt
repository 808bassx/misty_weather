package com.misty.weather.weather.icon

import java.io.File

// ─── Weather Icon Asset Contract ─────────────────────────────────────────────
//
// Phase 4.4-B0: Sealed hierarchy representing resolved icon assets.
//
// The UI layer only interacts with this contract and does not care whether the
// asset originated from an unpacked ZIP file, built-in vector, or geometric fallback.
//
// ─────────────────────────────────────────────────────────────────────────────

sealed interface WeatherIconAsset {
    val key: WeatherIconKey

    /**
     * File-backed vector asset (e.g. from an unzipped user icon pack on disk).
     */
    data class VectorFile(
        val file: File,
        override val key: WeatherIconKey,
    ) : WeatherIconAsset

    /**
     * In-memory SVG content asset (e.g. built-in SVG strings or cached content).
     */
    data class VectorContent(
        val svgContent: String,
        override val key: WeatherIconKey,
    ) : WeatherIconAsset

    /**
     * Ultimate safety net: lightweight geometric Canvas fallback.
     * Guaranteed never to fail or crash.
     */
    data class BuiltInFallback(
        override val key: WeatherIconKey,
    ) : WeatherIconAsset

    /**
     * Missing asset indicator for testing or diagnostics.
     */
    data class Missing(
        override val key: WeatherIconKey,
    ) : WeatherIconAsset
}
