package com.example.mistyweather.weather.data.mapper

import com.example.mistyweather.weather.data.remote.WeatherResponseDto
import com.example.mistyweather.weather.domain.DomainForecastSlot
import com.example.mistyweather.weather.domain.WeatherCondition
import com.example.mistyweather.weather.domain.WeatherState
import kotlin.math.roundToInt

// ─── Weather DTO Mapper (Architectural Firewall) ──────────────────────────────
//
// Phase 4.2: Unidirectional mapper translating external REST DTOs into pure
// domain [WeatherState].
//
// Invariants:
//   1. External DTOs NEVER cross beyond this mapper into UI, ViewModel, or Canvas layers.
//   2. WMO weather codes are translated to pure [WeatherCondition] using domain logic.
//   3. MinSdk 24 compatible string parsing for ISO 8601 timestamps (no API 26-only APIs).
//   4. Missing fields or nulls fall back gracefully without exceptions.
//
// ─────────────────────────────────────────────────────────────────────────────

object WeatherDtoMapper {

    /**
     * Maps an Open-Meteo [WeatherResponseDto] to a domain [WeatherState].
     *
     * @param dto           Raw response from Open-Meteo REST API.
     * @param cityName      Localized city display name (e.g. "东京").
     * @param cityNameLatin Latin/Romanized city display name (e.g. "Tokyo").
     */
    fun toDomainState(
        dto: WeatherResponseDto,
        cityName: String = "东京",
        cityNameLatin: String = "Tokyo",
    ): WeatherState {
        val current = dto.current
        val temp = current.temperature2m.roundToInt()
        val feelsLike = current.apparentTemperature?.roundToInt() ?: temp
        val humidity = current.relativeHumidity2m
        val condition = WeatherCondition.fromWmoCode(current.weatherCode)

        // Sunrise & sunset times (e.g. "2026-09-28T05:32" -> "05:32")
        val sunrise = dto.daily?.sunrise?.firstOrNull()?.let(::extractTimePart) ?: "05:32"
        val sunset = dto.daily?.sunset?.firstOrNull()?.let(::extractTimePart) ?: "17:28"

        // Map hourly forecast slots
        val hourlySlots = mapHourlyForecast(dto, condition, temp)

        return WeatherState(
            cityName       = cityName,
            cityNameLatin  = cityNameLatin,
            temperature    = temp,
            feelsLike      = feelsLike,
            humidity       = humidity,
            condition      = condition,
            weatherType    = condition.canonicalWeatherType,
            sunriseTime    = sunrise,
            sunsetTime     = sunset,
            hourlyForecast = hourlySlots,
        )
    }

    /**
     * Maps and slices hourly forecast items, anchoring the first slot at "现在".
     */
    private fun mapHourlyForecast(
        dto: WeatherResponseDto,
        currentCondition: WeatherCondition,
        currentTemp: Int,
    ): List<DomainForecastSlot> {
        val hourly = dto.hourly ?: return listOf(
            DomainForecastSlot(
                timeLabel   = "现在",
                temperature = currentTemp,
                condition   = currentCondition,
                weatherType = currentCondition.canonicalWeatherType,
            )
        )

        val times = hourly.time
        val temps = hourly.temperature2m
        val codes = hourly.weatherCode

        val count = minOf(times.size, temps.size, codes.size)
        if (count == 0) {
            return listOf(
                DomainForecastSlot(
                    timeLabel   = "现在",
                    temperature = currentTemp,
                    condition   = currentCondition,
                    weatherType = currentCondition.canonicalWeatherType,
                )
            )
        }

        // Align with current time if available, otherwise start from index 0
        val currentTimeIso = dto.current.time
        val startIndex = if (!currentTimeIso.isNullOrEmpty()) {
            val matched = times.indexOfFirst { it.startsWith(currentTimeIso.take(13)) }
            if (matched >= 0) matched else 0
        } else {
            0
        }

        val slots = mutableListOf<DomainForecastSlot>()
        val maxSlots = 24
        val endIndex = minOf(count, startIndex + maxSlots)

        for (i in startIndex until endIndex) {
            val isFirst = (i == startIndex)
            val timeLabel = if (isFirst) "现在" else extractTimePart(times[i])
            val slotTemp = temps[i].roundToInt()
            val slotCondition = WeatherCondition.fromWmoCode(codes[i])
            slots.add(
                DomainForecastSlot(
                    timeLabel   = timeLabel,
                    temperature = slotTemp,
                    condition   = slotCondition,
                    weatherType = slotCondition.canonicalWeatherType,
                )
            )
        }

        return if (slots.isEmpty()) {
            listOf(
                DomainForecastSlot(
                    timeLabel   = "现在",
                    temperature = currentTemp,
                    condition   = currentCondition,
                    weatherType = currentCondition.canonicalWeatherType,
                )
            )
        } else {
            slots
        }
    }

    /**
     * Extracts "HH:mm" from ISO 8601 strings like "2026-09-28T16:00".
     * MinSdk 24 safe (does not require API 26 java.time).
     */
    internal fun extractTimePart(isoString: String): String {
        return when {
            isoString.contains("T") -> isoString.substringAfter("T").take(5)
            isoString.contains(" ") -> isoString.substringAfter(" ").take(5)
            isoString.length >= 5 -> isoString.takeLast(5)
            else -> isoString
        }
    }
}
