package com.misty.weather.weather.icon

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ─── Weather Icon Pack Manifest ──────────────────────────────────────────────
//
// Phase 4.4-B0: Schema contract for icon pack manifest.json.
//
// Guaranteed forward and backward compatibility:
//   • Unknown keys are ignored during deserialization.
//   • Optional fields default to null or safe defaults.
//   • Validated strictly before installation.
//
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class WeatherIconPackManifest(
    val format: String = FORMAT_MISTY_ICON_PACK,
    val version: Int = 1,
    val id: String,
    val name: String,
    val author: String? = null,
    val license: String? = null,
    val description: String? = null,
    val iconFormat: String = "svg",
    val preview: String? = null,
    val sourceUrl: String? = null,
    val licenseUrl: String? = null,
    val icons: Map<String, String> = emptyMap(),
) {
    companion object {
        const val FORMAT_MISTY_ICON_PACK = "misty-weather-icon-pack"
        const val MANIFEST_FILE_NAME = "manifest.json"

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
            encodeDefaults = true
        }

        fun fromJson(jsonString: String): WeatherIconPackManifest {
            val sanitized = jsonString.trim().removePrefix("\uFEFF")
            return json.decodeFromString(serializer(), sanitized)
        }

        fun toJson(manifest: WeatherIconPackManifest): String {
            return json.encodeToString(serializer(), manifest)
        }
    }
}
