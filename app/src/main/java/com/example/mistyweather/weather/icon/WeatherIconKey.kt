package com.example.mistyweather.weather.icon

import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.weather.domain.WeatherCondition

// ─── Weather Icon Semantic Key ───────────────────────────────────────────────
//
// Phase 4.4-B0: Stable internal semantic standard for weather icons.
//
// Decouples weather condition / WMO domain logic from external icon pack filenames.
// Every icon pack maps external file paths to these canonical keys via manifest.json.
//
// ─────────────────────────────────────────────────────────────────────────────

enum class WeatherIconKey(
    val serialName: String,
    val displayName: String,
) {
    CLEAR_DAY("clear_day", "晴朗 (白天)"),
    CLEAR_NIGHT("clear_night", "晴朗 (夜间)"),
    PARTLY_CLOUDY_DAY("partly_cloudy_day", "多云 (白天)"),
    PARTLY_CLOUDY_NIGHT("partly_cloudy_night", "多云 (夜间)"),
    CLOUDY("cloudy", "阴云"),
    OVERCAST("overcast", "阴天"),
    DRIZZLE("drizzle", "毛毛雨"),
    RAIN("rain", "降雨"),
    HEAVY_RAIN("heavy_rain", "大雨"),
    THUNDERSTORM("thunderstorm", "雷雨"),
    SNOW("snow", "降雪"),
    SLEET("sleet", "雨夹雪"),
    FOG("fog", "大雾"),
    WIND("wind", "大风"),
    UNKNOWN("unknown", "未知");

    companion object {
        /**
         * Resolves canonical key from manifest serial name (case-insensitive, trimmed).
         */
        fun fromSerialName(name: String): WeatherIconKey {
            val normalized = name.trim().lowercase().replace('-', '_')
            return entries.find { it.serialName == normalized } ?: UNKNOWN
        }

        /**
         * Maps domain [WeatherCondition] and diurnal phase to canonical [WeatherIconKey].
         */
        fun fromCondition(condition: WeatherCondition, isDaytime: Boolean = true): WeatherIconKey = when (condition) {
            WeatherCondition.ClearSky -> if (isDaytime) CLEAR_DAY else CLEAR_NIGHT
            WeatherCondition.MainlyClear,
            WeatherCondition.PartlyCloudy -> if (isDaytime) PARTLY_CLOUDY_DAY else PARTLY_CLOUDY_NIGHT
            WeatherCondition.Overcast -> OVERCAST
            WeatherCondition.Fog -> FOG
            WeatherCondition.Drizzle -> DRIZZLE
            WeatherCondition.ModerateRain -> RAIN
            WeatherCondition.HeavyRain -> HEAVY_RAIN
            WeatherCondition.Thunderstorm,
            WeatherCondition.SevereThunderstorm -> THUNDERSTORM
            WeatherCondition.LightSnow,
            WeatherCondition.HeavySnow -> SNOW
        }

        /**
         * Maps legacy/fallback [WeatherType] to canonical [WeatherIconKey].
         */
        fun fromWeatherType(type: WeatherType, isDaytime: Boolean = true): WeatherIconKey = when (type) {
            WeatherType.Sunny -> if (isDaytime) CLEAR_DAY else CLEAR_NIGHT
            WeatherType.PartlyCloudy -> if (isDaytime) PARTLY_CLOUDY_DAY else PARTLY_CLOUDY_NIGHT
            WeatherType.Cloudy -> CLOUDY
            WeatherType.Rain -> RAIN
            WeatherType.Storm -> THUNDERSTORM
            WeatherType.Snow -> SNOW
            WeatherType.Fog -> FOG
        }

        /**
         * The required set of keys for coverage calculation (excluding UNKNOWN).
         */
        val requiredKeys: Set<WeatherIconKey> = entries.filter { it != UNKNOWN }.toSet()
    }
}
