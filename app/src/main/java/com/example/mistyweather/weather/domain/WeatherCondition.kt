package com.example.mistyweather.weather.domain

import com.example.mistyweather.core.weather.WeatherType

// ─── Weather Condition ────────────────────────────────────────────────────────
//
// Phase 3.6 Domain Layer: Pure domain representation of meteorological conditions.
//
// Decouples external data sources (Open-Meteo WMO codes, NOAA, etc.) from the
// internal rendering engine [WeatherType].
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Domain meteorological condition with localized descriptions and mapping to [WeatherType].
 */
enum class WeatherCondition(
    val displayName: String,
    val canonicalWeatherType: WeatherType,
) {
    ClearSky("晴空万里", WeatherType.Sunny),
    MainlyClear("晴间多云", WeatherType.PartlyCloudy),
    PartlyCloudy("多云", WeatherType.PartlyCloudy),
    Overcast("阴天", WeatherType.Cloudy),
    Fog("大雾弥漫", WeatherType.Fog),
    Drizzle("毛毛细雨", WeatherType.Rain),
    ModerateRain("中雨", WeatherType.Rain),
    HeavyRain("大雨倾盆", WeatherType.Rain),
    Thunderstorm("雷阵雨", WeatherType.Storm),
    SevereThunderstorm("强烈雷暴", WeatherType.Storm),
    LightSnow("小雪飘落", WeatherType.Snow),
    HeavySnow("大雪纷飞", WeatherType.Snow);

    companion object {
        /**
         * Resolves WMO weather codes (0-99) into pure domain [WeatherCondition].
         * Prepared for Phase 4 Open-Meteo REST API integration.
         */
        fun fromWmoCode(code: Int): WeatherCondition = when (code) {
            0 -> ClearSky
            1 -> MainlyClear
            2 -> PartlyCloudy
            3 -> Overcast
            45, 48 -> Fog
            51, 53, 55, 56, 57 -> Drizzle
            61, 63 -> ModerateRain
            65, 66, 67, 80, 81, 82 -> HeavyRain
            71, 73, 75, 77, 85, 86 -> LightSnow
            95 -> Thunderstorm
            96, 99 -> SevereThunderstorm
            else -> if (code in 70..79) HeavySnow else Overcast
        }
    }
}
