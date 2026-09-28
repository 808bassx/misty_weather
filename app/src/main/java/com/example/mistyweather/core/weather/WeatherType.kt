package com.example.mistyweather.core.weather

// ─── Weather Domain Types ─────────────────────────────────────────────────────
// Phase 1: Types only. No API, no network, no location.
// These are the domain contracts that Weather scenes and UI bind against.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The canonical set of weather conditions Misty Weather can visualise.
 *
 * Phase 1 renders: [Sunny], [Cloudy], [Rain].
 * Remaining variants are reserved for Phase 2+ weather scenes.
 */
enum class WeatherType {
    Sunny,
    PartlyCloudy,
    Cloudy,
    Rain,
    Storm,
    Snow,
    Fog,
}

/**
 * A lightweight, UI-ready snapshot of current weather conditions.
 *
 * **Phase 1 contract**: this model is populated from static preview data only.
 * No network calls, no [android.location.Location] access.
 *
 * @param city        City display name (e.g. "东京").
 * @param cityLatin   Romanised city name (e.g. "Tokyo").
 * @param temperature Current temperature in °C (integer, no decimal clutter).
 * @param condition   Human-readable condition string (e.g. "多云").
 * @param feelsLike   Apparent temperature in °C.
 * @param humidity    Relative humidity 0–100 (%).
 * @param weatherType The [WeatherType] variant driving the visual scene.
 */
data class WeatherPreview(
    val city: String,
    val cityLatin: String,
    val temperature: Int,
    val condition: String,
    val feelsLike: Int,
    val humidity: Int,
    val weatherType: WeatherType,
)

/**
 * A single slot in the hourly forecast strip.
 *
 * @param label       Time label — "现在", "16:00", "17:00", etc.
 * @param temperature Temperature in °C at this slot.
 * @param weatherType [WeatherType] for the icon displayed at this slot.
 */
data class ForecastSlot(
    val label: String,
    val temperature: Int,
    val weatherType: WeatherType,
)

// ─── Static Preview Data ──────────────────────────────────────────────────────
// These are the ONLY weather data sources permitted in Phase 1.

/** Canonical static preview — displayed on the Home Screen in Phase 1. */
val PreviewWeatherTokyo = WeatherPreview(
    city        = "东京",
    cityLatin   = "Tokyo",
    temperature = 23,
    condition   = "多云",
    feelsLike   = 22,
    humidity    = 71,
    weatherType = WeatherType.Cloudy,
)

/** Hourly forecast slots for the preview (static, no API). */
val PreviewForecastSlots = listOf(
    ForecastSlot(label = "现在",  temperature = 23, weatherType = WeatherType.Cloudy),
    ForecastSlot(label = "16:00", temperature = 23, weatherType = WeatherType.Cloudy),
    ForecastSlot(label = "17:00", temperature = 22, weatherType = WeatherType.PartlyCloudy),
    ForecastSlot(label = "18:00", temperature = 21, weatherType = WeatherType.Rain),
    ForecastSlot(label = "19:00", temperature = 20, weatherType = WeatherType.Rain),
    ForecastSlot(label = "20:00", temperature = 19, weatherType = WeatherType.Rain),
)
