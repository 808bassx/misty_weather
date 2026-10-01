package com.misty.weather.weather.icon

import java.io.File

// ─── Weather Icon Resolver & 3-Tier Fallback ─────────────────────────────────
//
// Phase 4.4-B0: Production-grade three-tier fallback resolution.
//
// Strategy:
//   Tier 1: User Selected Pack (User Pack SVG file or Built-in SVG)
//             ↓ (if missing or invalid)
//   Tier 2: System Default Pack (Misty Default SVG)
//             ↓ (if missing)
//   Tier 3: Built-in Geometric Fallback (Pure Canvas geometric rendering)
//
// Guarantee: An incomplete icon pack NEVER produces a blank space, broken image, or crash.
//
// ─────────────────────────────────────────────────────────────────────────────

interface WeatherIconResolver {
    fun resolve(
        key: WeatherIconKey,
        activePack: WeatherIconPack,
        defaultPack: WeatherIconPack = BuiltInIconPack.instance,
    ): WeatherIconAsset
}

class DefaultWeatherIconResolver : WeatherIconResolver {

    override fun resolve(
        key: WeatherIconKey,
        activePack: WeatherIconPack,
        defaultPack: WeatherIconPack,
    ): WeatherIconAsset {
        // ── Tier 1: User Selected Pack ───────────────────────────────────────
        if (activePack.isBuiltIn) {
            if (activePack.supportedKeys.contains(key)) {
                return WeatherIconAsset.VectorContent(BuiltInIconPack.getSvgContent(key), key)
            }
        } else if (activePack.supportedKeys.contains(key)) {
            val relPath = activePack.iconRelativePaths[key]
            if (relPath != null && activePack.rootDir != null) {
                val file = File(activePack.rootDir, relPath)
                if (file.exists() && file.isFile && file.length() > 0) {
                    return WeatherIconAsset.VectorFile(file, key)
                }
            }
        }

        // ── Tier 2: Default Pack ─────────────────────────────────────────────
        if (defaultPack.isBuiltIn) {
            if (defaultPack.supportedKeys.contains(key)) {
                return WeatherIconAsset.VectorContent(BuiltInIconPack.getSvgContent(key), key)
            }
        } else if (defaultPack.supportedKeys.contains(key)) {
            val relPath = defaultPack.iconRelativePaths[key]
            if (relPath != null && defaultPack.rootDir != null) {
                val file = File(defaultPack.rootDir, relPath)
                if (file.exists() && file.isFile && file.length() > 0) {
                    return WeatherIconAsset.VectorFile(file, key)
                }
            }
        }

        // ── Tier 3: Built-in Canvas Fallback ─────────────────────────────────
        return WeatherIconAsset.BuiltInFallback(key)
    }
}
